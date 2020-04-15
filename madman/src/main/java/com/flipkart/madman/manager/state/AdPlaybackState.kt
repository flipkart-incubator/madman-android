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
import com.flipkart.madman.manager.data.helper.AdDataHelper
import com.flipkart.madman.manager.finder.AdBreakFinder
import com.flipkart.madman.manager.model.VastAd
import java.util.*

/**
 * Maintains the state of the ad playback
 */
class AdPlaybackState(private val adBreaks: List<AdBreak>) {
    /** indicates that the duration of content is set or not **/
    private var durationSet: Boolean = false
    /** indicates the start position of the content, default is 0, non 0 value for continue watching cases **/
    private var contentStartPosition: Float = 0F
    /** indicates if the content has completed **/
    private var contentCompleted: Boolean = false
    /** currently playing ad group **/
    private var playableAdGroup: AdGroup? = null
    /** indicates the ad state **/
    private var adState: AdState = AdState.INIT

    enum class AdState {
        INIT, STARTED, PLAYING, PAUSED, ENDED;
    }

    inner class AdGroup(
        private val adBreaksInGroup: List<AdBreak>,
        private val adBreakQueue: Queue<AdBreak> = ArrayDeque(adBreaksInGroup),
        private val count: Int = adBreaksInGroup.size
    ) {
        fun getAdBreak(): AdBreak {
            return adBreakQueue.element()
        }

        fun getVastAd(vastData: VASTData): VastAd? {
            val adBreak = getAdBreak()
            adBreak.adSource?.vastAdData = vastData
            return AdDataHelper.createAdFor(
                adBreak,
                adBreaksInGroup.indexOf(adBreak),
                count
            )
        }

        fun updateAdBreakState(state: AdBreak.AdBreakState) {
            getAdBreak().state = state
            adBreaks[adBreaks.indexOf(getAdBreak())].state = state
        }

        fun onAdBreakComplete() {
            try {
                adBreakQueue.remove()
            } catch (e: NoSuchElementException) {
            }
        }

        fun hasMoreAdBreaksInAdGroup(): Boolean {
            return adBreakQueue.isNotEmpty()
        }

        fun hasNextAdBreakInAdGroup(): Boolean {
            return adBreakQueue.size - 1 > 0
        }
    }

    /**
     * Initialise the [AdPlaybackState] with the total duration of the media.
     * The duration is used to update the time offset for all the post roll ad breaks
     *
     * Initially the time offset for post roll is -1
     */
    fun withContentProgress(currentTime: Float, duration: Float): AdPlaybackState {
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
    fun findPlayableAdGroup(
        currentPosition: Float,
        duration: Float,
        adBreakFinder: AdBreakFinder
    ) {
        /**
         * If the ad break is null, or the scan for ad break returns true (ie the position had moved(
         * and the ad is not playing
         */
        val scanForAdBreak = adBreakFinder.scanForAdBreak(currentPosition)
        if (playableAdGroup == null || scanForAdBreak) {
            val playableAdBreaks = adBreakFinder.findPlayableAdBreaks(
                currentPosition,
                contentStartPosition,
                duration,
                adBreaks
            )
            playableAdGroup = if (playableAdBreaks.isNotEmpty()) {
                AdGroup(playableAdBreaks)
            } else {
                null
            }
        }
    }

    fun getAdGroup(): AdGroup? {
        return playableAdGroup
    }

    fun onAdGroupComplete() {
        playableAdGroup = null
    }

    fun isPostRollPlayed(): Boolean {
        return adBreaks.find { it.timeOffset == AdBreak.TimeOffsetTypes.END && it.state != AdBreak.AdBreakState.PLAYED } == null
    }

    /**
     * called when the content has ended. The plugin notifies when the content has ended
     */
    fun contentCompleted() {
        contentCompleted = true
    }

    /**
     * if the content is completed
     */
    fun hasContentCompleted(): Boolean {
        return contentCompleted
    }

    fun isAdPlaying(): Boolean {
        return adState == AdState.PLAYING
    }

    fun isAdPaused(): Boolean {
        return adState == AdState.PAUSED
    }

    fun hasAdEnded(): Boolean {
        return adState == AdState.ENDED
    }

    fun hasAdStarted(): Boolean {
        return adState == AdState.STARTED
    }

    fun updateAdState(state: AdState) {
        adState = state
    }
}
