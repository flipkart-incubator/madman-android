/*
 *
 *  * Copyright (C) 2019 Flipkart Internet Pvt Ltd
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.flipkart.madman.manager.finder

import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.logger.LogUtil
import com.flipkart.madman.manager.helper.Constant

/**
 * Default implementation of [AdBreakFinder]
 *
 * Returns the playable ad break depending upon the current position of the player
 *
 * Algorithm:
 *
 * Using two pointers for the previous cue point index (which denotes the previous cue point) and the
 * next cue point index (which denotes the next cue point).
 *
 * Initially the previous cue point index is set to 0 and next cue point index to 0
 *
 * Using both the indexes, get the previousCuePoint and the nextCuePoint. If the current position of the player is
 * greater than the previous cue point and less than the next cue point (which means the position is b/w these two cue points)
 *
 * Ad break to be played will be the ad break at the next cue point if not already played.
 * If the previous cue point is not played, give priority to the previous ad break before playing the ad break at next cue point.
 *
 * Now, to identify if the scrubber position has been moved by the user.
 * If the current position of the player is less than the previous and next cue index, which means the scrubber was moved behind
 * If the current position of the player is more than the previous and next cue index, which means the scrubber was moved ahead.
 * In both the cases, using linear search find out the previous and next cue point index of the ad breaks.
 *
 * So, if the [scanForAdBreak] returns true which means the scrubber position has changed, calculate the new previous and next cue point index.
 *
 * The caller has to call the [findPlayableAdBreak] to fetch the next ad break
 */
class DefaultAdBreakFinder : AdBreakFinder {
    /**
     * Points to the previous cue point from the current position of media
     * Initialised to 0 by default
     */
    private var previousCuePointIndex: Int = 0

    /**
     * Points to the next cue point from the current position of media
     * Initialised to 0 by default
     */
    private var nextCuePointIndex: Int = 0

    /**
     * previous cue point using [previousCuePointIndex]
     */
    private var previousCuePoint: Float = -1F

    /**
     * next cue point using [nextCuePointIndex]
     */
    private var nextCuePoint: Float = -1F

    override fun findPlayableAdBreak(
        currentPosition: Float,
        contentStartPosition: Float,
        contentDuration: Float,
        adBreakList: List<AdBreak>
    ): AdBreak? {
        var adBreak: AdBreak? = null

        /**
         * If position has changed, do a linear search to find out previous and next cue point index, and update accordingly
         */
        if (scanForAdBreak(currentPosition, adBreakList)) {
            previousCuePointIndex = getAdBreakIndexForPosition(currentPosition, adBreakList)
            nextCuePointIndex =
                if (previousCuePointIndex < adBreakList.size - 1) previousCuePointIndex + 1 else Constant.INDEX_UNSET
            LogUtil.log("[DefaultAdBreakFinder] progress position changed $currentPosition, new previous point at $previousCuePointIndex, new next point at $nextCuePointIndex")
        }

        /**
         * get the previous cue point. If the index is not -1, get the ad break for that index, else 0
         */
        previousCuePoint =
            if (previousCuePointIndex != Constant.INDEX_UNSET) adBreakList[previousCuePointIndex].timeOffsetInSec else 0F

        /**
         * get the next cue point. If the index is -1, it means the next cue point has reached the end (no queue point ahead)
         * hence the next cue point is same as the content duration
         */
        nextCuePoint =
            if (nextCuePointIndex != Constant.INDEX_UNSET) adBreakList[nextCuePointIndex].timeOffsetInSec else contentDuration

        /**
         * Check if next ad break can be played ie only play if not already played
         */
        try {
            val nextAdBreak = adBreakList.first { it.timeOffsetInSec == nextCuePoint }
            if (nextAdBreak.state != AdBreak.AdBreakState.PLAYED && nextAdBreak.state != AdBreak.AdBreakState.SKIPPED) {
                LogUtil.log("[DefaultAdBreakFinder] next ad break ${nextAdBreak.timeOffsetInSec}")
                adBreak = nextAdBreak
            }
        } catch (e: NoSuchElementException) {
        }

        /**
         * Check if previous ad break can be played ie only play if not already played.
         * If yes, return the previous ad break
         */
        try {
            val previousAdBreak = adBreakList.first { it.timeOffsetInSec == previousCuePoint }
            if (previousAdBreak.state != AdBreak.AdBreakState.PLAYED && previousAdBreak.state != AdBreak.AdBreakState.SKIPPED) {
                LogUtil.log("[DefaultAdBreakFinder] playing previous ad break ${previousAdBreak.timeOffsetInSec}")
                adBreak = previousAdBreak
            }
        } catch (e: NoSuchElementException) {
        }

        return adBreak
    }

    override fun scanForAdBreak(
        currentPosition: Float,
        adBreakList: List<AdBreak>
    ): Boolean {
        return hasPositionChanged(
            previousCuePoint,
            nextCuePoint,
            currentPosition
        )
    }

    /**
     * Returns if scrubber position has changed.
     * ie If the current progress is less than the previous and next cue point, it means the scrubber was moved behind
     * ie If the current progress is more than the previous and next cue point, it means the scrubber was moved ahead
     */
    private fun hasPositionChanged(
        previousCuePoint: Float,
        nextCuePoint: Float,
        currentPosition: Float
    ): Boolean {
        return currentPosition > 0 && ((currentPosition < nextCuePoint && currentPosition < previousCuePoint) || (currentPosition > nextCuePoint && currentPosition > previousCuePoint))
    }

    /**
     * Returns the index of the ad group at or before position
     */
    private fun getAdBreakIndexForPosition(
        position: Float,
        adBreaks: List<AdBreak>
    ): Int {
        /**
         * Use a linear search as the array elements may not be increasing due to TIME_END_OF_SOURCE.
         * In practice we expect there to be few ad groups so the search shouldn't be expensive.
         */
        var index = adBreaks.size - 1
        while (index >= 0 && isPositionBeforeAdGroup(position, index, adBreaks)) {
            index--
        }
        return if (index >= 0) index else Constant.INDEX_UNSET
    }

    private fun isPositionBeforeAdGroup(
        position: Float,
        index: Int,
        adBreaks: List<AdBreak>
    ): Boolean {
        val adCuePoint = adBreaks[index].timeOffsetInSec
        return if (adCuePoint == Constant.INDEX_UNSET.toFloat()) {
            adCuePoint == Constant.INDEX_UNSET.toFloat() || position < adCuePoint
        } else {
            position < adCuePoint
        }
    }
}
