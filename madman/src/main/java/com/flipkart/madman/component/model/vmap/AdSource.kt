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

package com.flipkart.madman.component.model.vmap

import com.flipkart.madman.component.model.vast.VASTData

class AdSource {
    /** id of the ad source **/
    var id: String? = null

    /** flag indicates whether multiple ads should be played or not. Default value is true as per specifications **/
    var allowMultipleAds: Boolean = true

    /** indicates whether the video player should honor redirects within an ad response **/
    var followRedirects: Boolean? = null

    /** contains the url to the ad server for fetching ads response **/
    var adTagURI: AdTagURI? = null

    /** indicates that a VAST ad response is embedded within the VMAP response **/
    var vastAdData: VASTData? = null

    /** arbitrary string data that represents non-VAST ad response **/
    var customAdData: CustomAdData? = null

    companion object {
        const val MULTIPLE_ADS_XML_ATTR = "allowMultipleAds"
        const val FOLLOW_REDIRECT_XML_ATTR = "followRedirects"
        const val ID_XML_ATTR = "id"
        const val AD_TAG_URI_XML_TAG_V1 = "AdTagURI"
        const val AD_TAG_URI_XML_TAG_V2 = "vmap:AdTagURI"
        const val VAST_DATA_XML_TAG_V1 = "VASTData"
        const val VAST_DATA_XML_TAG_V2 = "vmap:VASTAdData"
    }
}
