package com.flipkart.madman.parser

import com.flipkart.madman.component.model.vmap.VMAPData
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
class VMAPParserTest {

    /**
     * Test to validate a vmap response with ad breaks and tracking
     */
    @Test
    fun testValidVMAPResponse() {
        val response = VMAPUtil.readVMAP()
        val parser = createVMAPParser(response)
        val parsedVMAPData = parser.parse(response)

        // verify the version is correct
        assert(parsedVMAPData?.version == "1.0")

        // verify it contains 4 ad breaks
        assert(parsedVMAPData?.adBreaks?.size == 5)

        // verify properties of 1st ad break
        val firstAdBreak = parsedVMAPData?.adBreaks?.get(0)
        assert(firstAdBreak?.breakId == null)
        assert(firstAdBreak?.breakType == "linear")
        assert(firstAdBreak?.timeOffset == "start")
        assert(firstAdBreak?.trackingEvents?.size == 3)
        assert(firstAdBreak?.adSource?.id == "2")
        assert(firstAdBreak?.adSource?.allowMultipleAds == true)
        assert(firstAdBreak?.adSource?.followRedirects == true)
        assert(firstAdBreak?.adSource?.vastAdData != null)
    }

    /**
     * Test to validate a vmap response with ad breaks and tracking in different order
     * The first two ad breaks has tracking before ad-source and last two has vice versa.
     */
    @Test
    fun testValidVMAPResponseWithReordering() {
        val response = VMAPUtil.readVMAPWithReorderedTags()
        val parser = createVMAPParser(response)
        val parsedVMAPData = parser.parse(response)

        // verify the version is correct
        assert(parsedVMAPData?.version == "1.0")

        // verify it contains 4 ad breaks
        assert(parsedVMAPData?.adBreaks?.size == 4)

        // verify properties of 1st ad break
        val firstAdBreak = parsedVMAPData?.adBreaks?.get(0)
        assert(firstAdBreak?.breakId == "midrolls2")
        assert(firstAdBreak?.breakType == "linear")
        assert(firstAdBreak?.timeOffset == "00:00:15.125")
        assert(firstAdBreak?.trackingEvents?.size == 3)
        assert(firstAdBreak?.adSource?.id == "2")
        assert(firstAdBreak?.adSource?.allowMultipleAds == true)
        assert(firstAdBreak?.adSource?.followRedirects == true)
        assert(firstAdBreak?.adSource?.adTagURI?.templateType == "vast3")
        assert(firstAdBreak?.adSource?.adTagURI?.url == "//playertest.longtailvideo.com/adtags/vast3_pod.xml")

        // verify properties of last ad break
        val lastAdBreak = parsedVMAPData?.adBreaks?.get(3)
        assert(lastAdBreak?.breakId == "post")
        assert(lastAdBreak?.breakType == "linear")
        assert(lastAdBreak?.timeOffset == "end")
        assert(lastAdBreak?.trackingEvents?.size == 3)
        assert(lastAdBreak?.adSource?.id == "3")
        assert(lastAdBreak?.adSource?.allowMultipleAds == true)
        assert(lastAdBreak?.adSource?.followRedirects == true)
        assert(lastAdBreak?.adSource?.adTagURI?.templateType == "vast2")
        assert(lastAdBreak?.adSource?.adTagURI?.url == "//playertest.longtailvideo.com/adtags/postroll.xml")
    }

    /**
     * Test to validate a vmap response with an ad break which has no ad source tag but has tracking events.
     * According to the specifications, it is a valid case.
     */
    @Test
    fun testVMAPWithNoAdSourceButHasTrackingEvents() {
        val response = VMAPUtil.readVMAPWithNoAdSource()
        val parser = createVMAPParser(response)
        val parsedVMAPData = parser.parse(response)

        // verify the version is correct
        assert(parsedVMAPData?.version == "1.0")

        // verify it contains 1 ad breaks
        assert(parsedVMAPData?.adBreaks?.size == 1)

        // verify it contains no adsource
        assert(parsedVMAPData?.adBreaks?.get(0)?.adSource == null)

        // verify it contains tracking events
        assert(parsedVMAPData?.adBreaks?.get(0)?.trackingEvents?.size == 3)
    }

    /**
     * Test to validate a corrupt vmap xml. Expected to throw an IOException.
     */
    @Test
    fun testCorruptedVMAPResponse() {
        try {
            val response = VMAPUtil.readCorruptVMAP()
            val parser = createVMAPParser(response)
            val parsedVMAPData = parser.parse(response)

            assert(parsedVMAPData == null)
        } catch (e: ParserException) {
            assert(e.errorCode == ParserErrorCode.VMAP_PARSING_ERROR)
        }
    }

    /**
     * Test to validate vmap which contains a vast
     */
    @Test
    fun testVMAPWithVAST() {
        val response = VMAPUtil.readVMAP()

        val parser = createVMAPParser(response)
        val parsedVMAPData = parser.parse(response)

        assert(parsedVMAPData?.version == "1.0")
        assert(parsedVMAPData?.adBreaks?.size == 5)
        assert(parsedVMAPData?.adBreaks?.get(0)?.adSource?.vastAdData != null)
    }

    private fun createVMAPParser(xmlString: String): Parser<VMAPData> {
        val xmlPullParser = XmlParserHelper.createNewParser()
        xmlPullParser.setInput(StringReader(xmlString))
        xmlPullParser.nextTag()
        return VMAPParser(
            xmlPullParser,
            VASTParser(xmlPullParser)
        )
    }
}
