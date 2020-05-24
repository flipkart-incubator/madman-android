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
package com.flipkart.madman.manager

import com.flipkart.madman.listener.AdErrorListener
import com.flipkart.madman.listener.AdEventListener
import com.flipkart.madman.manager.data.VastAdProvider
import com.flipkart.madman.manager.finder.AdBreakFinder
import com.flipkart.madman.manager.tracking.TrackingHandler
import com.flipkart.madman.provider.ContentProgressProvider

interface AdManager : PlayableAdManager {
    /**
     * Initializes the [AdManager]
     *
     * @param [ContentProgressProvider] provider which returns the content progress
     */
    fun init(contentProgressProvider: ContentProgressProvider)

    /**
     * Initializes the [AdManager]
     *
     * @param [ContentProgressProvider] provider which returns the content progress
     * @param [AdBreakFinder] to find the ad break to play
     * @param [VastAdProvider] which provides the vast ad
     */
    fun init(
        contentProgressProvider: ContentProgressProvider,
        adBreakFinder: AdBreakFinder,
        vastAdProvider: VastAdProvider
    )

    /**
     * Adds an [AdEventListener] to receive ad events
     *
     * @param [AdEventListener] to add
     */
    fun addAdEventListener(listener: AdEventListener)

    /**
     * Removes an already added [AdEventListener]
     *
     * @param [AdEventListener] to remove
     */
    fun removeAdEventListener(listener: AdEventListener)

    /**
     * Adds an [AdErrorListener] to receive ad error events
     *
     * @param [AdErrorListener] to add
     */
    fun addAdErrorListener(listener: AdErrorListener)

    /**
     * Removes an already added [AdErrorListener]
     *
     * @param [AdErrorListener] to remove
     */
    fun removeAdErrorListener(listener: AdErrorListener)

    /**
     * Override the [TrackingHandler]
     */
    fun addTrackingHandler(handler: TrackingHandler) {}
}
