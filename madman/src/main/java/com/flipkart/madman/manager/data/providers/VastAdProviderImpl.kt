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

package com.flipkart.madman.manager.data.providers

import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.manager.data.VastAdProvider

/**
 * It delegates the call to the correct [VastAdProvider]
 */
class VastAdProviderImpl(
    private val stringVastAdProvider: StringVastAdProvider,
    private val networkVastAdProvider: NetworkVastAdProvider
) : VastAdProvider {
    override fun getVASTAd(adBreak: AdBreak, listener: VastAdProvider.Listener) {
        adBreak.adSource?.let {
            when {
                it.adTagURI != null ->
                    /** if ad tag uri is present, delegate to network vast ad provider **/
                    networkVastAdProvider.getVASTAd(adBreak, listener)
                it.vastAdData != null ->
                    /** if ad tag uri is present, delegate to string vast ad provider **/
                    stringVastAdProvider.getVASTAd(adBreak, listener)
                else ->
                    /** throw error as ad tag or vast is null **/
                    listener.onVastFetchError(
                        AdErrorType.NO_AD,
                        "no ad tag uri or vast ad data for $adBreak"
                    )
            }
        } ?: run {
            /** throw error as ad source is null **/
            listener.onVastFetchError(AdErrorType.NO_AD, "AdSource is null for $adBreak")
        }
    }
}
