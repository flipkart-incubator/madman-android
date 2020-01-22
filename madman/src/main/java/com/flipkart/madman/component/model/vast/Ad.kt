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

package com.flipkart.madman.component.model.vast

class Ad {
    /** id of the ad **/
    var id: String? = null

    /** sequence of the ad **/
    var sequence: String? = null

    /** inline ad **/
    var inLine: InLine? = null

    companion object {
        const val INLINE_XML_TAG = "InLine"
        const val ID_XML_ATTR = "id"
        const val SEQUENCE_XML_ATTR = "sequence"
    }
}
