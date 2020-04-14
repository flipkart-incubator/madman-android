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
package com.flipkart.madman.component.model.vmap

import androidx.annotation.StringDef
import com.flipkart.madman.component.model.common.Tracking
import com.flipkart.madman.component.model.vmap.AdBreak.BreakTypes.Companion.DISPLAY
import com.flipkart.madman.component.model.vmap.AdBreak.BreakTypes.Companion.LINEAR
import com.flipkart.madman.component.model.vmap.AdBreak.BreakTypes.Companion.NON_LINEAR
import com.flipkart.madman.component.model.vmap.AdBreak.TimeOffsetTypes.Companion.END
import com.flipkart.madman.component.model.vmap.AdBreak.TimeOffsetTypes.Companion.START

class AdBreak {
    /** represents the time offset. Eg: start, end, 00:00:15.125, 40%, #1 **/
    var timeOffset: String? = null

    /** converted value of timeOffset in seconds **/
    var timeOffsetInSec: Float = 0F

    /** break type eg linear, nonlinear **/
    @BreakTypes
    var breakType: String? = null

    /** break id **/
    var breakId: String? = null

    /** repeat same ad break using the same ad source. Format is hh:mm:ss **/
    var repeatAfter: String? = null

    /** the ad source **/
    var adSource: AdSource? = null

    /** list of tracking events **/
    var trackingEvents: List<Tracking>? = null

    /** parsed event to tracking url map **/
    var eventToTrackingUrlsMap: Map<Tracking.TrackingEvent, List<String>>? = null

    /** can be used to express additional information not supported in the VMAP specification **/
    var extensions: List<Extensions>? = null

    /** represents the state of the ad break **/
    var state: AdBreakState = AdBreakState.NOT_PLAYED

    /** group index **/
    var podIndex: Int = 0

    companion object {
        const val BREAK_TYPE_XML_ATTR = "breakType"
        const val BREAK_ID_XML_ATTR = "breakId"
        const val REPEAT_AFTER_XML_ATTR = "repeatAfter"
        const val TIME_OFFSET_XML_ATTR = "timeOffset"
        const val AD_SOURCE_XML_TAG = "vmap:AdSource"
        const val TRACKING_XML_TAG = "vmap:Tracking"
        const val TRACKING_EVENT_XML_TAG = "vmap:TrackingEvents"
    }

    @StringDef(LINEAR, NON_LINEAR, DISPLAY)
    annotation class BreakTypes {
        companion object {
            // different break types
            const val LINEAR = "linear"
            const val NON_LINEAR = "nonlinear"
            const val DISPLAY = "display"
        }
    }

    @StringDef(START, END)
    annotation class TimeOffsetTypes {
        companion object {
            const val START = "start"
            const val END = "end"
        }
    }

    /**
     * represents the state for [AdBreak]
     */
    enum class AdBreakState {
        NOT_PLAYED, LOADED, PLAYED, ERROR
    }
}
