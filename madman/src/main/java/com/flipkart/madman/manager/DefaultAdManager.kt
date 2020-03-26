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
package com.flipkart.madman.manager

import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.enums.StringErrorConstants
import com.flipkart.madman.component.enums.VastErrors
import com.flipkart.madman.component.model.vast.VASTData
import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.listener.AdErrorListener
import com.flipkart.madman.listener.AdEventListener
import com.flipkart.madman.listener.impl.AdError
import com.flipkart.madman.loader.AdLoader
import com.flipkart.madman.logger.LogUtil
import com.flipkart.madman.manager.data.VastAdProvider
import com.flipkart.madman.manager.data.helper.AdDataHelper
import com.flipkart.madman.manager.event.Event
import com.flipkart.madman.manager.handler.AdProgressHandler
import com.flipkart.madman.manager.handler.ContentProgressHandler
import com.flipkart.madman.manager.helper.Constant.FIRST_QUARTILE
import com.flipkart.madman.manager.helper.Constant.MIDPOINT
import com.flipkart.madman.manager.helper.Constant.THIRD_QUARTILE
import com.flipkart.madman.network.NetworkLayer
import com.flipkart.madman.network.model.NetworkAdRequest
import com.flipkart.madman.provider.ContentProgressProvider
import com.flipkart.madman.provider.Progress
import com.flipkart.madman.renderer.AdRenderer
import com.flipkart.madman.renderer.callback.ViewClickListener
import com.flipkart.madman.renderer.settings.RenderingSettings
import kotlin.math.ceil

/**
 * Core part of Madman
 */
open class DefaultAdManager(
    private val data: VMAPData,
    adLoader: AdLoader<NetworkAdRequest>,
    networkLayer: NetworkLayer,
    private val adRenderer: AdRenderer,
    adEventListener: AdEventListener,
    private val adErrorListener: AdErrorListener
) : BaseAdManager(data, adRenderer, adLoader, networkLayer, adEventListener),
    VastAdProvider.Listener, AdProgressHandler.AdProgressUpdateListener, ViewClickListener {

    /** ad rendering settings **/
    private val adRenderingSettings: RenderingSettings by lazy {
        adRenderer.getRenderingSettings()
    }

    /** list of all cue points **/
    private val adCuePoints: List<Float> by lazy {
        AdDataHelper.getCuePoints(data)
    }

    /** last ad progress percentage **/
    private var lastAdProgressPercentage: Float = 0F

    override fun getCuePoints(): List<Float> {
        return adCuePoints
    }

    override fun init(contentProgressProvider: ContentProgressProvider) {
        super.init(contentProgressProvider)
        adRenderer.registerViewClickListener(this)
    }

    /**
     * Gets called when the media is ready
     * ie the media progress is not [Progress.UNDEFINED]
     */
    override fun onInit() {
        if (AdDataHelper.hasPreRollAds(data)) {
            LogUtil.log("pre-roll ads present")
        } else {
            LogUtil.log("no pre-roll, resuming content")
            notifyAndTrackEvent(Event.CONTENT_RESUME)
        }
        startContentHandler()
    }

    override fun start() {
        LogUtil.log("[AdManager] : start")
    }

    override fun pause() {
        LogUtil.log("[AdManager] : pause")
        pauseAdIfPlaying()
        removeAdMessageHandler()
    }

    override fun resume() {
        LogUtil.log("[AdManager] : resume")
        resumeAdIfPaused()
        startAdMessageHandler()
    }

    /**
     * Gets called every x seconds by [ContentProgressHandler]
     *
     * Provides the progress of the media running.
     * This is the place where the decision logic of which ad break to play happens
     */
    override fun onContentProgressUpdate(progress: Progress) {
        val currentTime = ceil(progress.currentTime)
        val duration = ceil(progress.duration)
        LogUtil.log("onContentProgressUpdate: progress: $currentTime, duration: $duration")

        /**
         * Update the playback state with the content duration if not set
         */
        adPlaybackState = adPlaybackState.withContentProgress(currentTime, duration)

        /**
         * Get the playable ad break from ad playback state depending on the current time of the media
         */
        val adBreak = adPlaybackState.getPlayableAdBreak(currentTime, duration, adBreakFinder)
        adBreak?.let {
            when (it.state) {
                /**
                 * If the ad break has been played, send all the play finished events
                 */
                AdBreak.AdBreakState.PLAYED -> {
                    notifyAndTrackEvent(Event.AD_STOPPED)
                    notifyAndTrackEvent(Event.AD_COMPLETED)
                    notifyAndTrackEvent(Event.CONTENT_RESUME)
                    adPlaybackState.markAdBreakAsCompleted()
                    onContentCompleted()
                }
                /**
                 * If the ad break is skipped
                 */
                AdBreak.AdBreakState.SKIPPED -> {
                    notifyAndTrackEvent(Event.AD_STOPPED)
                    notifyAndTrackEvent(Event.CONTENT_RESUME)
                    adPlaybackState.markAdBreakAsCompleted()
                    onContentCompleted()
                }
                /**
                 * If the ad break has been loaded which means we have the vast for the given ad
                 * send the play ad event as soon as current time is same as the cue point
                 */
                AdBreak.AdBreakState.LOADED -> {
                    if (canPlayAdBreak(it, currentTime, duration)) {
                        notifyAndTrackEvent(Event.PLAY_AD)
                        adPlaybackState.updateStateForAdBreak(AdBreak.AdBreakState.PLAYING)
                        adPlaybackState.markAdBreakAsPlaying()
                    }
                }
                /**
                 * If the ad break has not been played yet, and the cue point is within the prefetch range,
                 * fetch the ad and keep it ready
                 */
                AdBreak.AdBreakState.NOT_PLAYED -> {
                    if (canPreloadAdBreak(it, currentTime)) {
                        adPlaybackState.updateStateForAdBreak(AdBreak.AdBreakState.LOADING)
                        vastAdProvider.getVASTAd(it, this)
                    }
                }
                /**
                 * Do nothing for these states as of now
                 */
                AdBreak.AdBreakState.LOADING, AdBreak.AdBreakState.PLAYING -> {
                    // do nothing for now
                }
            }
        }

        /** send message after 1 second **/
        contentProgressHandler.sendMessageDelayed(MEDIA_PROGRESS_HANDLER_DELAY)
    }

    /**
     * Gets called every x seconds by [AdProgressHandler]
     *
     * Provides the progress of the ad running. Returns [Progress.UNDEFINED] till the ad starts playing
     */
    override fun onAdProgressUpdate(progress: Progress) {
        if (progress == Progress.UNDEFINED) {
            /** progress is undefined, try again **/
            adProgressHandler.sendMessage()
            return
        }

        LogUtil.log("onAdProgressUpdate: progress: ${progress.currentTime}, duration: ${progress.duration}")
        val percentage = progress.currentTime / progress.duration
        when {
            /**
             * if the previous percentage is less than 0.25F and current percentage is greater than 0.25F,
             * fire the first quartile event
             */
            lastAdProgressPercentage > 0 && lastAdProgressPercentage < FIRST_QUARTILE && FIRST_QUARTILE < percentage -> {
                notifyAndTrackEvent(Event.FIRST_QUARTILE)
            }
            /**
             * if the previous percentage is less than 0.50F and current percentage is greater than 0.50F,
             * fire the midpoint event
             */
            lastAdProgressPercentage > 0 && lastAdProgressPercentage < MIDPOINT && MIDPOINT < percentage -> {
                notifyAndTrackEvent(Event.MIDPOINT)
            }
            /**
             * if the previous percentage is less than 0.75F and current percentage is greater than 0.75F,
             * fire the third quartile event
             */
            lastAdProgressPercentage > 0 && lastAdProgressPercentage < THIRD_QUARTILE && THIRD_QUARTILE < percentage -> {
                notifyAndTrackEvent(Event.THIRD_QUARTILE)
            }
            /**
             * if the previous percentage is less than 0F and current percentage is greater than 0F,
             * fire the started event
             */
            lastAdProgressPercentage == 0F && percentage > 0F -> {
                currentAd?.let {
                    adRenderer.renderView(it.getAdElement())
                }
                notifyAndTrackEvent(Event.AD_STARTED)
            }
            /**
             * for all other cases, fire the progress event
             */
            lastAdProgressPercentage < percentage -> {
                notifyAndTrackEvent(Event.AD_PROGRESS)
            }
        }

        /** store the percentage **/
        lastAdProgressPercentage = percentage

        /** send message after "x" ms **/
        adProgressHandler.sendMessageAfter(AD_PROGRESS_HANDLER_DELAY)
    }

    /**
     * Ad play callback. This is called when the player's playAd is called.
     * When playAd is called, the plugin / client sends the onPlay callback which notifies the
     * sdk to start the ad playback.
     */
    override fun onAdPlay() {
        removeContentHandler()
        startAdMessageHandler()
        adRenderer.createView()
    }

    /**
     * Ad ended callback from the player.
     * Remove the ad message handlers and update the state
     */
    override fun onAdEnded() {
        removeAdMessageHandler()
        startContentHandler()
        lastAdProgressPercentage = 0F
        adRenderer.removeView()
        adPlaybackState.updateStateForAdBreak(AdBreak.AdBreakState.PLAYED)
    }

    /**
     * Ad error callback from the player
     */
    override fun onAdError() {
        notifyAndTrackEvent(Event.AD_ERROR)
        onAdEnded()
    }

    /**
     * Ad pause callback from the player
     */
    override fun onAdPause() {
        notifyAndTrackEvent(Event.AD_PAUSED)
    }

    /**
     * Ad resume callback from the player
     */
    override fun onAdResume() {
        notifyAndTrackEvent(Event.AD_RESUMED)
    }

    /**
     * Called when the user clicks on skip ad
     */
    override fun onSkipAdClick() {
        notifyAndTrackEvent(Event.AD_SKIPPED)
        onAdEnded()
        adPlaybackState.updateStateForAdBreak(AdBreak.AdBreakState.SKIPPED)
    }

    /**
     * Called when the user clicks on the ad view
     */
    override fun onAdViewClick() {
        notifyAndTrackEvent(Event.AD_TAPPED)
    }

    /**
     * Called when the user clicks on the cta's such as learn more
     */
    override fun onClickThroughClick() {
        notifyAndTrackEvent(Event.AD_CTA_CLICKED)
    }

    /**
     * Called when the content is completed
     */
    override fun contentComplete() {
        adPlaybackState.markContentCompleted()
        if (adPlaybackState.isPostRollPlayed()) {
            onContentCompleted()
        }
    }

    override fun destroy() {
        super.destroy()
        adRenderer.destroy()
        adRenderer.unregisterViewClickListener(this)
    }

    /**
     * Called on vast fetch error, may be due to network error or no ad present in the vast
     * throw error with the message
     */
    override fun onVastFetchError(errorType: AdErrorType, message: String) {
        adErrorListener.onAdError(AdError(errorType, message))
        adPlaybackState.updateStateForAdBreak(AdBreak.AdBreakState.SKIPPED)
        notifyAndTrackEvent(Event.VAST_ERROR, VastErrors.mapErrorTypeToInt(errorType))
    }

    /**
     * Called on successful fetch of [VASTData]
     * fire the ad loaded event
     */
    override fun onVastFetchSuccess(vastData: VASTData) {
        currentAd = adPlaybackState.getAdFrom(vastData)
        adPlaybackState.updateStateForAdBreak(AdBreak.AdBreakState.LOADED)
        if (currentAd?.getAdMediaUrls()?.isNotEmpty() == true) {
            notifyAndTrackEvent(Event.LOAD_AD)
        } else {
            notifyAndTrackEvent(
                Event.AD_ERROR,
                VastErrors.mapErrorTypeToInt(AdErrorType.NO_MEDIA_URL)
            )
            adErrorListener.onAdError(
                AdError(
                    AdErrorType.NO_MEDIA_URL,
                    StringErrorConstants.NO_MEDIA_URL
                )
            )
        }
    }

    /**
     * if the content is completed or the post roll is played,
     * fire all ads completed event and remove all the handlers.
     */
    private fun onContentCompleted() {
        if (adPlaybackState.contentCompleted || adPlaybackState.isPostRollPlayed()) {
            removeContentHandler()
            removeAdMessageHandler()
            notifyAndTrackEvent(Event.ALL_AD_COMPLETED)
        }
    }

    /**
     * check if the given ad break be preloaded
     */
    private fun canPreloadAdBreak(adBreak: AdBreak, currentTime: Float): Boolean {
        return adBreak.timeOffsetInSec - currentTime <= adRenderingSettings.getPreloadTime() && adBreak.timeOffsetInSec != -1f
    }

    /**
     * check if ad break be played
     */
    private fun canPlayAdBreak(adBreak: AdBreak, currentTime: Float, duration: Float): Boolean {
        return currentTime >= adBreak.timeOffsetInSec || currentTime >= duration
    }

    /**
     * pause the ad if playing
     */
    private fun pauseAdIfPlaying() {
        if (adPlaybackState.isAdPlaying) {
            notifyAndTrackEvent(Event.PAUSE_AD)
            adPlaybackState.isAdPaused = true
        }
    }

    /**
     * resumed the ad if paused
     */
    private fun resumeAdIfPaused() {
        if (adPlaybackState.isAdPaused) {
            notifyAndTrackEvent(Event.RESUME_AD)
            adPlaybackState.isAdPaused = false
        }
    }

    companion object {
        /** call media progress provider after every 1 second **/
        const val MEDIA_PROGRESS_HANDLER_DELAY = 1000L // in ms

        /** call ad progress provider after every 250 ms **/
        const val AD_PROGRESS_HANDLER_DELAY = 250L // in ms
    }
}
