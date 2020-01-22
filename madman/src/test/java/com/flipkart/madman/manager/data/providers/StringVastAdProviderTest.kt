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

package com.flipkart.madman.manager.data.providers

import com.flipkart.madman.component.model.vast.VASTData
import com.flipkart.madman.component.model.vmap.AdSource
import com.flipkart.madman.testutils.VMAPUtil
import com.flipkart.madman.testutils.anyObject
import com.flipkart.madman.testutils.capture
import com.flipkart.madman.manager.data.VastAdProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test for [StringVastAdProvider]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class StringVastAdProviderTest {
    @Captor
    private lateinit var errorCaptor: ArgumentCaptor<String>

    @Captor
    private lateinit var vastDataCaptor: ArgumentCaptor<VASTData>

    @Mock
    private lateinit var mockListener: VastAdProvider.Listener

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Test if error is thrown when vo [VASTData] is present in the given ad break
     */
    @Test
    fun testErrorIsThrownIfNoVASTPresent() {
        val stringVastAdProvider = StringVastAdProvider()

        val adBreak = VMAPUtil.createAdBreak("linear", "1")
        adBreak.adSource = AdSource().apply {
            vastAdData = null
        }

        /** call get vast ad **/
        stringVastAdProvider.getVASTAd(adBreak, mockListener)

        /** verify error callback is called once **/
        verify(mockListener, times(1)).onVastFetchError(
            anyObject(),
            capture(errorCaptor)
        )

        /** verify the error message **/
        assert(errorCaptor.value == "No vast ad to play for $adBreak")
    }

    /**
     * Test if error is thrown when vo [VASTData] is present in the given ad break
     */
    @Test
    fun testErrorIsCalledIfVASTPresentWithNoAds() {
        val stringVastAdProvider = StringVastAdProvider()

        val adBreak = VMAPUtil.createAdBreak("linear", "1")
        adBreak.adSource = AdSource().apply {
            vastAdData = VASTData().apply {
                version = "3.0"
            }
        }

        /** call get vast ad **/
        stringVastAdProvider.getVASTAd(adBreak, mockListener)

        /** verify error callback is called **/
        verify(mockListener, times(1)).onVastFetchError(
            anyObject(),
            capture(errorCaptor)
        )

        /** verify success callback is not called **/
        verify(mockListener, times(0)).onVastFetchSuccess(anyObject())

        /** verify the error is present **/
        assert(errorCaptor.value != null)
    }
}
