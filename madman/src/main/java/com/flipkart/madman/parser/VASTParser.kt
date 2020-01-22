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

package com.flipkart.madman.parser

import com.flipkart.madman.component.model.common.Tracking
import com.flipkart.madman.component.model.vast.*
import com.flipkart.madman.component.model.vast.media.BaseAdMedia
import com.flipkart.madman.component.model.vast.media.LinearAdMedia
import com.flipkart.madman.helper.Utils
import com.flipkart.madman.parser.exception.ParserException
import com.flipkart.madman.parser.helper.ParserErrorCode
import com.flipkart.madman.parser.helper.XmlParserHelper.readAttr
import com.flipkart.madman.parser.helper.XmlParserHelper.readAttrAsBool
import com.flipkart.madman.parser.helper.XmlParserHelper.readAttrAsInt
import com.flipkart.madman.parser.helper.XmlParserHelper.readAttrAsLong
import com.flipkart.madman.parser.helper.XmlParserHelper.readText
import com.flipkart.madman.parser.helper.XmlParserHelper.requireEndTag
import com.flipkart.madman.parser.helper.XmlParserHelper.requireStartTag
import com.flipkart.madman.parser.helper.XmlParserHelper.skip
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

/**
 * Handles VAST parsing and builds [VASTData]
 *
 * @author anirudhramanan
 */
class VASTParser(private var pullParser: XmlPullParser) :
    Parser<VASTData> {

    @Throws(ParserException::class)
    override fun parse(xmlString: String): VASTData? {
        try {
            /** make sure the vast response starts with the correct tag **/
            requireStartTag(pullParser, VASTData.VAST_XML_TAG)

            val vastData = VASTData()
            vastData.version = readAttr(pullParser, VASTData.VERSION_XML_ATTR)

            pullParser.next()

            var currentAd: Ad? = null
            val ads = ArrayList<Ad>()
            var event = pullParser.eventType

            /** loop until the closing tag is found **/
            while (pullParser.name != VASTData.VAST_XML_TAG) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        when (pullParser.name) {
                            VASTData.AD_XML_TAG -> {
                                /** ad tag found **/
                                currentAd = readAd(pullParser)
                            }
                            VASTData.ERROR_XML_ATTR -> {
                                /** error tag found **/
                                val errorUrl = readText(pullParser)
                                val errorUrlList =
                                    vastData.errorUrls?.toMutableList() ?: ArrayList()
                                errorUrl?.let { errorUrlList.add(it) }
                                vastData.errorUrls = errorUrlList
                            }
                            Ad.INLINE_XML_TAG -> {
                                /** inline tag found **/
                                currentAd?.inLine = readInLine(pullParser)
                            }
                            else -> skip(pullParser)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (pullParser.name) {
                            VASTData.AD_XML_TAG -> {
                                /** ad tag closing found, add to the list **/
                                currentAd?.let { ads.add(it) }
                            }
                        }
                    }
                }
                event = pullParser.next()
            }

            /** make sure the vast response ends with the correct tag **/
            requireEndTag(pullParser, VASTData.VAST_XML_TAG)

            vastData.ads = ads
            return vastData
        } catch (e: XmlPullParserException) {
            throw ParserException(
                "VAST Parsing failed: ${e.cause?.message}",
                ParserErrorCode.VAST_PARSING_ERROR
            )
        } catch (e: IOException) {
            throw ParserException(
                "VAST Parsing failed: ${e.cause?.message}",
                ParserErrorCode.VAST_PARSING_ERROR
            )
        }
    }

    /**
     * Read and build [Ad] model
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readAd(xmlParser: XmlPullParser): Ad {
        /** check start tag **/
        requireStartTag(xmlParser, VASTData.AD_XML_TAG)
        val ad = Ad()
        ad.id = readAttr(xmlParser, Ad.ID_XML_ATTR)
        ad.sequence = readAttr(xmlParser, Ad.SEQUENCE_XML_ATTR)
        return ad
    }

    /**
     * Read and build [InLine] model
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readInLine(pullParser: XmlPullParser): InLine {
        /** check start tag **/
        requireStartTag(pullParser, Ad.INLINE_XML_TAG)
        pullParser.nextTag()

        val inLine = InLine()
        var event = pullParser.eventType

        /** loop until close tag for inline is found **/
        while (pullParser.name != Ad.INLINE_XML_TAG) {
            if (event == XmlPullParser.START_TAG) {
                when (pullParser.name) {
                    InLine.AD_SYSTEM_XML_TAG -> inLine.adSystem = readText(pullParser)
                    InLine.AD_TITLE_XML_TAG -> inLine.adTitle = readText(pullParser)
                    InLine.DESCRIPTION_XML_TAG -> inLine.description = readText(pullParser)
                    InLine.ERROR_XML_TAG -> {
                        val errorUrl = readText(pullParser)
                        val errorUrlList = inLine.errorUrls?.toMutableList() ?: ArrayList()
                        errorUrl?.let { errorUrlList.add(it) }
                        inLine.errorUrls = errorUrlList
                    }
                    InLine.IMPRESSION_XML_TAG -> {
                        val impressionUrl = readText(pullParser)
                        val impressionList = inLine.impressionUrls?.toMutableList() ?: ArrayList()
                        impressionUrl?.let { impressionList.add(it) }
                        inLine.impressionUrls = impressionList
                    }
                    InLine.CREATIVES_XML_TAG -> inLine.creatives = readCreatives(pullParser)
                    else -> skip(pullParser)
                }
            }
            event = pullParser.next()
        }

        /** check end tag **/
        requireEndTag(pullParser, Ad.INLINE_XML_TAG)
        return inLine
    }

    /**
     * Read and build the list of [Creative] models
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readCreatives(xmlParser: XmlPullParser): List<Creative> {
        /** check start tag **/
        requireStartTag(xmlParser, InLine.CREATIVES_XML_TAG)
        xmlParser.nextTag()

        val creatives = ArrayList<Creative>()
        var event = xmlParser.eventType
        while (xmlParser.name != InLine.CREATIVES_XML_TAG) {
            if (event == XmlPullParser.START_TAG) {
                when (xmlParser.name) {
                    Creative.CREATIVE_XML_TAG -> {
                        val creative = Creative()
                        creative.id = readAttr(xmlParser, Creative.ID_XML_ATTR)
                        creative.sequence = readAttr(xmlParser, Creative.SEQUENCE_XML_ATTR)
                        creative.adMedia = readMedia(xmlParser)
                        creatives.add(creative)
                    }
                    else -> skip(xmlParser)
                }
            }
            event = xmlParser.next()
        }

        /** check end tag **/
        requireEndTag(xmlParser, InLine.CREATIVES_XML_TAG)
        return creatives
    }

    /**
     * Read media [BaseAdMedia] could a linear ad media or a companion ad media
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readMedia(xmlParser: XmlPullParser): BaseAdMedia? {
        xmlParser.nextTag()
        when (xmlParser.name) {
            Creative.LINEAR_XML_TAG -> {
                return readLinearAdMedia(xmlParser)
            }
            else -> {
                skip(xmlParser)
            }
        }
        return null
    }

    /**
     * Read and build the [LinearAdMedia] model
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readLinearAdMedia(xmlParser: XmlPullParser): LinearAdMedia {
        /** check start tag **/
        requireStartTag(xmlParser, Creative.LINEAR_XML_TAG)

        val media = LinearAdMedia()
        media.skipOffset = readAttr(xmlParser, LinearAdMedia.SKIP_OFFSET_XML_TAG)

        xmlParser.nextTag()

        var event = xmlParser.eventType

        /** loop until closing tag is found **/
        while (xmlParser.name != Creative.LINEAR_XML_TAG) {
            if (event == XmlPullParser.START_TAG) {
                when (xmlParser.name) {
                    LinearAdMedia.DURATION_XML_TAG -> {
                        media.duration = readText(xmlParser)
                        media.durationInSeconds = Utils.convertDateFormatToSeconds(media.duration)
                    }
                    LinearAdMedia.TRACKING_EVENTS_XML_TAG -> {
                        media.trackingEvents = readTrackingEvents(xmlParser)

                        val trackingMap: MutableMap<Tracking.TrackingEvent, ArrayList<String>> =
                            mutableMapOf()

                        media.trackingEvents?.forEach { tracking ->
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

                        media.eventToTrackingUrlsMap = trackingMap
                    }
                    LinearAdMedia.MEDIA_FILES_XML_TAG -> media.mediaFiles =
                        readMediaFiles(xmlParser)
                    LinearAdMedia.VIDEO_CLICKS_XML_TAG -> media.videoClicks =
                        readVideoClicks(xmlParser)
                    else -> skip(xmlParser)
                }
            }
            event = xmlParser.next()
        }

        /** check end tag **/
        requireEndTag(xmlParser, Creative.LINEAR_XML_TAG)

        media.skipOffset?.let {
            if (it.contains("%")) {
                // skip offset of type n%
                val percentage = it.replace("%", "").toDouble()
                media.skipOffsetInSeconds = (media.durationInSeconds * percentage) / 100
            } else {
                // skip offset of type HH:MM:SS.mmm
                media.skipOffsetInSeconds = Utils.convertDateFormatToSeconds(it)
            }
        }

        return media
    }

    /**
     * Read and build the list of [Tracking] model
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTrackingEvents(parser: XmlPullParser): MutableList<Tracking> {
        /** check start tag **/
        requireStartTag(parser, LinearAdMedia.TRACKING_EVENTS_XML_TAG)
        parser.nextTag()

        val trackingEvents = ArrayList<Tracking>()
        var event = pullParser.eventType

        /** loop until closing tag is found **/
        while (pullParser.name != LinearAdMedia.TRACKING_EVENTS_XML_TAG) {
            if (event == XmlPullParser.START_TAG) {
                when (pullParser.name) {
                    LinearAdMedia.TRACKING_XML_TAG -> {
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
        requireEndTag(parser, LinearAdMedia.TRACKING_EVENTS_XML_TAG)
        return trackingEvents
    }

    /**
     * Read and build the list of [MediaFile] model
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readMediaFiles(parser: XmlPullParser): MutableList<MediaFile> {
        /** check start tag **/
        requireStartTag(parser, LinearAdMedia.MEDIA_FILES_XML_TAG)
        parser.nextTag()

        val mediaFiles = ArrayList<MediaFile>()
        var event = pullParser.eventType

        /** loop until closing tag is found **/
        while (pullParser.name != LinearAdMedia.MEDIA_FILES_XML_TAG) {
            if (event == XmlPullParser.START_TAG) {
                when (pullParser.name) {
                    MediaFile.MEDIA_FILE_XML_TAG -> {
                        val mediaFile = MediaFile()
                        mediaFile.id = readAttr(pullParser, MediaFile.ID_XML_ATTR)
                        mediaFile.delivery = readAttr(pullParser, MediaFile.DELIVERY_XML_ATTR)
                        mediaFile.width = readAttrAsInt(pullParser, MediaFile.WIDTH_XML_ATTR)
                        mediaFile.height = readAttrAsInt(pullParser, MediaFile.HEIGHT_XML_ATTR)
                        mediaFile.type = readAttr(pullParser, MediaFile.TYPE_XML_ATTR)
                        mediaFile.bitrate = readAttrAsLong(pullParser, MediaFile.BITRATE_XML_ATTR)
                        mediaFile.scalable = readAttrAsBool(pullParser, MediaFile.SCALABLE_XML_ATTR)
                        mediaFile.maintainAspectRatio =
                            readAttrAsBool(pullParser, MediaFile.ASPECT_RATIO_XML_ATTR)
                        mediaFile.url = readText(pullParser)
                        mediaFiles.add(mediaFile)
                    }
                    else -> skip(pullParser)
                }
            }
            event = pullParser.next()
        }

        /** check end tag **/
        requireEndTag(parser, LinearAdMedia.MEDIA_FILES_XML_TAG)
        return mediaFiles
    }

    /**
     * Read and build the map of [VideoClicks] model
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readVideoClicks(parser: XmlPullParser): VideoClicks {
        /** check start tag **/
        requireStartTag(parser, LinearAdMedia.VIDEO_CLICKS_XML_TAG)
        parser.nextTag()

        val videoClicks = VideoClicks()
        val clickEvents = LinkedHashMap<String, MutableList<ClickEvent>>()
        var event = pullParser.eventType

        /** loop until closing tag is found **/
        while (pullParser.name != LinearAdMedia.VIDEO_CLICKS_XML_TAG) {
            if (event == XmlPullParser.START_TAG) {
                val clickEvent = ClickEvent()
                clickEvent.id = readAttr(pullParser, ClickEvent.ID_XML_ATTR)
                clickEvent.url = readText(pullParser)
                var clickEventList = clickEvents[pullParser.name]
                if (clickEventList == null) {
                    clickEventList = mutableListOf()
                }
                clickEventList.add(clickEvent)
                clickEvents[pullParser.name] = clickEventList
            }
            event = pullParser.next()
        }

        /** check end tag **/
        requireEndTag(parser, LinearAdMedia.VIDEO_CLICKS_XML_TAG)
        videoClicks.clicks = clickEvents
        return videoClicks
    }
}
