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

import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.model.common.Tracking
import com.flipkart.madman.manager.event.Error
import com.flipkart.madman.manager.event.Event
import com.flipkart.madman.manager.model.AdElement
import com.flipkart.madman.manager.model.VastAd
import com.flipkart.madman.manager.tracking.TrackingHandler

/**
 * Tracking helper class
 */
class TrackingEventHelper(private var trackingHandler: TrackingHandler) {

    fun setTrackingHandler(trackingHandler: TrackingHandler) {
        this.trackingHandler = trackingHandler
    }

    fun handleEvent(eventType: Event, ad: VastAd?, errorCode: Int? = null) {
        val adTracking: VastAd.AdTracking? = ad?.getAdTracking()
        val trackingMap: Map<Tracking.TrackingEvent, List<String>>? = adTracking?.getAdTrackingMap()
        val adElement = ad?.getAdElement()

        when (eventType) {
            Event.AD_STARTED -> {
                track(Tracking.TrackingEvent.START, trackingMap, adElement)
                track(
                    Tracking.TrackingEvent.IMPRESSION,
                    adTracking?.getAdImpressionUrls(),
                    adElement
                )
            }
            Event.FIRST_QUARTILE -> {
                track(Tracking.TrackingEvent.FIRST_QUARTILE, trackingMap, adElement)
            }
            Event.MIDPOINT -> {
                track(Tracking.TrackingEvent.MIDPOINT, trackingMap, adElement)
            }
            Event.THIRD_QUARTILE -> {
                track(Tracking.TrackingEvent.THIRD_QUARTILE, trackingMap, adElement)
            }
            Event.AD_COMPLETED -> {
                track(Tracking.TrackingEvent.COMPLETE, trackingMap, adElement)
            }
            Event.AD_SKIPPED -> {
                track(Tracking.TrackingEvent.SKIP, trackingMap, adElement)
            }
            Event.PAUSE_AD -> {
                track(Tracking.TrackingEvent.PAUSE, trackingMap, adElement)
            }
            Event.RESUME_AD -> {
                track(Tracking.TrackingEvent.RESUME, trackingMap, adElement)
            }
            Event.AD_CTA_CLICKED -> {
                track(
                    Tracking.TrackingEvent.CLICK_THROUGH,
                    adTracking?.getClickThroughTracking(),
                    adElement
                )
            }
            else -> {
                // do nothing
            }
        }
    }

    fun handleError(error: AdErrorType, ad: VastAd?) {
        val adTracking: VastAd.AdTracking? = ad?.getAdTracking()
        val adElement = ad?.getAdElement()
        val errorCode = Error.mapErrorTypeToError(error).errorCode

        when (error) {
            AdErrorType.VAST_ERROR, AdErrorType.NO_MEDIA_URL -> {
                adTracking?.getVastErrorUrls()?.let {
                    val replacedUrls = replaceWithErrorCode(it, errorCode)
                    track(Tracking.TrackingEvent.ERROR, replacedUrls, adElement)
                }
            }
            else -> {
                adTracking?.getAdErrorUrls()?.let {
                    val replacedUrls = replaceWithErrorCode(it, errorCode)
                    track(Tracking.TrackingEvent.ERROR, replacedUrls, adElement)
                }
            }
        }
    }

    private fun track(
        event: Tracking.TrackingEvent,
        trackingMap: Map<Tracking.TrackingEvent, List<String>>?,
        ad: AdElement?
    ) {
        val urls = trackingMap?.get(event)
        urls?.let {
            trackingHandler.trackEvent(urls, event, ad)
        }
    }

    private fun track(
        event: Tracking.TrackingEvent,
        urls: List<String>?,
        ad: AdElement?
    ) {
        urls?.let {
            trackingHandler.trackEvent(urls, event, ad)
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
