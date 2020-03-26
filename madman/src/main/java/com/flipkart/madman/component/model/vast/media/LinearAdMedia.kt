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
package com.flipkart.madman.component.model.vast.media

import com.flipkart.madman.component.model.vast.MediaFile
import com.flipkart.madman.component.model.vast.VideoClicks

class LinearAdMedia : BaseAdMedia() {
    /** duration of the ad **/
    var duration: String? = null

    /** duration of the ad in seconds **/
    var durationInSeconds: Double = 0.0

    /** skip offset, either HH:MM:SS.mmm or % **/
    var skipOffset: String? = null

    /** skip offset in seconds **/
    var skipOffsetInSeconds: Double = -1.0

    /** video click events **/
    var videoClicks: VideoClicks? = null

    /** list of media files **/
    var mediaFiles: List<MediaFile>? = null

    companion object {
        const val DURATION_XML_TAG = "Duration"
        const val SKIP_OFFSET_XML_TAG = "skipoffset"
        const val TRACKING_EVENTS_XML_TAG = "TrackingEvents"
        const val TRACKING_XML_TAG = "Tracking"
        const val MEDIA_FILES_XML_TAG = "MediaFiles"
        const val VIDEO_CLICKS_XML_TAG = "VideoClicks"
    }
}
