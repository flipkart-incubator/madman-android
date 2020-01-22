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

package com.flipkart.madman.manager.tracking

import com.flipkart.madman.component.model.common.Tracking
import com.flipkart.madman.logger.LogUtil
import com.flipkart.madman.network.NetworkLayer

/**
 * Default implemetation of [TrackingHandler]
 */
class DefaultTrackingHandler(private val networkLayer: NetworkLayer) : TrackingHandler {
    override fun trackEvent(url: String?) {
        url?.let {
            LogUtil.log("Sending tracking with url : $it")
            networkLayer.post(url)
        }
    }

    override fun trackEvent(event: Tracking.TrackingEvent, urlList: List<String>?) {
        urlList?.forEach {
            LogUtil.log("Sending tracking for ${event.name} with url : $it")
            trackEvent(it)
        }
    }
}
