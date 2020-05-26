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
package com.flipkart.madman.component.model.common

class Tracking {
    /** url to hit to track **/
    var url: String? = null

    /** event type eg breakStart, breakEnd, error **/
    var event: String? = null

    /**
     * for cases such as progress event, the offset indicates the time value at which the event should
     * be triggered. Eg HH:MM:SS or HH:MM:SS.mmm or n%
     */
    var offset: Float? = null

    companion object {
        const val EVENT_XML_ATTR = "event"
        const val OFFSET_XML_ATTR = "offset"
    }

    enum class TrackingEvent {
        START,
        FIRST_QUARTILE,
        MIDPOINT,
        THIRD_QUARTILE,
        COMPLETE,
        SKIP,
        PROGRESS,
        FULLSCREEN,
        EXIT_FULLSCREEN,
        MUTE,
        UNMUTE,
        PAUSE,
        REWIND,
        RESUME,
        EXPAND,
        COLLAPSE,
        CLOSE_LINEAR,
        BREAK_START,
        BREAK_END,
        ERROR,

        CLICK_THROUGH,
        IMPRESSION;

        companion object {
            fun getType(value: String): TrackingEvent? {
                when (value) {
                    "start" -> return START
                    "firstQuartile" -> return FIRST_QUARTILE
                    "midpoint" -> return MIDPOINT
                    "thirdQuartile" -> return THIRD_QUARTILE
                    "complete" -> return COMPLETE
                    "skip" -> return SKIP
                    "progress" -> return PROGRESS
                    "fullscreen" -> return FULLSCREEN
                    "exitFullscreen" -> return EXIT_FULLSCREEN
                    "mute" -> return MUTE
                    "unmute" -> return UNMUTE
                    "pause" -> return PAUSE
                    "rewind" -> return REWIND
                    "resume" -> return RESUME
                    "expand" -> return EXPAND
                    "collapse" -> return COLLAPSE
                    "closeLinear" -> return CLOSE_LINEAR
                    "breakStart" -> return BREAK_START
                    "breakEnd" -> return BREAK_END
                    "error" -> return ERROR
                }
                return null
            }
        }
    }
}
