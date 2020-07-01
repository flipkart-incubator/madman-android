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

import com.flipkart.madman.testutils.VMAPUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test for [AdDataHelper]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class AdDataHelperTest {
    /**
     * test cue points are valid
     */
    @Test
    fun testGetCuePoints() {
        val vmapData = VMAPUtil.createVMAPWithAdTagURI()

        val cuePoints = AdDataHelper.getCuePoints(vmapData)

        /** asset cue points size **/
        assert(cuePoints.size == 3)
        assert(cuePoints[0] == 0F)
        assert(cuePoints[1] == 15F)
        assert(cuePoints[2] == -1F)
    }

    /**
     * test all ad breaks in vmap
     */
    @Test
    fun testGetAllAdBreaks() {
        val vmapData = VMAPUtil.createVMAPWithAdTagURI()
        val allAdBreaks = AdDataHelper.getAllAdBreaks(vmapData)

        /** asset ad breaks **/
        assert(allAdBreaks.size == 3)
    }

    /**
     * test if vmap has pre-rolls
     */
    @Test
    fun testIfVMAPHasPreRollAds() {
        val vmapData = VMAPUtil.createVMAPWithAdTagURI()
        val hasPreRollAds = AdDataHelper.hasPreRollAds(vmapData)

        /** has pre rolls **/
        assert(hasPreRollAds)
    }

    /**
     * test if vmap has only post roll ads
     */
    @Test
    fun testIfVMAPHasOnlyPostRollAds() {
        val vmapData = VMAPUtil.createVMAPWithOnlyPostRoll()
        val hasOnlyPostRollAds = AdDataHelper.hasOnlyPostRollAds(vmapData)
        assert(hasOnlyPostRollAds)
    }
}
