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
 * Plain implementation of [AdElement]
 */
class AdElementImpl(
    private val id: String,
    private val isLinear: Boolean,
    private val skipOffset: Double,
    private val canSkip: Boolean,
    private val duration: Double,
    private val title: String,
    private val adSystem: String,
    private val description: String,
    private val clickThroughUrl: String?,
    private val adPod: AdPod
) : AdElement {
    override fun getAdSystem(): String {
        return adSystem
    }

    override fun getDescription(): String {
        return description
    }

    override fun getClickThroughUrl(): String? {
        return clickThroughUrl
    }

    override fun getTitle(): String {
        return title
    }

    override fun getId(): String {
        return id
    }

    override fun isLinear(): Boolean {
        return isLinear
    }

    override fun canSkip(): Boolean {
        return canSkip
    }

    override fun getSkipOffset(): Double {
        return skipOffset
    }

    override fun getDuration(): Double {
        return duration
    }

    override fun getAdPod(): AdPod {
        return adPod
    }
}
