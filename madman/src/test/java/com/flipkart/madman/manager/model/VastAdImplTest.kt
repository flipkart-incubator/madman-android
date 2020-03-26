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

import org.junit.Test

/**
 * Test for [VastAd]
 */
class VastAdImplTest {

    @Test
    fun testVastAdGetters() {
        val mediaUrl = mutableListOf<String>()
        mediaUrl.add("https://www.google.com")
        mediaUrl.add("https://www.yahoo.com")

        val adPod = AdPodImpl(1, 1, false, 10.0, 1, 0.0)

        val adElement = AdElementImpl("1", true, 0.0, false, 10.0, "", "", "", "", adPod)

        assert(adElement.getId() == "1")
        assert(adElement.isLinear())
        assert(adElement.getSkipOffset() == 0.0)
        assert(!adElement.canSkip())
        assert(adElement.getDuration() == 10.0)
        assert(adElement.getAdPod() == adPod)

        assert(!adElement.getAdPod().isBumper())
        assert(adElement.getAdPod().getAdPosition() == 1)
        assert(adElement.getAdPod().getTotalAds() == 1)
        assert(adElement.getAdPod().getMaxDuration() == 10.0)
        assert(adElement.getAdPod().getPodIndex() == 1)
        assert(adElement.getAdPod().getTimeOffset() == 0.0)

        val vastAdImpl =
            VastAdImpl(adElement, mediaUrl, VastAdImpl.AdTrackingImpl(null, null, null, null, null))

        assert(vastAdImpl.getAdElement() == adElement)
        assert(vastAdImpl.getAdMediaUrls() == mediaUrl)
    }
}
