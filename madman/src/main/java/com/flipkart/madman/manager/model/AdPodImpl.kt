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
 * Plain implementation of [AdPod]
 */
class AdPodImpl(
    private val totalAds: Int,
    private val adPosition: Int,
    private val isBumper: Boolean,
    private val maxDuration: Double,
    private val podIndex: Int,
    private val timeOffset: Double
) : AdPod {
    override fun getTotalAds(): Int {
        return totalAds
    }

    override fun getAdPosition(): Int {
        return adPosition
    }

    override fun isBumper(): Boolean {
        return isBumper
    }

    override fun getMaxDuration(): Double {
        return maxDuration
    }

    override fun getPodIndex(): Int {
        return podIndex
    }

    override fun getTimeOffset(): Double {
        return timeOffset
    }
}
