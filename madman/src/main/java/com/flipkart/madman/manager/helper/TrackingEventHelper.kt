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
package com.flipkart.madman.manager.helper

import com.flipkart.madman.component.model.common.Tracking
import com.flipkart.madman.manager.event.Event
import com.flipkart.madman.manager.model.VastAd
import com.flipkart.madman.manager.tracking.TrackingHandler

/**
 * Tracking helper class
 */
class TrackingEventHelper(
    private var trackingHandler: TrackingHandler
) {

    fun setTrackingHandler(trackingHandler: TrackingHandler) {
        this.trackingHandler = trackingHandler
    }

    fun handleEvent(eventType: Event, ad: VastAd?, errorCode: Int? = null) {
        val adTracking: VastAd.AdTracking? = ad?.getAdTracking()
        val trackingMap: Map<Tracking.TrackingEvent, List<String>>? = adTracking?.getAdTrackingMap()

        when (eventType) {
            Event.AD_STARTED -> {
                track(Tracking.TrackingEvent.START, trackingMap)
                track(Tracking.TrackingEvent.IMPRESSION, adTracking?.getAdImpressionUrls())
            }
            Event.FIRST_QUARTILE -> {
                track(Tracking.TrackingEvent.FIRST_QUARTILE, trackingMap)
            }
            Event.MIDPOINT -> {
                track(Tracking.TrackingEvent.MIDPOINT, trackingMap)
            }
            Event.THIRD_QUARTILE -> {
                track(Tracking.TrackingEvent.THIRD_QUARTILE, trackingMap)
            }
            Event.AD_COMPLETED -> {
                track(Tracking.TrackingEvent.COMPLETE, trackingMap)
            }
            Event.AD_SKIPPED -> {
                track(Tracking.TrackingEvent.SKIP, trackingMap)
            }
            Event.PAUSE_AD -> {
                track(Tracking.TrackingEvent.PAUSE, trackingMap)
            }
            Event.RESUME_AD -> {
                track(Tracking.TrackingEvent.RESUME, trackingMap)
            }
            Event.AD_CTA_CLICKED -> {
                track(Tracking.TrackingEvent.CLICK_THROUGH, adTracking?.getClickThroughTracking())
            }
            Event.AD_ERROR -> {
                adTracking?.getAdErrorUrls()?.let {
                    val replacedUrls = replaceWithErrorCode(it, errorCode)
                    track(Tracking.TrackingEvent.ERROR, replacedUrls)
                }
            }
            Event.VAST_ERROR -> {
                adTracking?.getVastErrorUrls()?.let {
                    val replacedUrls = replaceWithErrorCode(it, errorCode)
                    track(Tracking.TrackingEvent.ERROR, replacedUrls)
                }
            }
            else -> {
                // do nothing
            }
        }
    }

    private fun track(
        event: Tracking.TrackingEvent,
        trackingMap: Map<Tracking.TrackingEvent, List<String>>?
    ) {
        val urls = trackingMap?.get(event)
        urls?.let {
            trackingHandler.trackEvent(urls, event)
        }
    }

    private fun track(
        event: Tracking.TrackingEvent,
        urls: List<String>?
    ) {
        urls?.let {
            trackingHandler.trackEvent(urls, event)
        }
    }

    private fun replaceWithErrorCode(urls: List<String>, errorCode: Int?): List<String> {
        errorCode?.let {
            val replacedUrls = ArrayList<String>(urls.size)
            urls.forEach {
                replacedUrls.add(it.replace(Constant.ERROR_CODE_MACRO, errorCode.toString()))
            }
            return replacedUrls
        } ?: run {
            return urls
        }
    }
}
