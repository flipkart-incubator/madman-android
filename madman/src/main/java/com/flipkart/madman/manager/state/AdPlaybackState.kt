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
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.manager.data.helper.AdDataHelper
import com.flipkart.madman.manager.finder.AdBreakFinder
import com.flipkart.madman.manager.model.VastAd

/**
 * Maintains the state of the ad playback
 */
class AdPlaybackState {

    private var durationSet: Boolean = false

    private var startPosition: Float = 0F

    /** list of all ad breaks **/
    private var adBreakList: List<AdBreak> = emptyList()

    /** current ad break to play **/
    private var adBreakToPlay: AdBreak? = null

    /** current ad to play from the ad break **/
    private var adToPlay: VastAd? = null

    /** if the ad is playing **/
    var isAdPlaying: Boolean = false

    /** if the ad has been paused (if was playing before) **/
    var isAdPaused: Boolean = false

    /** if the content has completed **/
    var contentCompleted: Boolean = false

    /**
     * Initialise the [AdPlaybackState] with the [VMAPData]
     */
    fun withData(data: VMAPData): AdPlaybackState {
        adBreakList = data.adBreaks ?: emptyList()
        return this
    }

    /**
     * Initialise the [AdPlaybackState] with the total duration of the media.
     * The duration is used to update the time offset for all the post roll ad breaks
     *
     * Initially the time offset for post roll is -1
     */
    fun withContentProgress(currentTime: Float, duration: Float): AdPlaybackState {
        if (duration > 0 && !durationSet) {
            startPosition = currentTime
            adBreakList.forEach {
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
    fun getPlayableAdBreak(
        currentPosition: Float,
        duration: Float,
        adBreakFinder: AdBreakFinder
    ): AdBreak? {
        /**
         * If the ad break is null, or the scan for ad break returns true (ie the position had moved(
         * and the ad is not playing
         */
        if ((adBreakToPlay == null || adBreakFinder.scanForAdBreak(currentPosition, adBreakList))
            && !isAdPlaying
        ) {
            /**
             * get the playable ad break from [AdBreakFinder]
             */
            adBreakToPlay =
                adBreakFinder.findPlayableAdBreak(
                    currentPosition,
                    startPosition,
                    duration,
                    adBreakList
                )
        }
        return adBreakToPlay
    }

    fun isPostRollPlayed(): Boolean {
        return adBreakList.find { it.timeOffset == AdBreak.TimeOffsetTypes.END && it.state != AdBreak.AdBreakState.PLAYED && it.state != AdBreak.AdBreakState.SKIPPED } == null
    }

    /**
     * update the state of the current ad break
     */
    fun updateStateForAdBreak(state: AdBreak.AdBreakState) {
        adBreakToPlay?.state = state
    }

    /**
     * once the ad break is completed, reset the states
     */
    fun markAdBreakAsCompleted() {
        adBreakToPlay = null
        adToPlay = null
        isAdPlaying = false
    }

    /**
     * once the ad break is completed, reset the states
     */
    fun markAdBreakAsPlaying() {
        isAdPlaying = true
    }

    /**
     * mark content completed
     */
    fun markContentCompleted() {
        contentCompleted = true
    }

    fun getAdFrom(vastData: VASTData): VastAd? {
        adToPlay = adBreakToPlay?.let {
            return AdDataHelper.createAdFor(
                it,
                vastData,
                it.podIndex
            )
        }
        return adToPlay
    }
}
