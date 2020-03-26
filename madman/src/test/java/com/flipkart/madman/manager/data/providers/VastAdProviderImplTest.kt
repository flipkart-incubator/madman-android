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

package com.flipkart.madman.manager.data.providers

import com.flipkart.madman.component.model.vast.VASTData
import com.flipkart.madman.component.model.vmap.AdSource
import com.flipkart.madman.component.model.vmap.AdTagURI
import com.flipkart.madman.manager.data.VastAdProvider
import com.flipkart.madman.testutils.VMAPUtil
import com.flipkart.madman.testutils.anyObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test for [VastAdProviderImpl]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class VastAdProviderImplTest {
    @Mock
    private lateinit var stringVastAdProvider: StringVastAdProvider

    @Mock
    private lateinit var networkVastAdProvider: NetworkVastAdProvider

    @Mock
    private lateinit var mockListener: VastAdProvider.Listener

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Test if correct providers are called for inline and remote vast
     */
    @Test
    fun testIfCorrectProvidersAreCalled() {
        val vastAdProvider = VastAdProviderImpl(stringVastAdProvider, networkVastAdProvider)

        /** ad break with vast inline */
        val adBreak = VMAPUtil.createAdBreak("linear", "1")
        adBreak.adSource = AdSource().apply {
            vastAdData = VASTData().apply {
                version = "3.0"
            }
        }

        vastAdProvider.getVASTAd(adBreak, mockListener)

        /** verify get vast ad is called on string vast ad provider **/
        verify(stringVastAdProvider, times(1)).getVASTAd(
            anyObject(),
            anyObject()
        )

        /** ad break with url */
        val adBreakWithUrl = VMAPUtil.createAdBreak("linear", "1")
        adBreakWithUrl.adSource = AdSource().apply {
            adTagURI = AdTagURI().apply {
                url = "test"
            }
        }

        vastAdProvider.getVASTAd(adBreakWithUrl, mockListener)

        /** verify get vast ad is called on network vast ad provider **/
        verify(networkVastAdProvider, times(1)).getVASTAd(
            anyObject(),
            anyObject()
        )

        /** ad break with url */
        val adBreakWithNoAdSource = VMAPUtil.createAdBreak("linear", "1")

        vastAdProvider.getVASTAd(adBreakWithNoAdSource, mockListener)

        /** verify error is called if no ad source present **/
        verify(mockListener, times(1)).onVastFetchError(anyObject(), ArgumentMatchers.anyString())
    }
}
