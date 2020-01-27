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

package com.flipkart.madman.manager.state

import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.logger.LogUtil
import com.flipkart.madman.logger.Logger
import com.flipkart.madman.manager.finder.DefaultAdBreakFinder
import com.flipkart.madman.parser.Parser
import com.flipkart.madman.parser.VASTParser
import com.flipkart.madman.parser.VMAPParser
import com.flipkart.madman.parser.helper.XmlParserHelper
import com.flipkart.madman.testutils.VMAPUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.StringReader

/**
 * Test for [AdPlaybackState]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class AdPlaybackStateTest {

    /**
     * Test the algorithm of fetching the next ad break to play
     * depending upon the current position of the media
     */
    @Test
    fun testPlayableAdBreak() {
        LogUtil.setLogger(Mockito.mock(Logger::class.java))

        val duration = 888F
        val defaultAdBreakFinder = DefaultAdBreakFinder()
        val response = VMAPUtil.readVMAPWithPreRoll()
        val parser = createVMAPParser(response)

        /** it contains 3 ad breaks, one pre-roll, one at 15 and post roll **/
        val parsedVMAPData = parser.parse(response)

        parsedVMAPData?.let {
            val adPlaybackState = AdPlaybackState()
            adPlaybackState.withData(parsedVMAPData)
            adPlaybackState.withContentProgress(1F, duration)

            /** get ad break to be played at 0 progress of media **/
            var playableAdBreak =
                adPlaybackState.getPlayableAdBreak(0F, duration, defaultAdBreakFinder)

            /**
             * since the vmap consists of a pre-roll, the playable ad break should be the pre-roll
             */
            assert(playableAdBreak == parsedVMAPData.adBreaks?.get(0))
            assert(playableAdBreak?.timeOffsetInSec == 0F)

            /** since the ad break is played, mark it as played **/
            adPlaybackState.updateStateForAdBreak(AdBreak.AdBreakState.PLAYED)
            adPlaybackState.markAdBreakAsCompleted()

            /**
             * now since the pre-roll has already been played, there should be no ad breaks
             * to be played when the current position of the media is 0
             */
            playableAdBreak = adPlaybackState.getPlayableAdBreak(0F, duration, defaultAdBreakFinder)
            assert(playableAdBreak == null)

            /** now assume the user has scrubbed to 20 seconds **/
            playableAdBreak =
                adPlaybackState.getPlayableAdBreak(20F, duration, defaultAdBreakFinder)

            /**
             * since there is an ad break at 15 seconds which is not played yet
             * it should return the 2nd ad break
             */
            assert(playableAdBreak == parsedVMAPData.adBreaks?.get(1))
            assert(playableAdBreak?.timeOffsetInSec == 15F)

            /** since the ad break is played, mark it as played **/
            adPlaybackState.updateStateForAdBreak(AdBreak.AdBreakState.PLAYED)
            adPlaybackState.markAdBreakAsCompleted()

            /** now assume the user has scrubbed to 30 seconds **/
            playableAdBreak =
                adPlaybackState.getPlayableAdBreak(30F, duration, defaultAdBreakFinder)

            /**
             * since there is an ad break at 15 seconds but is already played, it should return the 3rd ad break
             */
            assert(playableAdBreak == parsedVMAPData.adBreaks?.get(2))

            /** since the ad break is played, mark it as played **/
            adPlaybackState.updateStateForAdBreak(AdBreak.AdBreakState.PLAYED)
            adPlaybackState.markAdBreakAsCompleted()

            /** now assume the user has scrubbed back to 10 seconds **/
            playableAdBreak =
                adPlaybackState.getPlayableAdBreak(10F, duration, defaultAdBreakFinder)

            /**
             * since there is only pre-roll before 10 position which is already played, it should return null
             */
            assert(playableAdBreak == null)
        } ?: run {
            /** fail test if vmap is null **/
            assert(false)
        }
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
