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
package com.flipkart.madman.manager.data.helper

import com.flipkart.madman.component.model.vast.Ad
import com.flipkart.madman.component.model.vast.VASTData
import com.flipkart.madman.component.model.vast.VideoClicks
import com.flipkart.madman.component.model.vast.media.BaseAdMedia
import com.flipkart.madman.component.model.vast.media.LinearAdMedia
import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.manager.model.AdElementImpl
import com.flipkart.madman.manager.model.AdPodImpl
import com.flipkart.madman.manager.model.VastAd
import com.flipkart.madman.manager.model.VastAdImpl

object AdDataHelper {
    /**
     * get all the cue points for ads
     */
    fun getCuePoints(data: VMAPData): List<Float> {
        return getCuePoints(data.adBreaks)
    }

    private fun getCuePoints(adBreaks: List<AdBreak>?): List<Float> {
        val list = mutableListOf<Float>()
        adBreaks?.forEach {
            val timeOffset = when (it.timeOffset) {
                AdBreak.TimeOffsetTypes.START -> 0F
                AdBreak.TimeOffsetTypes.END -> -1F
                else -> it.timeOffsetInSec
            }
            if (!list.contains(timeOffset)) {
                list.add(timeOffset)
            }
        }
        return list
    }

    /**
     * get all ad breaks
     */
    fun getAllAdBreaks(data: VMAPData): List<AdBreak> {
        val result = ArrayList<AdBreak>(data.adBreaks?.size ?: 0)
        data.adBreaks?.forEach {
            result.add(it)
        }
        return result
    }

    /**
     * check if there are pre-roll ad present
     */
    fun hasPreRollAds(data: VMAPData): Boolean {
        val result = ArrayList<AdBreak>(1)
        data.adBreaks?.forEach {
            if (AdBreak.TimeOffsetTypes.START == it.timeOffset) {
                result.add(it)
            }
        }
        return result.size > 0
    }

    /**
     * check if there are only post-roll ad present
     */
    fun hasOnlyPostRollAds(data: VMAPData): Boolean {
        return data.adBreaks?.filter { it.timeOffset != AdBreak.TimeOffsetTypes.END }
            .isNullOrEmpty()
    }

    /**
     * creates a [AdElement] for the given [VASTData]
     */
    fun createAdFor(
        playableAdBreak: AdBreak?,
        playableAdBreakIndex: Int,
        totalAdBreaks: Int
    ): VastAd? {
        playableAdBreak?.let {
            var currentAd: VastAd? = null

            it.adSource?.vastAdData?.ads?.let { ads ->
                val ad = ads.first()
                var totalDuration = 0.0
                ads.forEach {
                    totalDuration += getAdDuration(it)
                }

                when (val adMedia =
                    getAdMediaFromAd(ad)) {
                    is LinearAdMedia -> {
                        val adPod = AdPodImpl(
                            totalAdBreaks,
                            playableAdBreakIndex + 1,
                            false,
                            totalDuration,
                            it.podIndex,
                            it.timeOffsetInSec.toDouble()
                        )
                        val adElement = AdElementImpl(
                            ad.id ?: "",
                            true,
                            adMedia.skipOffsetInSeconds,
                            adMedia.skipOffset != null,
                            adMedia.durationInSeconds,
                            ad.inLine?.adTitle ?: "",
                            ad.inLine?.adSystem ?: "",
                            ad.inLine?.description ?: "",
                            getClickThroughUrl(adMedia),
                            adPod
                        )

                        currentAd = VastAdImpl(
                            adElement,
                            getMediaUrlsForAd(
                                adMedia
                            ),
                            VastAdImpl.AdTrackingImpl(
                                adMedia.eventToTrackingUrlsMap,
                                ad.inLine?.impressionUrls,
                                ad.inLine?.errorUrls,
                                it.adSource?.vastAdData?.errorUrls,
                                getClickThroughTrackingUrlList(adMedia)
                            )
                        )
                    }
                }
            }
            return currentAd
        }
        return null
    }

    /**
     * get the media urls for the [Ad]
     */
    private fun getMediaUrlsForAd(adMedia: BaseAdMedia?): List<String> {
        val result = ArrayList<String>()
        if (adMedia is LinearAdMedia) {
            adMedia.mediaFiles?.forEach {
                it.url?.let { url -> result.add(url) }
            }
        }
        return result
    }

    private fun getAdMediaFromAd(ad: Ad?): BaseAdMedia? {
        val creatives = ad?.inLine?.creatives
        if (!creatives.isNullOrEmpty()) {
            // todo: what if there are multiple creatives, has to be checked
            return creatives[0].adMedia
        }
        return null
    }

    private fun getAdDuration(ad: Ad?): Double {
        val adMedia = getAdMediaFromAd(ad)
        if (adMedia is LinearAdMedia) {
            return adMedia.durationInSeconds
        }
        return 0.0
    }

    private fun getClickThroughUrl(adMedia: LinearAdMedia): String? {
        val clickEvents = adMedia.videoClicks?.clicks?.get(VideoClicks.CLICK_THROUGH_XML_TAG)
        if (clickEvents?.isNotEmpty() == true) {
            return clickEvents[0].url
        }
        return null
    }

    private fun getClickThroughTrackingUrlList(adMedia: LinearAdMedia): List<String>? {
        val clickEvents = adMedia.videoClicks?.clicks?.get(VideoClicks.CLICK_TRACKING_XML_TAG)
        var result: MutableList<String>? = null
        clickEvents?.forEach {
            it.url?.let { url ->
                if (result == null) {
                    result = mutableListOf()
                }
                result?.add(url)
            }
        }
        return result
    }
}
