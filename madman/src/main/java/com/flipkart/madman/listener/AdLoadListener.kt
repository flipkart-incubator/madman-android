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
package com.flipkart.madman.listener

import com.flipkart.madman.manager.AdManager

/**
 * Ad manager load listener callback
 */
interface AdLoadListener {
    /**
     * Called when [AdManager] is loaded
     *
     * @param AdManager to interact with
     */
    fun onAdManagerLoaded(manager: AdManager)

    /**
     * Called when [AdManager] load failed
     *
     * @param AdErrorListener.AdError which contains the error type and error message
     */
    fun onAdManagerLoadFailed(error: AdErrorListener.AdError)
}
