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

package com.flipkart.madman.manager.model

/**
 * Represent an ad element
 */
interface AdElement {
    /**
     * ad id
     */
    fun getId(): String

    /**
     * if ad is linear
     */
    fun isLinear(): Boolean

    /**
     * if ad can be skipped or not
     */
    fun canSkip(): Boolean

    /**
     * skip offset, used to show skip button
     */
    fun getSkipOffset(): Double

    /**
     * duration of the ad
     */
    fun getDuration(): Double

    /**
     * title of the ad
     */
    fun getTitle(): String

    /**
     * ad system
     */
    fun getAdSystem(): String

    /**
     * description of the ad
     *
     */
    fun getDescription(): String

    /**
     * click through url for click (for eg learn more)
     */
    fun getClickThroughUrl(): String?

    /**
     * pod information of the ad
     */
    fun getAdPod(): AdPod
}
