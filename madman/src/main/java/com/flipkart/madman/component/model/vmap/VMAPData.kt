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

class VMAPData {
    /** list of ad breaks **/
    var adBreaks: List<AdBreak>? = null

    /** version of the VMAP response **/
    var version: String? = null

    companion object {
        const val AD_BREAK_XML_TAG = "vmap:AdBreak"
        const val VMAP_XML_TAG = "vmap:VMAP"
        const val VERSION_XML_ATTR = "version"
    }
}
