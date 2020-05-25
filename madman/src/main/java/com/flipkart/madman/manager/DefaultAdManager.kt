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
import com.flipkart.madman.component.model.vast.VASTData
import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.logger.LogUtil
import com.flipkart.madman.manager.data.VastAdProvider
import com.flipkart.madman.manager.data.helper.AdDataHelper
import com.flipkart.madman.manager.event.Error
import com.flipkart.madman.manager.event.Event
import com.flipkart.madman.manager.handler.ProgressHandler
import com.flipkart.madman.manager.helper.Constant.FIRST_QUARTILE
import com.flipkart.madman.manager.helper.Constant.MIDPOINT
import com.flipkart.madman.manager.helper.Constant.THIRD_QUARTILE
import com.flipkart.madman.manager.model.VastAd
import com.flipkart.madman.manager.state.AdPlaybackState
import com.flipkart.madman.network.NetworkLayer
import com.flipkart.madman.parser.XmlParser
import com.flipkart.madman.provider.Progress
import com.flipkart.madman.renderer.AdRenderer
import com.flipkart.madman.renderer.callback.ViewClickListener
import com.flipkart.madman.validator.XmlValidator

/**
 * Core part of Madman
 */
open class DefaultAdManager(
    private val data: VMAPData,
    networkLayer: NetworkLayer,
    xmlParser: XmlParser,
    xmlValidator: XmlValidator,
    private val adRenderer: AdRenderer
) : BaseAdManager(data, adRenderer, networkLayer, xmlParser, xmlValidator), ViewClickListener {

    /** list of all cue points **/
    private val adCuePoints: List<Float> by lazy {
        AdDataHelper.getCuePoints(data)
    }

    /** previous ad progress **/
    private var previousAdProgress: Progress = Progress.UNDEFINED

    private var currentAd: VastAd? = null

    override fun getCuePoints(): List<Float> {
        return adCuePoints
    }

    override fun init() {
        adRenderer.registerViewClickListener(this)
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
        startAdMessageHandler()
    }

    override fun pause() {
        LogUtil.log("[AdManager] : pause")
        pauseAdIfPlaying()
    }

    override fun resume() {
        LogUtil.log("[AdManager] : resume")
        resumeAdIfPaused()
    }

    /**
     * Gets called every x seconds by [ProgressHandler]
     *
     * Provides the progress of the media running.
     * This is the place where the decision logic of which ad break to play happens
     */
    override fun onContentProgressUpdate(progress: Progress) {
        val currentTime = progress.currentTime
        val duration = progress.duration
        LogUtil.log("onContentProgressUpdate: progress: $currentTime, duration: $duration")

        /**
         * Update the playback state with the content duration if not set
         */
        adPlaybackState = adPlaybackState.withContentProgress(currentTime, duration)

        /**
         * Get the playable ad break from ad playback state depending on the current time of the media
         */
        adPlaybackState.findPlayableAdGroup(currentTime, duration, adBreakFinder)
        adPlaybackState.getAdGroup()?.getAdBreak()?.let {
            when (it.state) {
                /**
                 * If the ad break has not been played yet, and the cue point is within the prefetch range,
                 * fetch the ad and keep it ready
                 */
                AdBreak.AdBreakState.NOT_PLAYED -> {
                    if (canPreloadAdBreak(it, currentTime)) {
                        fetchAdBreak(it) {
                            loadAd()
                            notifyAndTrackEvent(Event.LOAD_AD)
                        }
                    }
                }
                /**
                 * If the ad break has been loaded which means we have the vast for the given ad
                 * send the play ad event as soon as current time is same as the cue point
                 */
                AdBreak.AdBreakState.LOADED -> {
                    if (canPlayAdBreak(it, currentTime, duration)) {
                        pauseContent()
                        playAd()
                    }
                }
                /**
                 * Do nothing for these states as of now
                 */
                else -> {
                }
            }
        }

        /** send message after 1 second **/
        progressHandler.sendDelayedMessageFor(
            MEDIA_PROGRESS_HANDLER_DELAY,
            ProgressHandler.MessageCode.CONTENT_MESSAGE
        )
    }

    /**
     * Gets called every x seconds by [ProgressHandler]
     *
     * Provides the progress of the ad running. Returns [Progress.UNDEFINED] till the ad starts playing
     */
    override fun onAdProgressUpdate(progress: Progress) {
        LogUtil.log("onAdProgressUpdate: progress: ${progress.currentTime}, duration: ${progress.duration}")

        val percentage = progress.currentTime / progress.duration
        val previousPercentage = previousAdProgress.currentTime / previousAdProgress.duration

        when {
            /**
             * if previous percentage is less than 0.25F and current percentage is greater than 0.25F, fire the FIRST_QUARTILE event
             */
            previousPercentage < FIRST_QUARTILE && percentage > FIRST_QUARTILE -> {
                notifyAndTrackEvent(Event.FIRST_QUARTILE)
            }
            /**
             * if previous percentage is less than 0.50F and current percentage is greater than 0.50F, fire the MIDPOINT event
             */
            previousPercentage < MIDPOINT && percentage > MIDPOINT -> {
                notifyAndTrackEvent(Event.MIDPOINT)
            }
            /**
             * if previous percentage is less than 0.75F and current percentage is greater than 0.75F, fire the THIRD_QUARTILE event
             */
            previousPercentage < THIRD_QUARTILE && percentage > THIRD_QUARTILE -> {
                notifyAndTrackEvent(Event.THIRD_QUARTILE)
            }
            /**
             * if previous percentage is less than current percentage, and progress is not [Progress.UNDEFINED], fire AD_PROGRESS event
             */
            previousPercentage <= percentage && progress != Progress.UNDEFINED -> {
                notifyAndTrackEvent(Event.AD_PROGRESS)
            }
            /**
             * if the ad state is [AdPlaybackState.AdState.STARTED], fire the AD_STARTED event
             */
            adPlaybackState.hasAdStarted() -> {
                notifyAndTrackEvent(Event.AD_STARTED)
                adPlaybackState.updateAdState(AdPlaybackState.AdState.PLAYING)
                currentAd?.let {
                    adRenderer.renderView(it.getAdElement())
                }
                if (adPlaybackState.getAdGroup()?.hasNextAdBreakInAdGroup() == true) {
                    notifyAndTrackEvent(Event.LOAD_AD)
                }
            }
            /**
             * if the ad state is [AdPlaybackState.AdState.ENDED] or [AdPlaybackState.AdState.SKIPPED],
             * stop the ad and load the next ad break in same ad group if present
             */
            adPlaybackState.hasAdEnded() || adPlaybackState.isAdSkipped() -> {
                val wasAdSkipped = adPlaybackState.isAdSkipped()
                updateAdState(AdPlaybackState.AdState.INIT)
                previousAdProgress = Progress.UNDEFINED

                adPlaybackState.getAdGroup()?.let {
                    updateAdBreakState(AdBreak.AdBreakState.PLAYED)
                    it.onAdBreakComplete()
                    notifyAndTrackEvent(Event.AD_STOPPED)
                    if (!wasAdSkipped) {
                        notifyAndTrackEvent(Event.AD_COMPLETED)
                    }
                    removeAdMessageHandler()

                    if (it.hasMoreAdBreaksInAdGroup()) {
                        /** play the next ad break for same cue point **/
                        fetchAdBreak(it.getAdBreak()) {
                            loadAd()
                            playAd()
                        }
                    } else {
                        /** no ad break for this ad group, resume content **/
                        adPlaybackState.onAdGroupComplete()
                        if (onContentCompleted()) {
                            return
                        }
                        resumeContent()
                        startContentHandler()
                        return
                    }
                }
            }
        }

        /** store the percentage **/
        previousAdProgress = progress

        /** send message after "x" ms **/
        progressHandler.sendDelayedMessageFor(
            AD_PROGRESS_HANDLER_DELAY,
            ProgressHandler.MessageCode.AD_MESSAGE
        )
    }

    /**
     * Ad play callback. This is called when the player's playAd is called.
     * When playAd is called, the plugin / client sends the onPlay callback which notifies the
     * sdk to start the ad playback.
     */
    override fun onAdPlayCallback() {
        updateAdState(AdPlaybackState.AdState.STARTED)
        removeContentHandler()
        adRenderer.createView()
    }

    /**
     * Ad ended callback from the player.
     * Remove the ad message handlers and update the state
     */
    override fun onAdEndedCallback(skipped: Boolean) {
        if (skipped) {
            updateAdState(AdPlaybackState.AdState.SKIPPED)
        } else {
            updateAdState(AdPlaybackState.AdState.ENDED)
        }
        adRenderer.removeView()
    }

    /**
     * Ad error callback from the player
     */
    override fun onAdErrorCallback() {
        notifyAndTrackError(AdErrorType.AD_ERROR)
        onAdEndedCallback(false)
    }

    /**
     * Ad pause callback from the player
     */
    override fun onAdPauseCallback() {
        notifyAndTrackEvent(Event.AD_PAUSED)
    }

    /**
     * Ad resume callback from the player
     */
    override fun onAdResumeCallback() {
        notifyAndTrackEvent(Event.AD_RESUMED)
    }

    /**
     * Called when the user clicks on skip ad
     */
    override fun onSkipAdClick() {
        notifyAndTrackEvent(Event.AD_SKIPPED)
        onAdEndedCallback(true)
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
        adPlaybackState.contentCompleted()
        onContentCompleted()
    }

    override fun destroy() {
        super.destroy()
        adRenderer.unregisterViewClickListener(this)
        adRenderer.destroy()
    }

    /**
     * if the content is completed or the post roll is played,
     * fire all ads completed event and remove all the handlers.
     */
    private fun onContentCompleted(): Boolean {
        if (adPlaybackState.hasContentCompleted() && adPlaybackState.isPostRollPlayed()) {
            removeContentHandler()
            removeAdMessageHandler()
            notifyAndTrackEvent(Event.ALL_AD_COMPLETED)
            return true
        }
        return false
    }

    private fun fetchAdBreak(adBreak: AdBreak, onSuccess: () -> (Unit)) {
        vastAdProvider.getVASTAd(adBreak, object : VastAdProvider.Listener {
            /**
             * Called on successful fetch of [VASTData]
             * fire the ad loaded event
             */
            override fun onVastFetchSuccess(vastData: VASTData) {
                updateAdBreakState(AdBreak.AdBreakState.LOADED)
                currentAd = adPlaybackState.getAdGroup()?.getVastAd(vastData)

                if (currentAd?.getAdMediaUrls()?.isNotEmpty() == true) {
                    onSuccess()
                } else {
                    notifyAndTrackError(
                        AdErrorType.NO_MEDIA_URL,
                        Error.NO_MEDIA_FILE_ERROR.errorMessage
                    )
                }
            }

            /**
             * Called on vast fetch error, may be due to network error or no ad present in the vast
             * throw error with the message
             */
            override fun onVastFetchError(errorType: AdErrorType, message: String) {
                updateAdBreakState(AdBreak.AdBreakState.ERROR)
                notifyAndTrackError(errorType, message)
            }
        })
    }

    private fun loadAd() {
        notifyAndTrackEvent(Event.AD_LOADED)
    }

    private fun playAd() {
        notifyAndTrackEvent(Event.PLAY_AD)
    }

    private fun pauseContent() {
        notifyAndTrackEvent(Event.CONTENT_PAUSE)
    }

    private fun resumeContent() {
        notifyAndTrackEvent(Event.CONTENT_RESUME)
    }

    private fun updateAdBreakState(state: AdBreak.AdBreakState) {
        adPlaybackState.getAdGroup()?.updateAdBreakState(state)
    }

    private fun updateAdState(state: AdPlaybackState.AdState) {
        adPlaybackState.updateAdState(state)
    }

    /**
     * check if the given ad break be preloaded
     */
    private fun canPreloadAdBreak(adBreak: AdBreak, currentTime: Float): Boolean {
        return adBreak.timeOffsetInSec - currentTime <= adRenderer.getRenderingSettings().getPreloadTime() && adBreak.timeOffsetInSec != -1f
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
        if (adPlaybackState.isAdPlaying()) {
            removeAdMessageHandler()
            notifyAndTrackEvent(Event.PAUSE_AD)
            updateAdState(AdPlaybackState.AdState.PAUSED)
        }
    }

    /**
     * resumed the ad if paused
     */
    private fun resumeAdIfPaused() {
        if (adPlaybackState.isAdPaused()) {
            startAdMessageHandler()
            notifyAndTrackEvent(Event.RESUME_AD)
            updateAdState(AdPlaybackState.AdState.PLAYING)
        }
    }

    /**
     * notify all the registered event handlers for the given event
     */
    private fun notifyAndTrackEvent(event: Event) {
        playerAdEventHelper.handleEvent(event, currentAd)
        trackingEventHelper.handleEvent(event, currentAd)
    }

    /**
     * notify all the registered event handlers for the given event
     */
    private fun notifyAndTrackError(errorCode: AdErrorType, errorMessage: String? = "") {
        playerAdEventHelper.handleError(errorCode, errorMessage)
        trackingEventHelper.handleError(errorCode, currentAd)
    }

    companion object {
        /** call media progress provider after every 1 second **/
        const val MEDIA_PROGRESS_HANDLER_DELAY = 200L // in ms

        /** call ad progress provider after every 250 ms **/
        const val AD_PROGRESS_HANDLER_DELAY = 200L // in ms
    }
}
