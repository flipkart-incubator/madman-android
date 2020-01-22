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

import com.flipkart.madman.component.model.common.Tracking

/**
 * Plain implementation of [VastAd]
 */
class VastAdImpl(
    private val adElement: AdElement,
    private val adMediaUrlList: List<String>,
    private val adTracking: VastAd.AdTracking
) : VastAd {

    override fun getAdTracking(): VastAd.AdTracking {
        return adTracking
    }

    override fun getAdElement(): AdElement {
        return adElement
    }

    override fun getAdMediaUrls(): List<String> {
        return adMediaUrlList
    }

    /**
     * Plain implementation of [VastAd.AdTracking]
     */
    class AdTrackingImpl(
        private val adTrackingMap: Map<Tracking.TrackingEvent, List<String>>?,
        private val adImpressionUrls: List<String>?,
        private val adErrorUrls: List<String>?,
        private val vastErrorUrls: List<String>?,
        private val clickTrackingUrls: List<String>?
    ) : VastAd.AdTracking {
        override fun getAdTrackingMap(): Map<Tracking.TrackingEvent, List<String>>? {
            return adTrackingMap
        }

        override fun getAdImpressionUrls(): List<String>? {
            return adImpressionUrls
        }

        override fun getAdErrorUrls(): List<String>? {
            return adErrorUrls
        }

        override fun getVastErrorUrls(): List<String>? {
            return vastErrorUrls
        }

        override fun getClickThroughTracking(): List<String>? {
            return clickTrackingUrls
        }
    }
}
