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

import com.flipkart.madman.component.enums.AdEventType
import com.flipkart.madman.listener.AdEventListener
import com.flipkart.madman.listener.impl.AdEvent
import com.flipkart.madman.manager.event.Event
import com.flipkart.madman.manager.model.AdElement
import com.flipkart.madman.manager.model.VastAd
import com.flipkart.madman.renderer.player.AdPlayer

/**
 * Event helper class
 */
class PlayerEventHelper(private val player: AdPlayer) {
    private var adEventListeners: MutableList<AdEventListener>? = null

    fun setEventListener(listener: AdEventListener) {
        if (adEventListeners == null) {
            adEventListeners = ArrayList()
        }
        adEventListeners?.add(listener)
    }

    fun handleEvent(eventType: Event, ad: VastAd?) {
        val adElement = ad?.getAdElement()
        when (eventType) {
            Event.CONTENT_RESUME -> {
                fireEventToListeners(AdEventType.CONTENT_RESUME_REQUESTED, adElement)
            }
            Event.CONTENT_PAUSE -> {
                fireEventToListeners(AdEventType.CONTENT_PAUSE_REQUESTED, adElement)
            }
            Event.LOAD_AD -> {
                ad?.let {
                    fireEventToListeners(AdEventType.LOADED, adElement)
                    player.loadAd(ad.getAdMediaUrls())
                }
            }
            Event.PLAY_AD -> {
                fireEventToListeners(AdEventType.CONTENT_PAUSE_REQUESTED, adElement)
                player.playAd()
            }
            Event.RESUME_AD -> {
                fireEventToListeners(AdEventType.RESUMED, adElement)
                player.playAd()
            }
            Event.PAUSE_AD -> {
                player.pauseAd()
                fireEventToListeners(AdEventType.PAUSED, adElement)
            }
            Event.FIRST_QUARTILE -> {
                fireEventToListeners(AdEventType.FIRST_QUARTILE, adElement)
            }
            Event.MIDPOINT -> {
                fireEventToListeners(AdEventType.MIDPOINT, adElement)
            }
            Event.THIRD_QUARTILE -> {
                fireEventToListeners(AdEventType.THIRD_QUARTILE, adElement)
            }
            Event.AD_PROGRESS -> {
                fireEventToListeners(AdEventType.PROGRESS, adElement)
            }
            Event.AD_STARTED -> {
                fireEventToListeners(AdEventType.STARTED, adElement)
            }
            Event.AD_SKIPPED -> {
                fireEventToListeners(AdEventType.SKIPPED, adElement)
            }
            Event.AD_STOPPED -> {
                player.stopAd()
            }
            Event.AD_COMPLETED -> {
                fireEventToListeners(AdEventType.COMPLETED, adElement)
            }
            Event.AD_TAPPED -> {
                fireEventToListeners(AdEventType.TAPPED, adElement)
            }
            Event.AD_BREAK_STARTED -> {
                fireEventToListeners(AdEventType.AD_BREAK_STARTED, adElement)
            }
            Event.AD_BREAK_LOADED -> {
                fireEventToListeners(AdEventType.AD_BREAK_READY, adElement)
            }
            Event.AD_BREAK_ENDED -> {
                fireEventToListeners(AdEventType.AD_BREAK_ENDED, adElement)
            }
            Event.AD_CTA_CLICKED -> {
                fireEventToListeners(AdEventType.CLICKED, adElement)
            }
            Event.ALL_AD_COMPLETED -> {
                fireEventToListeners(AdEventType.ALL_AD_COMPLETED, adElement)
            }
            else -> {
                // do nothing
            }
        }
    }

    fun destroy() {
        adEventListeners?.clear()
        adEventListeners = null
    }

    private fun fireEventToListeners(eventType: AdEventType, adElement: AdElement?) {
        val adEvent = AdEvent(eventType, adElement)
        adEventListeners?.forEach {
            it.onAdEvent(adEvent)
        }
    }
}
