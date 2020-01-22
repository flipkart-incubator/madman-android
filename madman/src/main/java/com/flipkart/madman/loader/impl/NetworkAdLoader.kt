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

package com.flipkart.madman.loader.impl

import android.os.CancellationSignal
import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.network.NetworkLayer
import com.flipkart.madman.network.NetworkListener
import com.flipkart.madman.network.model.NetworkAdRequest
import com.flipkart.madman.parser.XmlParser
import com.flipkart.madman.validator.XmlValidator

/**
 * The [NetworkAdLoader] expects a network url.
 *
 * The loader interacts with the [NetworkLayer] to fetch the xml string, and delegates the response to the parser.
 */
class NetworkAdLoader(
    private val networkLayer: NetworkLayer,
    parser: XmlParser,
    xmlValidator: XmlValidator
) : BaseAdLoader<NetworkAdRequest>(parser, xmlValidator) {

    override fun requestAds(
        param: NetworkAdRequest,
        onSuccess: (data: VMAPData) -> Unit,
        onFailure: (errorType: AdErrorType, message: String?) -> Unit
    ) {
        networkLayer.fetch(param, object : NetworkListener<String> {
            override fun onSuccess(statusCode: Int, result: String?) {
                result?.let {
                    /** response exists, delegate to parser **/
                    parseResponse(it, onSuccess, onFailure)
                } ?: run {
                    /** response is null, throw error **/
                    onFailure(
                        AdErrorType.EMPTY_VMAP_RESPONSE,
                        "Empty vmap response for ${param.url}"
                    )
                }
            }

            override fun onError(errorCode: Int, message: String) {
                /** network error, throw error **/
                onFailure(AdErrorType.AD_REQUEST_NETWORK_FAILURE, message)
            }
        }, CancellationSignal())
    }
}
