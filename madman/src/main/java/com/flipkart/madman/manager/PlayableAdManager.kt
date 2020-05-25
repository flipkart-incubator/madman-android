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

interface PlayableAdManager {
    /**
     * Starts the [AdManager] and attaches the [ProgressHandler] for ad playback.
     * This is called by the client
     */
    fun start()

    /**
     * Pauses the [AdManager], pauses the ad if it is playing and removes the [ProgressHandler]
     */
    fun pause()

    /**
     * Resumes the [AdManager], resume the ad if it is paused and attaches the [ProgressHandler]
     */
    fun resume()

    /**
     * Destroy the [AdManager]
     */
    fun destroy()

    /**
     * Notified by the client when the content is completed.
     */
    fun contentComplete()

    /**
     * Returns all the cue points in the [VMAPData]
     *
     * @return list of cue points
     */
    fun getCuePoints(): List<Float>
}
