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

class InLine {
    /** the ad system **/
    var adSystem: String? = null

    /** title of the ad **/
    var adTitle: String? = null

    /** description of the ad **/
    var description: String? = null

    /** error urls to hit in-case of ad error **/
    var errorUrls: List<String>? = null

    /** impression urls to send ad impressions **/
    var impressionUrls: List<String>? = null

    /** list of creatives in the ad **/
    var creatives: List<Creative>? = null

    companion object {
        const val CREATIVES_XML_TAG = "Creatives"
        const val AD_SYSTEM_XML_TAG = "AdSystem"
        const val AD_TITLE_XML_TAG = "AdTitle"
        const val DESCRIPTION_XML_TAG = "Description"
        const val ERROR_XML_TAG = "Error"
        const val IMPRESSION_XML_TAG = "Impression"
    }
}
