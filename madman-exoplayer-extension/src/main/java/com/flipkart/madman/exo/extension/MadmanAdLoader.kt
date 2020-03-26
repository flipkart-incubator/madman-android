/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (C) 2020 Flipkart Internet Pvt Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.madman.exo.extension

import android.content.Context
import android.net.Uri
import android.os.Looper
import android.os.SystemClock
import android.view.ViewGroup
import androidx.annotation.IntDef
import com.flipkart.madman.Madman
import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.enums.AdEventType
import com.flipkart.madman.listener.AdErrorListener
import com.flipkart.madman.listener.AdEventListener
import com.flipkart.madman.listener.AdLoadListener
import com.flipkart.madman.manager.AdManager
import com.flipkart.madman.network.NetworkLayer
import com.flipkart.madman.network.model.NetworkAdRequest
import com.flipkart.madman.network.model.StringAdRequest
import com.flipkart.madman.provider.ContentProgressProvider
import com.flipkart.madman.provider.Progress
import com.flipkart.madman.renderer.DefaultAdRenderer
import com.flipkart.madman.renderer.player.AdPlayer
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.ads.AdPlaybackState
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.source.ads.AdsMediaSource.AdLoadException
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.MimeTypes
import java.io.IOException
import java.util.*

/**
 * [AdsLoader] using the Madman SDK. All methods must be called on the main thread.
 *
 * The player instance that will play the loaded ads must be set before playback using [ ][.setPlayer]. If the ads loader is no longer required,
 * it must be released by calling [.release].
 *
 * Modified version of the IMAAdsLoader
 */
class MadmanAdLoader private constructor(
    context: Context,
    networkLayer: NetworkLayer,
    private val adTagUri: Uri?,
    private val adsResponse: String?
) : Player.EventListener, AdsLoader, AdPlayer, ContentProgressProvider, AdErrorListener,
    AdLoadListener, AdEventListener {
    private var debug = true

    private val madman: Madman
    private val period: Timeline.Period
    private val adCallbacks: MutableList<AdPlayer.AdPlayerCallback>

    private var nextPlayer: Player? = null
    private var pendingAdRequestContext: Any? = null
    private var supportedMimeTypes: List<String>? = null
    private var eventListener: AdsLoader.EventListener? = null
    private var player: Player? = null
    private var lastContentProgress: Progress? = null
    private var lastAdProgress: Progress? = null
    private var lastVolumePercentage: Int = 0

    private var adsManager: AdManager? = null
    private var pendingAdLoadError: AdLoadException? = null
    private var timeline: Timeline? = null
    private var contentDurationMs: Long = 0
    private var podIndexOffset: Int = 0
    private var adPlaybackState: AdPlaybackState = AdPlaybackState.NONE

    /**
     * The expected ad group index that Madman should load next.
     */
    private var expectedAdGroupIndex: Int = 0
    /**
     * The index of the current ad group that Madman is loading.
     */
    private var adGroupIndex: Int = 0
    /**
     * Whether Madman has sent an ad event to pause content since the last resume content event.
     */
    private var pausedContent: Boolean = false
    /**
     * The current ad playback state.
     */
    @AdState
    private var adState: Int = 0

    private var sentContentComplete: Boolean = false

    /**
     * Whether the player is playing an ad.
     */
    private var playingAd: Boolean = false
    /**
     * If the player is playing an ad, stores the ad index in its ad group. [C.INDEX_UNSET]
     * otherwise.
     */
    private var playingAdIndexInAdGroup: Int = 0
    /**
     * Whether there's a pending ad preparation error which Madman needs to be notified of when it
     * transitions from playing content to playing the ad.
     */
    private var shouldNotifyAdPrepareError: Boolean = false
    /**
     * If a content period has finished but Madman has not yet called [.playAd], stores the value
     * of [SystemClock.elapsedRealtime] when the content stopped playing. This can be used to
     * determine a fake, increasing content position. [C.TIME_UNSET] otherwise.
     */
    private var fakeContentProgressElapsedRealtimeMs: Long = 0
    /**
     * If [.fakeContentProgressElapsedRealtimeMs] is set, stores the offset from which the
     * content progress should increase. [C.TIME_UNSET] otherwise.
     */
    private var fakeContentProgressOffsetMs: Long = 0
    /**
     * Stores the pending content position when a seek operation was intercepted to play an ad.
     */
    private var pendingContentPositionMs: Long = 0
    /**
     * Whether [.getContentProgress] ()} has sent [.pendingContentPositionMs] to Madman.
     */
    private var sentPendingContentPositionMs: Boolean = false

    /**
     * Builder for [MadmanAdLoader].
     */
    class Builder
    /**
     * Creates a new builder for [MadmanAdLoader].
     *
     * @param context The context;
     */
        (private val context: Context, private val networkLayer: NetworkLayer) {

        /**
         * Returns a new [MadmanAdLoader] for the specified ad tag.
         *
         * @param adTagUri The URI of a compatible ad tag to load. See
         * https://developers.google.com/interactive-media-ads/docs/sdks/android/compatibility for
         * information on compatible ad tags.
         * @return The new [MadmanAdLoader].
         */
        fun buildForAdUri(adTagUri: Uri): MadmanAdLoader {
            return MadmanAdLoader(
                context,
                networkLayer,
                adTagUri,
                null
            )
        }

        /**
         * Returns a new [MadmanAdLoader] with the specified sideloaded ads response.
         *
         * @param adsResponse The side loaded VAST, VMAP, or ad rules response to be used instead of
         * making a request via an ad tag URL.
         * @return The new [MadmanAdLoader].
         */
        fun buildForAdsResponse(adsResponse: String): MadmanAdLoader {
            return MadmanAdLoader(
                context,
                networkLayer,
                null,
                adsResponse
            )
        }
    }

    /**
     * The state of ad playback.
     */
    @MustBeDocumented
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(AD_STATE_NONE, AD_STATE_PLAYING, AD_STATE_PAUSED)
    private annotation class AdState

    init {
        Assertions.checkArgument(adTagUri != null || adsResponse != null)
        period = Timeline.Period()
        adCallbacks = ArrayList(/* initialCapacity= */1)

        madman = Madman.Builder()
            .setAdErrorListener(this)
            .setAdEventListener(this)
            .setAdLoadListener(this)
            .setNetworkLayer(networkLayer)
            .build(context)

        fakeContentProgressElapsedRealtimeMs = C.TIME_UNSET
        fakeContentProgressOffsetMs = C.TIME_UNSET
        pendingContentPositionMs = C.TIME_UNSET
        adGroupIndex = C.INDEX_UNSET
        contentDurationMs = C.TIME_UNSET
    }

    /**
     * Requests ads, if they have not already been requested. Must be called on the main thread.
     *
     *
     * Ads will be requested automatically when the player is prepared if this method has not been
     * called, so it is only necessary to call this method if you want to request ads before preparing
     * the player.
     *
     * @param adViewGroup A [ViewGroup] on top of the player that will show any ad UI.
     */
    private fun requestAds(adViewGroup: ViewGroup) {
        if (adsManager != null || pendingAdRequestContext != null) {
            // Ads have already been requested.
            return
        }
        pendingAdRequestContext = Any()

        val adRenderer =
            DefaultAdRenderer.Builder().setPlayer(this).setContainer(adViewGroup).build(null)

        if (adTagUri != null) {
            val request = NetworkAdRequest()
            request.url = adTagUri.toString()
            madman.requestAds(request, adRenderer)
        } else {
            val request = StringAdRequest()
            request.response = adsResponse
            madman.requestAds(request, adRenderer)
        }
    }

    override fun setPlayer(player: Player?) {
        Assertions.checkState(Looper.getMainLooper() == Looper.myLooper())
        Assertions.checkState(
            player == null || player.applicationLooper == Looper.getMainLooper()
        )
        nextPlayer = player
    }

    override fun setSupportedContentTypes(@C.ContentType vararg contentTypes: Int) {
        val supportedMimeTypes = ArrayList<String>()
        for (contentType in contentTypes) {
            when (contentType) {
                C.TYPE_DASH -> supportedMimeTypes.add(MimeTypes.APPLICATION_MPD)
                C.TYPE_HLS -> supportedMimeTypes.add(MimeTypes.APPLICATION_M3U8)
                C.TYPE_OTHER -> supportedMimeTypes.addAll(
                    listOf(
                        MimeTypes.VIDEO_MP4,
                        MimeTypes.VIDEO_WEBM,
                        MimeTypes.VIDEO_H263,
                        MimeTypes.AUDIO_MP4,
                        MimeTypes.AUDIO_MPEG
                    )
                )
            }
        }
        this.supportedMimeTypes = Collections.unmodifiableList(supportedMimeTypes)
    }

    override fun start(
        eventListener: AdsLoader.EventListener,
        adViewProvider: AdsLoader.AdViewProvider
    ) {
        Assertions.checkNotNull(
            nextPlayer, "Set player using adsLoader.setPlayer before preparing the player."
        )
        player = nextPlayer
        this.eventListener = eventListener
        lastVolumePercentage = 0
        lastAdProgress = null
        lastContentProgress = null
        val adViewGroup = adViewProvider.adViewGroup
        player?.addListener(this)
        maybeNotifyPendingAdLoadError()
        if (adPlaybackState != AdPlaybackState.NONE) {
            // Pass the ad playback state to the player, and resume ads if necessary.
            eventListener.onAdPlaybackState(adPlaybackState)
            if (pausedContent && player?.playWhenReady == true) {
                adsManager?.resume()
            }
        } else if (adsManager != null) {
            // Ads have loaded but the ads manager is not initialized.
            startAdPlayback()
        } else {
            // Ads haven't loaded yet, so request them.
            requestAds(adViewGroup)
        }
    }

    override fun stop() {
        if (adsManager != null && pausedContent) {
            adPlaybackState = adPlaybackState.withAdResumePositionUs(
                if (playingAd) C.msToUs(player?.currentPosition ?: 0) else 0
            )
            adsManager?.pause()
        }
        lastAdProgress = getAdProgress()
        lastContentProgress = getContentProgress()
        player?.removeListener(this)
        player = null
        eventListener = null
    }

    override fun release() {
        pendingAdRequestContext = null
        adsManager?.destroy()
        adsManager = null
        pausedContent = false
        adState = AD_STATE_NONE
        pendingAdLoadError = null
        adPlaybackState = AdPlaybackState.NONE
        updateAdPlaybackState()
    }

    override fun handlePrepareError(
        adGroupIndex: Int,
        adIndexInAdGroup: Int,
        exception: IOException
    ) {
        if (player == null) {
            return
        }
        try {
            handleAdPrepareError(adGroupIndex, adIndexInAdGroup, exception)
        } catch (e: Exception) {
            maybeNotifyInternalError("handlePrepareError", e)
        }
    }

    override fun onAdManagerLoaded(manager: AdManager) {
        pendingAdRequestContext = null
        this.adsManager = manager
        if (player != null) {
            // If a player is attached already, start playback immediately.
            try {
                startAdPlayback()
            } catch (e: Exception) {
                maybeNotifyInternalError("onAdManagerLoaded", e)
            }
        }
    }

    // AdEvent.AdEventListener implementation.

    override fun onAdEvent(event: AdEventListener.AdEvent) {
        val adEventType = event.getType()
        if (debug) {
            Log.d(TAG, "onAdEvent: $adEventType")
        }
        if (adsManager == null) {
            Log.w(TAG, "Ignoring AdEvent after release: $adEventType")
            return
        }
        try {
            handleAdEvent(event)
        } catch (e: Exception) {
            maybeNotifyInternalError("onAdEvent", e)
        }
    }

    override fun onAdError(error: AdErrorListener.AdError) {
        //        AdError error = adErrorEvent.getError();
        if (debug) {
            Log.d(TAG, "onAdError" + error.getMessage())
        }
        if (adsManager == null) {
            // No ads were loaded, so allow playback to start without any ads.
            pendingAdRequestContext = null
            adPlaybackState = AdPlaybackState()
            updateAdPlaybackState()
        } else if (isAdGroupLoadError(error)) {
            try {
                handleAdGroupLoadError(IOException(error.getMessage()))
            } catch (e: Exception) {
                maybeNotifyInternalError("onAdError", e)
            }
        }
        if (pendingAdLoadError == null) {
            pendingAdLoadError =
                AdLoadException.createForAllAds(IOException(error.getMessage()))
        }
        maybeNotifyPendingAdLoadError()
    }

    override fun getContentProgress(): Progress {
        if (player == null) {
            return lastContentProgress ?: Progress.UNDEFINED
        }
        val hasContentDuration = contentDurationMs != C.TIME_UNSET
        val contentPositionMs: Long
        if (pendingContentPositionMs != C.TIME_UNSET) {
            sentPendingContentPositionMs = true
            contentPositionMs = pendingContentPositionMs
            expectedAdGroupIndex =
                adPlaybackState.getAdGroupIndexForPositionUs(C.msToUs(contentPositionMs))
        } else if (fakeContentProgressElapsedRealtimeMs != C.TIME_UNSET) {
            val elapsedSinceEndMs =
                SystemClock.elapsedRealtime() - fakeContentProgressElapsedRealtimeMs
            contentPositionMs = fakeContentProgressOffsetMs + elapsedSinceEndMs
            expectedAdGroupIndex =
                adPlaybackState.getAdGroupIndexForPositionUs(C.msToUs(contentPositionMs))
        } else if (sentContentComplete) {
            contentPositionMs = this.contentDurationMs
        } else if (adState == AD_STATE_NONE && !playingAd && hasContentDuration) {
            contentPositionMs = player?.currentPosition ?: 0
            // Update the expected ad group index for the current content position. The update is delayed
            // until MAXIMUM_PRELOAD_DURATION_MS before the ad so that an ad group load error delivered
            // just after an ad group isn't incorrectly attributed to the next ad group.
            val nextAdGroupIndex = adPlaybackState.getAdGroupIndexAfterPositionUs(
                C.msToUs(contentPositionMs), C.msToUs(contentDurationMs)
            )
            if (nextAdGroupIndex != expectedAdGroupIndex && nextAdGroupIndex != C.INDEX_UNSET) {
                var nextAdGroupTimeMs = C.usToMs(adPlaybackState.adGroupTimesUs[nextAdGroupIndex])
                if (nextAdGroupTimeMs == C.TIME_END_OF_SOURCE) {
                    nextAdGroupTimeMs = contentDurationMs
                }
                if (nextAdGroupTimeMs - contentPositionMs < MAXIMUM_PRELOAD_DURATION_MS) {
                    expectedAdGroupIndex = nextAdGroupIndex
                }
            }
        } else {
            return Progress(-1, -1)
        }
        val contentDurationMs =
            if (hasContentDuration) this.contentDurationMs else DURATION_UNSET

        return Progress(contentPositionMs, contentDurationMs)
    }

    // VideoAdPlayer implementation.

    override fun getAdProgress(): Progress {
        if (player == null) {
            return lastAdProgress ?: Progress.UNDEFINED
        } else if (adState != AD_STATE_NONE && playingAd) {
            val adDuration = player?.duration ?: C.TIME_UNSET
            return if (adDuration == C.TIME_UNSET)
                Progress(-1, -1)
            else
                Progress(player?.currentPosition ?: 0, adDuration)
        } else {
            return Progress.UNDEFINED
        }
    }

    override fun loadAd(urlList: List<String>) {
        try {
            if (debug) {
                Log.d(TAG, "loadAd in ad group $adGroupIndex")
            }
            if (adsManager == null) {
                Log.w(TAG, "Ignoring loadAd after release")
                return
            }
            if (adGroupIndex == C.INDEX_UNSET) {
                Log.w(
                    TAG,
                    "Unexpected loadAd without LOADED event; assuming ad group index is actually $expectedAdGroupIndex"
                )
                adGroupIndex = expectedAdGroupIndex
                adsManager?.start()
            }
            val adIndexInAdGroup = getAdIndexInAdGroupToLoad(adGroupIndex)
            if (adIndexInAdGroup == C.INDEX_UNSET) {
                Log.w(TAG, "Unexpected loadAd in an ad group with no remaining unavailable ads")
                return
            }
            adPlaybackState =
                adPlaybackState.withAdUri(adGroupIndex, adIndexInAdGroup, Uri.parse(urlList[0]))
            updateAdPlaybackState()
        } catch (e: Exception) {
            maybeNotifyInternalError("loadAd", e)
        }

    }

    override fun registerAdPlayerCallback(callback: AdPlayer.AdPlayerCallback) {
        adCallbacks.add(callback)
    }

    override fun unregisterAdPlayerCallback(callback: AdPlayer.AdPlayerCallback) {
        adCallbacks.remove(callback)
    }

    override fun playAd() {
        if (debug) {
            Log.d(TAG, "playAd")
        }
        if (adsManager == null) {
            Log.w(TAG, "Ignoring playAd after release")
            return
        }
        when (adState) {
            AD_STATE_PLAYING ->
                Log.w(TAG, "Unexpected playAd without stopAd")
            AD_STATE_NONE -> {
                // Madman is requesting to play the ad, so stop faking the content position.
                fakeContentProgressElapsedRealtimeMs = C.TIME_UNSET
                fakeContentProgressOffsetMs = C.TIME_UNSET
                adState = AD_STATE_PLAYING
                for (i in adCallbacks.indices) {
                    adCallbacks[i].onPlay()
                }
                if (shouldNotifyAdPrepareError) {
                    shouldNotifyAdPrepareError = false
                    for (i in adCallbacks.indices) {
                        adCallbacks[i].onError()
                    }
                }
            }
            AD_STATE_PAUSED -> {
                adState = AD_STATE_PLAYING
                for (i in adCallbacks.indices) {
                    adCallbacks[i].onResume()
                }
            }
            else -> throw IllegalStateException()
        }
        if (player == null) {
            Log.w(TAG, "Unexpected playAd while detached")
        } else if (player?.playWhenReady == false) {
            adsManager?.pause()
        }
    }

    override fun stopAd() {
        if (debug) {
            Log.d(TAG, "stopAd")
        }
        if (adsManager == null) {
            Log.w(TAG, "Ignoring stopAd after release")
            return
        }
        if (player == null) {
            Log.w(TAG, "Unexpected stopAd while detached")
        }
        if (adState == AD_STATE_NONE) {
            Log.w(TAG, "Unexpected stopAd")
            return
        }
        try {
            stopAdInternal()
        } catch (e: Exception) {
            maybeNotifyInternalError("stopAd", e)
        }

    }

    override fun pauseAd() {
        if (debug) {
            Log.d(TAG, "pauseAd")
        }
        if (adState == AD_STATE_NONE) {
            // This method is called after content is resumed.
            return
        }
        adState = AD_STATE_PAUSED
        for (i in adCallbacks.indices) {
            adCallbacks[i].onPause()
        }
    }

    // Player.EventListener implementation.

    override fun onTimelineChanged(
        timeline: Timeline, manifest: Any?, @Player.TimelineChangeReason reason: Int
    ) {
        if (timeline.isEmpty) {
            // The player is being reset or contains no media.
            return
        }
        Assertions.checkArgument(timeline.periodCount == 1)
        this.timeline = timeline
        val contentDurationUs = timeline.getPeriod(0, period).durationUs
        contentDurationMs = C.usToMs(contentDurationUs)
        if (contentDurationUs != C.TIME_UNSET) {
            adPlaybackState = adPlaybackState.withContentDurationUs(contentDurationUs)
        }
        updateStateForPlayerState()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (adsManager == null) {
            return
        }

        if (adState == AD_STATE_PLAYING && !playWhenReady) {
            adsManager?.pause()
            return
        }

        if (adState == AD_STATE_PAUSED && playWhenReady) {
            adsManager?.resume()
            return
        }

        if (adState == AD_STATE_NONE && playbackState == Player.STATE_BUFFERING
            && playWhenReady
        ) {
            checkForContentComplete()
        } else if (adState != AD_STATE_NONE && playbackState == Player.STATE_ENDED) {
            for (i in adCallbacks.indices) {
                adCallbacks[i].onEnded()
            }
            if (debug) {
                Log.d(TAG, "VideoAdPlayerCallback.onEnded in onPlayerStateChanged")
            }
        }
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        if (adState != AD_STATE_NONE) {
            for (i in adCallbacks.indices) {
                adCallbacks[i].onError()
            }
        }
    }

    override fun onPositionDiscontinuity(@Player.DiscontinuityReason reason: Int) {
        if (adsManager == null) {
            return
        }
        if (!playingAd && player?.isPlayingAd == false) {
            checkForContentComplete()
            if (sentContentComplete) {
                for (i in 0 until adPlaybackState.adGroupCount) {
                    if (adPlaybackState.adGroupTimesUs[i] != C.TIME_END_OF_SOURCE) {
                        adPlaybackState = adPlaybackState.withSkippedAdGroup(i)
                    }
                }
                updateAdPlaybackState()
            } else {
                val positionMs = player?.currentPosition ?: 0
                timeline?.getPeriod(0, period)
                val newAdGroupIndex = period.getAdGroupIndexForPositionUs(C.msToUs(positionMs))
                if (newAdGroupIndex != C.INDEX_UNSET) {
                    sentPendingContentPositionMs = false
                    pendingContentPositionMs = positionMs
                    if (newAdGroupIndex != adGroupIndex) {
                        shouldNotifyAdPrepareError = false
                    }
                }
            }
        } else {
            updateStateForPlayerState()
        }
    }

    // Internal methods.

    private fun startAdPlayback() {
        adsManager?.let {
            // Set up the ad playback state, skipping ads based on the start position as required.
            val adGroupTimesUs = getAdGroupTimesUs(it.getCuePoints())
            adPlaybackState = AdPlaybackState(*adGroupTimesUs)
            val contentPositionMs = player?.currentPosition ?: 0
            val adGroupIndexForPosition =
                adPlaybackState.getAdGroupIndexForPositionUs(C.msToUs(contentPositionMs))
            if (adGroupIndexForPosition > 0 && adGroupIndexForPosition != C.INDEX_UNSET) {
                // Skip any ad groups before the one at or immediately before the playback position.
                for (i in 0 until adGroupIndexForPosition) {
                    adPlaybackState = adPlaybackState.withSkippedAdGroup(i)
                }
                // Play ads after the midpoint between the ad to play and the one before it, to avoid issues
                // with rounding one of the two ad times.
                val adGroupForPositionTimeUs = adGroupTimesUs[adGroupIndexForPosition]
                val adGroupBeforeTimeUs = adGroupTimesUs[adGroupIndexForPosition - 1]
                val midpointTimeUs = (adGroupForPositionTimeUs + adGroupBeforeTimeUs) / 2.0
            }

            // Madman indexes any remaining midroll ad pods from 1. A preroll (if present) has index 0.
            // Store an index offset as we want to index all ads (including skipped ones) from 0.
            podIndexOffset = if (adGroupIndexForPosition == 0 && adGroupTimesUs[0] == 0L) {
                // We are playing a preroll.
                0
            } else if (adGroupIndexForPosition == C.INDEX_UNSET) {
                // There's no ad to play which means there's no preroll.
                -1
            } else {
                // We are playing a midroll and any ads before it were skipped.
                adGroupIndexForPosition - 1
            }

            if (adGroupIndexForPosition != C.INDEX_UNSET && hasMidRollAdGroups(adGroupTimesUs)) {
                // Provide the player's initial position to trigger loading and playing the ad.
                pendingContentPositionMs = contentPositionMs
            }

            // Start ad playback.
            adsManager?.init(this)
            updateAdPlaybackState()
            if (debug) {
                Log.d(TAG, "Initialized with ads rendering settings: ")
            }
        }
    }

    private fun handleAdEvent(adEvent: AdEventListener.AdEvent) {
        val ad = adEvent.getAdElement()
        when (adEvent.getType()) {
            AdEventType.LOADED -> {
                // The ad position is not always accurate when using preloading. See [Internal: b/62613240].
                val adPodInfo = ad?.getAdPod()
                adPodInfo?.let {
                    val podIndex = adPodInfo.getPodIndex()
                    adGroupIndex =
                        if (podIndex == -1) adPlaybackState.adGroupCount else podIndex + podIndexOffset
                    val adPosition = adPodInfo.getAdPosition()
                    val adCount = adPodInfo.getTotalAds()
                    adsManager?.start()
                    if (debug) {
                        Log.d(TAG, "Loaded ad $adPosition of $adCount in group $adGroupIndex")
                    }
                    val oldAdCount = adPlaybackState.adGroups?.get(adGroupIndex)?.count
                    if (adCount != oldAdCount) {
                        if (oldAdCount == C.LENGTH_UNSET) {
                            adPlaybackState = adPlaybackState.withAdCount(adGroupIndex, adCount)
                            updateAdPlaybackState()
                        } else {
                            Log.w(
                                TAG,
                                "Unexpected ad count in LOADED, $adCount, expected $oldAdCount"
                            )
                        }
                    }
                    if (adGroupIndex != expectedAdGroupIndex) {
                        Log.w(
                            TAG,
                            "Expected ad group index "
                                    + expectedAdGroupIndex
                                    + ", actual ad group index "
                                    + adGroupIndex
                        )
                        expectedAdGroupIndex = adGroupIndex
                    }
                }
            }
            AdEventType.CONTENT_PAUSE_REQUESTED -> {
                // After CONTENT_PAUSE_REQUESTED, Madman will playAd/pauseAd/stopAd to show one or more ads
                // before sending CONTENT_RESUME_REQUESTED.
                pausedContent = true
                pauseContentInternal()
            }
            AdEventType.TAPPED -> if (eventListener != null) {
                eventListener?.onAdTapped()
            }
            AdEventType.CLICKED -> if (eventListener != null) {
                eventListener?.onAdClicked()
            }
            AdEventType.CONTENT_RESUME_REQUESTED -> {
                pausedContent = false
                resumeContentInternal()
            }
            AdEventType.STARTED, AdEventType.ALL_AD_COMPLETED -> {
            }
            else -> {
            }
        }
    }

    private fun updateStateForPlayerState() {
        val wasPlayingAd = playingAd
        val oldPlayingAdIndexInAdGroup = playingAdIndexInAdGroup
        playingAd = player?.isPlayingAd ?: false
        playingAdIndexInAdGroup =
            if (playingAd) player?.currentAdIndexInAdGroup ?: 0 else C.INDEX_UNSET
        val adFinished = wasPlayingAd && playingAdIndexInAdGroup != oldPlayingAdIndexInAdGroup
        if (adFinished) {
            // Madman is waiting for the ad playback to finish so invoke the callback now.
            // Either CONTENT_RESUME_REQUESTED will be passed next, or playAd will be called again.
            for (i in adCallbacks.indices) {
                adCallbacks[i].onEnded()
            }
            if (debug) {
                Log.d(
                    TAG,
                    "VideoAdPlayerCallback.onEnded in onTimelineChanged/onPositionDiscontinuity"
                )
            }
        }
        if (!sentContentComplete && !wasPlayingAd && playingAd && adState == AD_STATE_NONE) {
            adGroupIndex = player?.currentAdGroupIndex ?: 0
            // Madman hasn't called playAd yet, so fake the content position.
            fakeContentProgressElapsedRealtimeMs = SystemClock.elapsedRealtime()
            fakeContentProgressOffsetMs = C.usToMs(adPlaybackState.adGroupTimesUs[adGroupIndex])
            if (fakeContentProgressOffsetMs == C.TIME_END_OF_SOURCE) {
                fakeContentProgressOffsetMs = contentDurationMs
            }
        }
    }

    private fun resumeContentInternal() {
        if (adState != AD_STATE_NONE) {
            adState = AD_STATE_NONE
            if (debug) {
                Log.d(TAG, "Unexpected CONTENT_RESUME_REQUESTED without stopAd")
            }
        }
        if (adGroupIndex != C.INDEX_UNSET) {
            adPlaybackState = adPlaybackState.withSkippedAdGroup(adGroupIndex)
            adGroupIndex = C.INDEX_UNSET
            updateAdPlaybackState()
        }
    }

    private fun pauseContentInternal() {
        adState = AD_STATE_NONE
        if (sentPendingContentPositionMs) {
            pendingContentPositionMs = C.TIME_UNSET
            sentPendingContentPositionMs = false
        }
    }

    private fun stopAdInternal() {
        adState = AD_STATE_NONE
        val adIndexInAdGroup = adPlaybackState.adGroups[adGroupIndex].firstAdIndexToPlay
        // TODO: Handle the skipped event so the ad can be marked as skipped rather than played.
        adPlaybackState =
            adPlaybackState.withPlayedAd(adGroupIndex, adIndexInAdGroup).withAdResumePositionUs(0)
        updateAdPlaybackState()
        if (!playingAd) {
            adGroupIndex = C.INDEX_UNSET
        }
    }

    private fun handleAdGroupLoadError(error: Exception) {
        val adGroupIndex =
            if (this.adGroupIndex == C.INDEX_UNSET) expectedAdGroupIndex else this.adGroupIndex
        if (adGroupIndex == C.INDEX_UNSET) {
            // Drop the error, as we don't know which ad group it relates to.
            return
        }
        var adGroup: AdPlaybackState.AdGroup = adPlaybackState.adGroups[adGroupIndex]
        if (adGroup.count == C.LENGTH_UNSET) {
            adPlaybackState =
                adPlaybackState.withAdCount(adGroupIndex, 1.coerceAtLeast(adGroup.states.size))
            adGroup = adPlaybackState.adGroups[adGroupIndex]
        }
        for (i in 0 until adGroup.count) {
            if (adGroup.states[i] == AdPlaybackState.AD_STATE_UNAVAILABLE) {
                if (debug) {
                    Log.d(TAG, "Removing ad $i in ad group $adGroupIndex")
                }
                adPlaybackState = adPlaybackState.withAdLoadError(adGroupIndex, i)
            }
        }
        updateAdPlaybackState()
        if (pendingAdLoadError == null) {
            pendingAdLoadError = AdLoadException.createForAdGroup(error, adGroupIndex)
        }
        pendingContentPositionMs = C.TIME_UNSET
        fakeContentProgressElapsedRealtimeMs = C.TIME_UNSET
    }

    private fun handleAdPrepareError(
        adGroupIndex: Int,
        adIndexInAdGroup: Int,
        exception: Exception
    ) {
        if (debug) {
            Log.d(
                TAG, "Prepare error for ad $adIndexInAdGroup in group $adGroupIndex", exception
            )
        }
        if (adsManager == null) {
            Log.w(TAG, "Ignoring ad prepare error after release")
            return
        }
        if (adState == AD_STATE_NONE) {
            // Send Madman a content position at the ad group so that it will try to play it, at which point
            // we can notify that it failed to load.
            fakeContentProgressElapsedRealtimeMs = SystemClock.elapsedRealtime()
            fakeContentProgressOffsetMs = C.usToMs(adPlaybackState.adGroupTimesUs[adGroupIndex])
            if (fakeContentProgressOffsetMs == C.TIME_END_OF_SOURCE) {
                fakeContentProgressOffsetMs = contentDurationMs
            }
            shouldNotifyAdPrepareError = true
        } else {
            // We're already playing an ad.
            if (adIndexInAdGroup > playingAdIndexInAdGroup) {
                // Mark the playing ad as ended so we can notify the error on the next ad and remove it,
                // which means that the ad after will load (if any).
                for (i in adCallbacks.indices) {
                    adCallbacks[i].onEnded()
                }
            }
            playingAdIndexInAdGroup =
                adPlaybackState.adGroups?.get(adGroupIndex)?.firstAdIndexToPlay ?: 0
            for (i in adCallbacks.indices) {
                adCallbacks[i].onError()
            }
        }
        adPlaybackState = adPlaybackState.withAdLoadError(adGroupIndex, adIndexInAdGroup)
        updateAdPlaybackState()
    }

    private fun checkForContentComplete() {
        if ((contentDurationMs != C.TIME_UNSET && pendingContentPositionMs == C.TIME_UNSET
                    && player?.contentPosition ?: 0 + END_OF_CONTENT_POSITION_THRESHOLD_MS >= contentDurationMs
                    && !sentContentComplete)
        ) {
            this.adsManager?.contentComplete()
            if (debug) {
                Log.d(TAG, "adsLoader.contentComplete")
            }
            sentContentComplete = true
            // After sending content complete Madman will not poll the content position, so set the expected
            // ad group index.
            expectedAdGroupIndex =
                adPlaybackState.getAdGroupIndexForPositionUs(C.msToUs(contentDurationMs))
        }
    }

    private fun updateAdPlaybackState() {
        // Ignore updates while detached. When a player is attached it will receive the latest state.
        eventListener?.onAdPlaybackState(adPlaybackState)
    }

    /**
     * Returns the next ad index in the specified ad group to load, or [C.INDEX_UNSET] if all
     * ads in the ad group have loaded.
     */
    private fun getAdIndexInAdGroupToLoad(adGroupIndex: Int): Int {
        @AdState val states = adPlaybackState.adGroups?.get(adGroupIndex)?.states ?: IntArray(0)
        var adIndexInAdGroup = 0
        while ((adIndexInAdGroup < states.size && states[adIndexInAdGroup] != AdPlaybackState.AD_STATE_UNAVAILABLE)) {
            adIndexInAdGroup++
        }
        return if (adIndexInAdGroup == states.size) C.INDEX_UNSET else adIndexInAdGroup
    }

    private fun maybeNotifyPendingAdLoadError() {
        if (pendingAdLoadError != null) {
            eventListener?.onAdLoadError(pendingAdLoadError, DataSpec(adTagUri))
            pendingAdLoadError = null
        }
    }

    private fun maybeNotifyInternalError(name: String, cause: Exception) {
        val message = "Internal error in $name"
        Log.e(TAG, message, cause)
        // We can't recover from an unexpected error in general, so skip all remaining ads.
        if (adPlaybackState == AdPlaybackState.NONE) {
            adPlaybackState = AdPlaybackState.NONE
        } else {
            for (i in 0 until adPlaybackState.adGroupCount) {
                adPlaybackState = adPlaybackState.withSkippedAdGroup(i)
            }
        }
        updateAdPlaybackState()
        eventListener?.onAdLoadError(
            AdLoadException.createForUnexpected(RuntimeException(message, cause)),
            DataSpec(adTagUri)
        )
    }

    companion object {

        private const val TAG = "MadmanAdLoader"

        private const val DURATION_UNSET = -1L

        /**
         * Threshold before the end of content at which Madman is notified that content is complete if the
         * player buffers, in milliseconds.
         */
        private const val END_OF_CONTENT_POSITION_THRESHOLD_MS: Long = 5000

        /**
         * The maximum duration before an ad break that Madman may start preloading the next ad.
         */
        private const val MAXIMUM_PRELOAD_DURATION_MS: Long = 8000

        /**
         * The ad playback state when Madman is not playing an ad.
         */
        private const val AD_STATE_NONE = 0
        /**
         * The ad playback state when Madman has called [.playAd] and not [.pauseAd].
         */
        private const val AD_STATE_PLAYING = 1
        /**
         * The ad playback state when Madman has called [.pauseAd] while playing an ad.
         */
        private const val AD_STATE_PAUSED = 2

        private fun getAdGroupTimesUs(cuePoints: List<Float>): LongArray {
            if (cuePoints.isEmpty()) {
                // If no cue points are specified, there is a preroll ad.
                return longArrayOf(0)
            }

            val count = cuePoints.size
            val adGroupTimesUs = LongArray(count)
            var adGroupIndex = 0
            for (i in 0 until count) {
                val cuePoint = cuePoints[i].toDouble()
                if (cuePoint == -1.0) {
                    adGroupTimesUs[count - 1] = C.TIME_END_OF_SOURCE
                } else {
                    adGroupTimesUs[adGroupIndex++] = (C.MICROS_PER_SECOND * cuePoint).toLong()
                }
            }
            // Cue points may be out of order, so sort them.
            Arrays.sort(adGroupTimesUs, 0, adGroupIndex)
            return adGroupTimesUs
        }

        private fun isAdGroupLoadError(adError: AdErrorListener.AdError): Boolean {
            // TODO: Find out what other errors need to be handled (if any), and whether each one relates to
            // a single ad, ad group or the whole timeline.
            return adError.getType() == AdErrorType.NO_MEDIA_URL
        }

        private fun hasMidRollAdGroups(adGroupTimesUs: LongArray): Boolean {
            return when (adGroupTimesUs.size) {
                1 -> adGroupTimesUs[0] != 0L && adGroupTimesUs[0] != C.TIME_END_OF_SOURCE
                2 -> adGroupTimesUs[0] != 0L || adGroupTimesUs[1] != C.TIME_END_OF_SOURCE
                else -> // There's at least one mid roll ad group, as adGroupTimesUs is never empty.
                    true
            }
        }
    }
}
