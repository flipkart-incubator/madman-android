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

package com.flipkart.madman.loader

import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.network.model.Request

/**
 * AdLoader interface.
 */
interface AdLoader<T : Request> {
    /**
     * request ads for the given url or response.
     *
     * onVastFetchSuccess callback gives [VMAPData] as a parameter
     * onFailure callback gives [AdErrorType] and [String] message
     */
    fun requestAds(
        param: T,
        onSuccess: (data: VMAPData) -> Unit,
        onFailure: (errorType: AdErrorType, message: String?) -> Unit
    )
}
