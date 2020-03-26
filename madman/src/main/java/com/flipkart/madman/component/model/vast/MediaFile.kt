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
package com.flipkart.madman.component.model.vast

import androidx.annotation.StringDef
import com.flipkart.madman.component.model.vast.MediaFile.MimeTypes.Companion.FLASH
import com.flipkart.madman.component.model.vast.MediaFile.MimeTypes.Companion.MP4

class MediaFile {
    /** id of the media file **/
    var id: String? = null

    /** delivery protocol **/
    var delivery: String? = null

    /** url of the media **/
    var url: String? = null

    /** mime type of the file **/
    @MimeTypes
    var type: String? = null

    /** the bitrate  **/
    var bitrate: Long? = null

    /** width of the media **/
    var width: Int? = null

    /** height of the media **/
    var height: Int? = null

    /** indicates if media is scalable **/
    var scalable: Boolean? = null

    /** maintain the aspect ratio of the media **/
    var maintainAspectRatio: Boolean? = null

    companion object {
        const val MEDIA_FILE_XML_TAG = "MediaFile"
        const val ID_XML_ATTR = "id"
        const val DELIVERY_XML_ATTR = "delivery"
        const val TYPE_XML_ATTR = "type"
        const val BITRATE_XML_ATTR = "bitrate"
        const val WIDTH_XML_ATTR = "width"
        const val HEIGHT_XML_ATTR = "height"
        const val SCALABLE_XML_ATTR = "scalable"
        const val ASPECT_RATIO_XML_ATTR = "maintainAspectRatio"
    }

    @StringDef(MP4, FLASH)
    annotation class MimeTypes {
        companion object {
            const val MP4 = "video/mp4"
            const val FLASH = "video/xflv"
        }
    }
}
