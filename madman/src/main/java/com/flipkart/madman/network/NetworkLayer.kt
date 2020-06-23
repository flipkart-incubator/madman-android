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
package com.flipkart.madman.network

import android.os.CancellationSignal
import com.flipkart.madman.network.model.NetworkAdRequest

/**
 * Network Layer
 */
interface NetworkLayer {
    /**
     * Fetch vmap for the given url
     * returns a string xml as response
     */
    fun fetch(
        request: NetworkAdRequest,
        resultListener: NetworkListener<String>,
        cancellationSignal: CancellationSignal
    )

    /**
     * make the post call for the given url
     */
    fun post(url: String, resultListener: NetworkListener<String>)
}
