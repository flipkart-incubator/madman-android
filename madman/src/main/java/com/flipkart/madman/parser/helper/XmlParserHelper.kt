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
package com.flipkart.madman.parser.helper

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException

object XmlParserHelper {

    /**
     * create a new [XmlPullParser]
     */
    fun createNewParser(): XmlPullParser {
        val xmlPullParserFactory = XmlPullParserFactory.newInstance()
        val xmlParser = xmlPullParserFactory.newPullParser()
        xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        return xmlParser
    }

    /**
     * read the text
     */
    @Throws(IOException::class, XmlPullParserException::class)
    fun readText(parser: XmlPullParser): String? {
        var result: String? = null
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            result = result?.trim() ?: ""
            parser.nextTag()
        }
        return result
    }

    /**
     * read the attribute value of the tag
     */
    fun readAttr(parser: XmlPullParser, attributeName: String): String? {
        return parser.getAttributeValue(null, attributeName)
    }

    /**
     * read the attribute value of the tag as boolean
     */
    fun readAttrAsBool(
        parser: XmlPullParser,
        attributeName: String
    ): Boolean? {
        return readAttr(
            parser,
            attributeName
        )?.toBoolean()
    }

    /**
     * read the attribute value of the tag as int
     */
    fun readAttrAsInt(
        parser: XmlPullParser,
        attributeName: String
    ): Int? {
        return readAttr(
            parser,
            attributeName
        )?.toInt()
    }

    /**
     * read the attribute value of the tag as long
     */
    fun readAttrAsLong(
        parser: XmlPullParser,
        attributeName: String
    ): Long? {
        return readAttr(
            parser,
            attributeName
        )?.toLong()
    }

    /**
     * ensures that parser starts with the given tag
     */
    @Throws(IOException::class, XmlPullParserException::class)
    fun requireStartTag(parser: XmlPullParser, tag: String) {
        parser.require(XmlPullParser.START_TAG, null, tag)
    }

    /**
     * ensures that parser ends with the given tag
     */
    @Throws(IOException::class, XmlPullParserException::class)
    fun requireEndTag(parser: XmlPullParser, tag: String) {
        parser.require(XmlPullParser.END_TAG, null, tag)
    }

    /**
     * skip the tag
     */
    @Throws(XmlPullParserException::class, IOException::class)
    fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IOException("${parser.eventType} not of type start tag")
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}
