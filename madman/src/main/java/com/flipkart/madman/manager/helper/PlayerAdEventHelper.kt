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
import com.flipkart.madman.component.enums.AdEventType
import com.flipkart.madman.listener.AdErrorListener
import com.flipkart.madman.listener.AdEventListener
import com.flipkart.madman.listener.impl.AdError
import com.flipkart.madman.listener.impl.AdEvent
import com.flipkart.madman.manager.event.Event
import com.flipkart.madman.manager.model.AdElement
import com.flipkart.madman.manager.model.VastAd
import com.flipkart.madman.renderer.player.AdPlayer

/**
 * [AdEvent] helper class which notifies all the registered [AdEventListener]
 *
 * The [AdManager]  fires the internal [Event] and this class maps the [Event] to [AdEvent]
 * and interacts with the [AdPlayer]
 */
class PlayerAdEventHelper(private val player: AdPlayer) {
    private var adEventListeners: MutableList<AdEventListener>? = null
    private var adErrorListeners: MutableList<AdErrorListener>? = null

    fun addEventListener(listener: AdEventListener) {
        if (adEventListeners == null) {
            adEventListeners = ArrayList()
        }
        adEventListeners?.add(listener)
    }

    fun addErrorListener(listener: AdErrorListener) {
        if (adErrorListeners == null) {
            adErrorListeners = ArrayList()
        }
        adErrorListeners?.add(listener)
    }

    fun removeAdEventListener(listener: AdEventListener) {
        adEventListeners?.remove(listener)
    }

    fun removeAdErrorListener(listener: AdErrorListener) {
        adErrorListeners?.remove(listener)
    }

    fun handleEvent(eventType: Event, ad: VastAd?) {
        val adElement = ad?.getAdElement()
        when (eventType) {
            Event.CONTENT_RESUME -> {
                notifyAllAdEventListeners(AdEventType.CONTENT_RESUME_REQUESTED, adElement)
            }
            Event.CONTENT_PAUSE -> {
                notifyAllAdEventListeners(AdEventType.CONTENT_PAUSE_REQUESTED, adElement)
            }
            Event.LOAD_AD -> {
                ad?.let {
                    player.loadAd(ad.getAdMediaUrls())
                }
            }
            Event.PLAY_AD -> {
                player.playAd()
            }
            Event.RESUME_AD -> {
                notifyAllAdEventListeners(AdEventType.RESUMED, adElement)
                player.playAd()
            }
            Event.PAUSE_AD -> {
                player.pauseAd()
                notifyAllAdEventListeners(AdEventType.PAUSED, adElement)
            }
            Event.FIRST_QUARTILE -> {
                notifyAllAdEventListeners(AdEventType.FIRST_QUARTILE, adElement)
            }
            Event.MIDPOINT -> {
                notifyAllAdEventListeners(AdEventType.MIDPOINT, adElement)
            }
            Event.THIRD_QUARTILE -> {
                notifyAllAdEventListeners(AdEventType.THIRD_QUARTILE, adElement)
            }
            Event.AD_LOADED -> {
                notifyAllAdEventListeners(AdEventType.LOADED, adElement)
            }
            Event.AD_PROGRESS -> {
                notifyAllAdEventListeners(AdEventType.PROGRESS, adElement)
            }
            Event.AD_STARTED -> {
                notifyAllAdEventListeners(AdEventType.STARTED, adElement)
            }
            Event.AD_SKIPPED -> {
                notifyAllAdEventListeners(AdEventType.SKIPPED, adElement)
            }
            Event.AD_STOPPED -> {
                player.stopAd()
            }
            Event.AD_COMPLETED -> {
                notifyAllAdEventListeners(AdEventType.COMPLETED, adElement)
            }
            Event.AD_TAPPED -> {
                notifyAllAdEventListeners(AdEventType.TAPPED, adElement)
            }
            Event.AD_BREAK_STARTED -> {
                notifyAllAdEventListeners(AdEventType.AD_BREAK_STARTED, adElement)
            }
            Event.AD_BREAK_LOADED -> {
                notifyAllAdEventListeners(AdEventType.AD_BREAK_READY, adElement)
            }
            Event.AD_BREAK_ENDED -> {
                notifyAllAdEventListeners(AdEventType.AD_BREAK_ENDED, adElement)
            }
            Event.AD_CTA_CLICKED -> {
                notifyAllAdEventListeners(AdEventType.CLICKED, adElement)
            }
            Event.ALL_AD_COMPLETED -> {
                notifyAllAdEventListeners(AdEventType.ALL_AD_COMPLETED, adElement)
            }
            else -> {
                // do nothing
            }
        }
    }

    fun handleError(error: AdErrorType, errorMessage: String?) {
        notifyAllAdErrorListeners(error, errorMessage ?: "")
    }

    fun destroy() {
        adEventListeners?.clear()
        adErrorListeners?.clear()
        adEventListeners = null
        adErrorListeners = null
    }

    private fun notifyAllAdEventListeners(eventType: AdEventType, adElement: AdElement?) {
        val adEvent = AdEvent(eventType, adElement)
        adEventListeners?.forEach {
            it.onAdEvent(adEvent)
        }
    }

    private fun notifyAllAdErrorListeners(errorType: AdErrorType, errorMessage: String) {
        val adError = AdError(errorType, errorMessage)
        adErrorListeners?.forEach {
            it.onAdError(adError)
        }
    }
}
