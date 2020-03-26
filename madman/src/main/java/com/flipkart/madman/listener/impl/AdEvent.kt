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
package com.flipkart.madman.listener.impl

import com.flipkart.madman.component.enums.AdEventType
import com.flipkart.madman.manager.model.AdElement
import com.flipkart.madman.listener.AdEventListener

/**
 * Ad event implementation, passed as an argument in the event callback interface [AdEventListener]
 *
 * eventType: [AdEventType]
 * adElement: [AdElement]
 */
class AdEvent(private val eventType: AdEventType, private val adElement: AdElement?) :
    AdEventListener.AdEvent {
    override fun getAdElement(): AdElement? {
        return adElement
    }

    override fun getType(): AdEventType {
        return eventType
    }
}
