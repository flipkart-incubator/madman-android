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
package com.flipkart.madman.manager.data.providers

import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.component.model.vmap.AdSource
import com.flipkart.madman.component.model.vmap.AdTagURI
import com.flipkart.madman.loader.AdLoader
import com.flipkart.madman.manager.data.VastAdProvider
import com.flipkart.madman.manager.event.Error
import com.flipkart.madman.network.model.NetworkAdRequest

/**
 * Implementation of [VastAdProvider] which interacts with the ad loader and network layer to fetch the vast ads.
 *
 * It reads the url from [AdBreak] -> [AdSource] -> [AdTagURI]
 */
class NetworkVastAdProvider(private val adLoader: AdLoader<NetworkAdRequest>) :
    VastAdProvider {
    override fun getVASTAd(adBreak: AdBreak, listener: VastAdProvider.Listener) {
        adBreak.adSource?.adTagURI?.let {
            val adUrl = it.url
            adUrl?.let {
                adLoader.requestAds(NetworkAdRequest(adUrl), { vmap ->
                    /**
                     * The vast data gets wrapped in the VMAP model with one ad break
                     */
                    val firstAdBreak = vmap.adBreaks?.get(0)
                    firstAdBreak?.adSource?.vastAdData?.let { vast ->
                        /** throw error if vast has no ads to play **/
                        if (vast.ads?.isNotEmpty() == true) {
                            listener.onVastFetchSuccess(vast)
                        } else {
                            listener.onVastFetchError(
                                AdErrorType.VAST_ERROR,
                                "no ad from the given $adUrl"
                            )
                        }
                    } ?: run {
                        listener.onVastFetchError(
                            AdErrorType.VAST_ERROR,
                            "no vast from the given $adUrl"
                        )
                    }
                }, { _: AdErrorType, message: String? ->
                    listener.onVastFetchError(
                        AdErrorType.VAST_ERROR,
                        message ?: Error.UNKNOWN_ERROR.errorMessage
                    )
                })
            } ?: run {
                listener.onVastFetchError(
                    AdErrorType.VAST_ERROR,
                    "no url to fetch ads for $adBreak"
                )
            }
        } ?: run {
            listener.onVastFetchError(AdErrorType.VAST_ERROR, "no AdTagURI for $adBreak")
        }
    }
}
