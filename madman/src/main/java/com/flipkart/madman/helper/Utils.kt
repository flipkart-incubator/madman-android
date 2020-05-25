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
package com.flipkart.madman.helper

import com.flipkart.madman.component.model.vmap.AdBreak
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    /**
     * Convert the ad time offset in string to float
     * For eg
     *
     * start -> 0
     * end -> -1
     * "hh:mm:ss" -> value in seconds
     */
    @Throws(IOException::class)
    fun convertAdTimeOffsetToSeconds(value: String?): Float {
        return when {
            value == AdBreak.TimeOffsetTypes.START -> 0F
            value == "0%" -> 0F
            value?.matches("\\d+:\\d{2}:\\d{2}(.\\d+)?".toRegex()) == true -> convertDateFormatToSeconds(
                value
            ).toFloat()
            else -> Float.MAX_VALUE
        }
    }

    /**
     * Convert a simple date time with the format "HH:mm:ss.SSS" or "HH:mm:ss" to seconds.
     *
     * @param timeToConvert Time to convert. Expecting format of "HH:mm:ss.SSS" or "HH:mm:ss".
     * @return The converted time in seconds.
     */
    @Throws(IOException::class)
    fun convertDateFormatToSeconds(timeToConvert: String?): Double {
        if (timeToConvert == null) {
            return -1.0
        }

        try {
            var pattern = "1970-01-01 00:00:00.000"

            val sdf: SimpleDateFormat = when {
                timeToConvert.matches("\\d+:\\d{2}:\\d{2}.\\d{3}".toRegex()) -> SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss.SSS",
                    Locale.US
                )
                timeToConvert.matches("\\d+:\\d{2}:\\d{2}.\\d{2}".toRegex()) -> {
                    pattern = "1970-01-01 00:00:00.00"
                    SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss.SS",
                        Locale.US
                    )
                }
                timeToConvert.matches("\\d+:\\d{2}:\\d{2}.\\d{1}".toRegex()) -> {
                    pattern = "1970-01-01 00:00:00.0"
                    SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss.S",
                        Locale.US
                    )
                }
                timeToConvert.matches("\\d+:\\d{2}:\\d{2}".toRegex()) -> SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.US
                )
                else -> throw IOException("Time format does not match expected.")
            }

            sdf.parse(pattern)?.let {
                val time = (it.time / 1000).toDouble()
                sdf.parse("1970-01-01 $timeToConvert")?.let { date ->
                    return date.time / 1000 - time
                }
            }
            return -1.0
        } catch (e: ParseException) {
            throw IOException(e.message)
        }
    }

    fun formatSecondsToMMSS(seconds: Int): String {
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return "%02d:%02d".format(minutes, remainingSeconds)
    }
}
