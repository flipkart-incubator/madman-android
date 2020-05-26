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
package com.flipkart.madman.parser

import com.flipkart.madman.component.model.common.Tracking
import com.flipkart.madman.component.model.vast.VASTData
import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.component.model.vmap.AdSource
import com.flipkart.madman.component.model.vmap.AdTagURI
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.helper.Utils
import com.flipkart.madman.parser.exception.ParserException
import com.flipkart.madman.parser.helper.ParserErrorCode
import com.flipkart.madman.parser.helper.XmlParserHelper.readAttr
import com.flipkart.madman.parser.helper.XmlParserHelper.readAttrAsBool
import com.flipkart.madman.parser.helper.XmlParserHelper.readText
import com.flipkart.madman.parser.helper.XmlParserHelper.requireEndTag
import com.flipkart.madman.parser.helper.XmlParserHelper.requireStartTag
import com.flipkart.madman.parser.helper.XmlParserHelper.skip
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

/**
 * Handles VMAP parsing and builds [VMAPData]
 * VMAP can also contain VAST data, this parser delegates the vast parsing to the [VASTParser]
 *
 * @see [VMAP Specification](https://www.iab.com/wp-content/uploads/2015/06/VMAPv1_0.pdf)
 *
 * @author anirudhramanan
 */
class VMAPParser(private var pullParser: XmlPullParser, private var vastParser: Parser<VASTData>) :
    Parser<VMAPData> {

    @Throws(ParserException::class)
    override fun parse(xmlString: String): VMAPData? {
        try {
            /** make sure the vmap response starts with the correct tag **/
            requireStartTag(pullParser, VMAPData.VMAP_XML_TAG)

            val vmap = VMAPData()
            vmap.version = readAttr(pullParser, VMAPData.VERSION_XML_ATTR)
            pullParser.next()

            val adBreaks = ArrayList<AdBreak>()
            var currentAdBreak: AdBreak? = null
            var event = pullParser.eventType

            /** loop until the correct closing tag is found **/
            while (pullParser.name != VMAPData.VMAP_XML_TAG) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        when (pullParser.name) {
                            VMAPData.AD_BREAK_XML_TAG -> {
                                /** ad break found **/
                                currentAdBreak = readAdBreak(pullParser)
                            }
                            AdBreak.AD_SOURCE_XML_TAG -> {
                                /** ad source found **/
                                currentAdBreak?.adSource = readAdSource(pullParser, xmlString)
                            }
                            AdBreak.TRACKING_EVENT_XML_TAG -> {
                                /** tracking events found **/
                                currentAdBreak?.trackingEvents = readTrackingEvents(pullParser)

                                val trackingMap: MutableMap<Tracking.TrackingEvent, ArrayList<String>> =
                                    mutableMapOf()

                                currentAdBreak?.trackingEvents?.forEach { tracking ->
                                    tracking.event?.let {
                                        /** map to tracking event **/
                                        val parsedEvent = Tracking.TrackingEvent.getType(it)
                                        parsedEvent?.let {
                                            tracking.url?.let { url ->
                                                /** if tracking event present, append the url to the map **/
                                                var trackingUrls = trackingMap[parsedEvent]
                                                if (trackingUrls == null) {
                                                    trackingUrls = ArrayList()
                                                }
                                                trackingUrls.add(url)
                                                trackingMap[parsedEvent] = trackingUrls
                                            }
                                        }
                                    }
                                }

                                currentAdBreak?.eventToTrackingUrlsMap = trackingMap
                            }
                            else -> skip(pullParser)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (pullParser.name) {
                            VMAPData.AD_BREAK_XML_TAG -> {
                                /** once the ad break closing tag is found, add the object to the list **/
                                currentAdBreak?.let {
                                    adBreaks.add(it)
                                }
                            }
                        }
                    }
                }
                event = pullParser.next()
            }

            var currentPodIndex =
                if (adBreaks.any { it.timeOffset == AdBreak.TimeOffsetTypes.START }) 0 else 1
            var previousAdBreak: AdBreak? = adBreaks.first()
            for (adBreak in adBreaks) {
                if (previousAdBreak?.timeOffset != adBreak.timeOffset) {
                    currentPodIndex += 1
                }
                adBreak.podIndex = currentPodIndex
                previousAdBreak = adBreak
            }

//            adBreaks.forEach {
//                it.podIndex = currentPodIndex
//                currentPodIndex += 1
//            }

            vmap.adBreaks = if (adBreaks.size > 0) adBreaks else null

            /** make sure the vmap response ends with the correct tag **/
            requireEndTag(pullParser, VMAPData.VMAP_XML_TAG)
            return vmap
        } catch (e: XmlPullParserException) {
            throw ParserException(
                "VMAP Parsing failed: ${e.cause?.message}",
                ParserErrorCode.VMAP_PARSING_ERROR
            )
        } catch (e: IOException) {
            throw ParserException(
                "VMAP Parsing failed: ${e.cause?.message}",
                ParserErrorCode.VMAP_PARSING_ERROR
            )
        }
    }

    /**
     * Read and build the [AdBreak] model
     */
    @Throws(IOException::class, XmlPullParserException::class, ParserException::class)
    private fun readAdBreak(parser: XmlPullParser): AdBreak {
        /** check start tag **/
        requireStartTag(parser, VMAPData.AD_BREAK_XML_TAG)

        val currentAdBreak = AdBreak()
        currentAdBreak.breakType = readAttr(parser, AdBreak.BREAK_TYPE_XML_ATTR)
        currentAdBreak.timeOffset = readAttr(parser, AdBreak.TIME_OFFSET_XML_ATTR)
        currentAdBreak.timeOffsetInSec =
            Utils.convertAdTimeOffsetToSeconds(currentAdBreak.timeOffset)
        currentAdBreak.breakId = readAttr(parser, AdBreak.BREAK_ID_XML_ATTR)
        currentAdBreak.repeatAfter = readAttr(parser, AdBreak.REPEAT_AFTER_XML_ATTR)
        return currentAdBreak
    }

    /**
     * Read and build the list of [Tracking] model
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTrackingEvents(parser: XmlPullParser): MutableList<Tracking> {
        /** check start tag **/
        requireStartTag(parser, AdBreak.TRACKING_EVENT_XML_TAG)
        parser.nextTag()

        val trackingEvents = ArrayList<Tracking>()
        var event = pullParser.eventType

        /** loop until closing tag is found **/
        while (pullParser.name != AdBreak.TRACKING_EVENT_XML_TAG) {
            if (event == XmlPullParser.START_TAG) {
                when (pullParser.name) {
                    AdBreak.TRACKING_XML_TAG -> {
                        val tracking = Tracking()
                        tracking.event = readAttr(parser, Tracking.EVENT_XML_ATTR)
                        tracking.url = readText(parser)
                        trackingEvents.add(tracking)
                    }
                    else -> skip(pullParser)
                }
            }
            event = pullParser.next()
        }

        /** check end tag **/
        requireEndTag(parser, AdBreak.TRACKING_EVENT_XML_TAG)
        return trackingEvents
    }

    /**
     * Read and build the [AdSource] model
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readAdSource(parser: XmlPullParser, xmlString: String): AdSource {
        /** check start tag **/
        requireStartTag(parser, AdBreak.AD_SOURCE_XML_TAG)

        val currentAdSource = AdSource()
        currentAdSource.allowMultipleAds =
            readAttrAsBool(parser, AdSource.MULTIPLE_ADS_XML_ATTR) ?: true
        currentAdSource.followRedirects = readAttrAsBool(parser, AdSource.FOLLOW_REDIRECT_XML_ATTR)
        currentAdSource.id = readAttr(parser, AdSource.ID_XML_ATTR)

        pullParser.nextTag()
        var event = pullParser.eventType

        /** loop until closing tag is found **/
        while (pullParser.name != AdBreak.AD_SOURCE_XML_TAG) {
            if (event == XmlPullParser.START_TAG) {
                when (pullParser.name) {
                    AdSource.AD_TAG_URI_XML_TAG_V1 -> {
                        /** ad tag found **/
                        currentAdSource.adTagURI =
                            readAdTagURI(pullParser, AdSource.AD_TAG_URI_XML_TAG_V1)
                    }
                    AdSource.AD_TAG_URI_XML_TAG_V2 -> {
                        /** ad tag found **/
                        currentAdSource.adTagURI =
                            readAdTagURI(pullParser, AdSource.AD_TAG_URI_XML_TAG_V2)
                    }
                    AdSource.VAST_DATA_XML_TAG_V1, AdSource.VAST_DATA_XML_TAG_V2 -> {
                        /** vast found, delegate it to the vast parser **/
                        pullParser.nextTag()
                        currentAdSource.vastAdData = vastParser.parse(xmlString)
                    }
                    else -> skip(pullParser)
                }
            }
            event = pullParser.next()
        }

        /** check end tag **/
        requireEndTag(parser, AdBreak.AD_SOURCE_XML_TAG)
        return currentAdSource
    }

    /**
     * Read and build the [AdTagURI] model
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readAdTagURI(parser: XmlPullParser, tag: String): AdTagURI {
        /** check start tag **/
        requireStartTag(parser, tag)
        val currentAdTagURI = AdTagURI()
        currentAdTagURI.templateType = readAttr(parser, AdTagURI.TEMPLATE_TYPE_XML_ATTR)
        currentAdTagURI.url = readText(parser)
        /** check end tag **/
        requireEndTag(parser, tag)
        return currentAdTagURI
    }
}
