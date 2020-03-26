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
package com.flipkart.madman.manager.finder

import com.flipkart.madman.component.model.vmap.AdBreak

/**
 * Finds the playable ad break using the current position of the the media
 */
interface AdBreakFinder {
    /**
     * Find a ad break to play given the current position, duration of the content
     */
    fun findPlayableAdBreak(
        currentPosition: Float,
        contentStartPosition: Float,
        contentDuration: Float,
        adBreakList: List<AdBreak>
    ): AdBreak?

    /**
     * Should the finder scan for ad break
     */
    fun scanForAdBreak(currentPosition: Float, adBreakList: List<AdBreak>): Boolean
}
