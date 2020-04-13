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
package com.flipkart.madman.manager.state

import com.flipkart.madman.component.model.vast.VASTData
import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.logger.LogUtil
import com.flipkart.madman.manager.data.helper.AdDataHelper
import com.flipkart.madman.manager.finder.AdBreakFinder
import com.flipkart.madman.manager.model.VastAd

/**
 * Maintains the state of the ad playback
 */
class AdState(private val adBreaks: List<AdBreak>) {
    /** indicates that the duration of content is set or not **/
    private var durationSet: Boolean = false
    /** indicates the start position of the content, default is 0, non 0 value for continue watching cases **/
    private var contentStartPosition: Float = 0F
    /** if the content has completed **/
    var contentCompleted: Boolean = false

    /** if the ad is playing **/
    var isAdPlaying: Boolean = false
    /** if the ad has been paused (if was playing before) **/
    var isAdPaused: Boolean = false
    /** if the ad is skipped **/
    var isAdSkipped: Boolean = false

    var playableAdBreaks: List<AdBreak>? = null
    var currentAdBreakIndex: Int = 0
    var currentAdBreak: AdBreak? = null
    var currentAd: VastAd? = null

    /**
     * Initialise the [AdState] with the total duration of the media.
     * The duration is used to update the time offset for all the post roll ad breaks
     *
     * Initially the time offset for post roll is -1
     */
    fun withContentProgress(currentTime: Float, duration: Float): AdState {
        if (duration > 0 && !durationSet) {
            contentStartPosition = currentTime
            adBreaks.forEach {
                if (it.timeOffset == AdBreak.TimeOffsetTypes.END) {
                    it.timeOffsetInSec = duration
                }
            }
            durationSet = true
        }
        return this
    }

    /**
     * This is called in a handler to fetch the next playable ad break.
     */
    fun findPlayableAdBreaks(
        currentPosition: Float,
        duration: Float,
        adBreakFinder: AdBreakFinder
    ): AdBreak? {
        /**
         * If the ad break is null, or the scan for ad break returns true (ie the position had moved(
         * and the ad is not playing
         */
        val scanForAdBreak = adBreakFinder.scanForAdBreak(currentPosition, adBreaks)
        if (playableAdBreaks == null || scanForAdBreak) {
            playableAdBreaks = adBreakFinder.findPlayableAdBreaks(
                currentPosition,
                contentStartPosition,
                duration,
                adBreaks
            )
        }

        return getPlayableAdBreak()
    }

    /**
     * Get the [AdBreak] to play
     */
    fun getPlayableAdBreak(): AdBreak? {
        currentAdBreak = playableAdBreaks?.get(currentAdBreakIndex)
        return currentAdBreak
    }

    fun getVastAd(): VastAd? {
        if (currentAd == null) {
            currentAd = AdDataHelper.createAdFor(
                playableAdBreaks,
                currentAdBreakIndex
            )
        }
        return currentAd
    }

    fun isPostRollPlayed(): Boolean {
        return adBreaks.find { it.timeOffset == AdBreak.TimeOffsetTypes.END && it.state != AdBreak.AdBreakState.PLAYED } == null
    }

    /**
     * update the state of the current ad break
     */
    fun onAdBreakStateChange(state: AdBreak.AdBreakState) {
        currentAdBreak?.state = state
    }

    /**
     * called when the ad is completed either ended, or skipped
     */
    fun onAdComplete() {
        reset()
        if (hasMoreAdBreakForSameCuePoint()) {
            moveToNextAdBreakForSameCuePoint()
        } else {
            currentAdBreakIndex = 0
            playableAdBreaks = null
        }
    }

    /**
     * mark content completed
     */
    fun markContentCompleted() {
        contentCompleted = true
    }

    fun updateVastDataForCurrentAdBreak(vastData: VASTData) {
        currentAdBreak?.adSource?.vastAdData = vastData
    }

    private fun moveToNextAdBreakForSameCuePoint() {
        currentAdBreakIndex += 1
        currentAdBreak = playableAdBreaks?.get(currentAdBreakIndex)
        LogUtil.log("onAdBreakComplete: next ad break index is $currentAdBreakIndex")
    }

    fun hasMoreAdBreakForSameCuePoint(): Boolean {
        val totalPlayableAdBreaks = playableAdBreaks?.size ?: 0
        return (currentAdBreakIndex < totalPlayableAdBreaks - 1)
    }

    private fun reset() {
        currentAd = null
        currentAdBreak = null
    }
}
