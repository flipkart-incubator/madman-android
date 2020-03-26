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
package com.flipkart.madman.manager.model

/**
 * Represents an ad pod
 */
interface AdPod {
    /**
     * total number of ads in the pod
     */
    fun getTotalAds(): Int

    /**
     * current ad position
     */
    fun getAdPosition(): Int

    /**
     * is bumper ad
     */
    fun isBumper(): Boolean

    /**
     * max duration of the pod
     */
    fun getMaxDuration(): Double

    /**
     * pod index
     */
    fun getPodIndex(): Int

    /**
     * time offset ie the cue point
     */
    fun getTimeOffset(): Double
}
