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

package com.flipkart.madman.manager.data

import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.model.vast.VASTData
import com.flipkart.madman.component.model.vmap.AdBreak

/**
 * [VASTData] ad provider
 */
interface VastAdProvider {
    /**
     * Get vast ad from the given ad break.
     * Pass in the [Listener] for success and error callback
     */
    fun getVASTAd(adBreak: AdBreak, listener: Listener)

    interface Listener {
        /**
         * vast fetch success callback
         */
        fun onVastFetchSuccess(vastData: VASTData)

        /**
         * vast fetch error callback
         */
        fun onVastFetchError(errorType: AdErrorType, message: String)
    }
}
