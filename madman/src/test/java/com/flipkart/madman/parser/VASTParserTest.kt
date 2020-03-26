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

import com.flipkart.madman.component.model.vast.VASTData
import com.flipkart.madman.component.model.vast.media.LinearAdMedia
import com.flipkart.madman.parser.exception.ParserException
import com.flipkart.madman.parser.helper.ParserErrorCode
import com.flipkart.madman.parser.helper.XmlParserHelper
import com.flipkart.madman.testutils.VMAPUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.StringReader

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class VASTParserTest {
    /**
     * Test to validate a valid vast response with ad
     */
    @Test
    fun testValidVASTResponse() {
        val vastResponse = VMAPUtil.createVAST()

        // verify the version is correct
        assert(vastResponse.version == "3.0")

        // verify it contains 1 ad
        assert(vastResponse.ads?.size == 1)

        // verify ad properties
        val ad = vastResponse.ads?.get(0)
        assert(ad?.id == "1")
        assert(ad?.sequence == "1")
        assert(ad?.inLine != null)
        assert(ad?.inLine?.adSystem == "iabtechlab")
        assert(ad?.inLine?.adTitle == "iabtechlab video ad")
        assert(ad?.inLine?.creatives?.size == 1)
        assert(ad?.inLine?.creatives?.get(0)?.id == "1")
        assert(ad?.inLine?.creatives?.get(0)?.sequence == "1")
        assert(ad?.inLine?.creatives?.get(0)?.adMedia?.trackingEvents?.size == 6)
        assert((ad?.inLine?.creatives?.get(0)?.adMedia as? LinearAdMedia)?.mediaFiles?.size == 1)
        assert((ad?.inLine?.creatives?.get(0)?.adMedia as? LinearAdMedia)?.mediaFiles?.get(0)?.type == "video/mp4")
        assert((ad?.inLine?.creatives?.get(0)?.adMedia as? LinearAdMedia)?.mediaFiles?.get(0)?.id == "1")
        assert((ad?.inLine?.creatives?.get(0)?.adMedia as? LinearAdMedia)?.mediaFiles?.get(0)?.delivery == "progressive")
        assert((ad?.inLine?.creatives?.get(0)?.adMedia as? LinearAdMedia)?.videoClicks?.clicks?.size == 2)
    }

    /**
     * Test to validate a valid vast response with ad (reordered)
     */
    @Test
    fun testValidVASTResponseWithReordering() {
        val vastResponse = VMAPUtil.createVAST()

        // verify the version is correct
        assert(vastResponse.version == "3.0")

        // verify it contains 1 ad
        assert(vastResponse.ads?.size == 1)

        // verify ad properties
        val ad = vastResponse.ads?.get(0)
        assert(ad?.id == "1")
        assert(ad?.sequence == "1")
        assert(ad?.inLine != null)
        assert(ad?.inLine?.adSystem == "iabtechlab")
        assert(ad?.inLine?.adTitle == "iabtechlab video ad")
        assert(ad?.inLine?.creatives?.size == 1)
        assert(ad?.inLine?.creatives?.get(0)?.id == "1")
        assert(ad?.inLine?.creatives?.get(0)?.sequence == "1")
        assert(ad?.inLine?.creatives?.get(0)?.adMedia?.trackingEvents?.size == 6)
        assert((ad?.inLine?.creatives?.get(0)?.adMedia as? LinearAdMedia)?.mediaFiles?.size == 1)
        assert((ad?.inLine?.creatives?.get(0)?.adMedia as? LinearAdMedia)?.mediaFiles?.get(0)?.type == "video/mp4")
        assert((ad?.inLine?.creatives?.get(0)?.adMedia as? LinearAdMedia)?.mediaFiles?.get(0)?.id == "1")
        assert((ad?.inLine?.creatives?.get(0)?.adMedia as? LinearAdMedia)?.mediaFiles?.get(0)?.delivery == "progressive")
        assert((ad?.inLine?.creatives?.get(0)?.adMedia as? LinearAdMedia)?.videoClicks?.clicks?.size == 2)
    }

    /**
     * Test to validate a corrupt vast
     */
    @Test
    fun testCorruptVASTThrowsException() {
        val vastResponse = VMAPUtil.readCorruptVAST()
        val parser = createVASTParser(vastResponse)

        try {
            val parsedVASTData = parser.parse(vastResponse)
            assert(parsedVASTData == null)
        } catch (e: ParserException) {
            assert(e.errorCode == ParserErrorCode.VAST_PARSING_ERROR)
            assert(e.message != null)
        }
    }

    private fun createVASTParser(xmlString: String): Parser<VASTData> {
        val xmlPullParser = XmlParserHelper.createNewParser()
        xmlPullParser.setInput(StringReader(xmlString))
        xmlPullParser.nextTag()
        return VASTParser(xmlPullParser)
    }
}
