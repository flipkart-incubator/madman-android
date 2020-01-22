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

import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.model.vast.VASTData
import com.flipkart.madman.component.model.vmap.AdSource
import com.flipkart.madman.component.model.vmap.AdTagURI
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.testutils.VMAPUtil
import com.flipkart.madman.testutils.capture
import com.flipkart.madman.loader.AdLoader
import com.flipkart.madman.manager.data.VastAdProvider
import com.flipkart.madman.network.model.NetworkAdRequest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks
import org.mockito.stubbing.Answer
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test for [NetworkVastAdProvider]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class NetworkVastAdProviderTest {

    @Captor
    private lateinit var errorCaptor: ArgumentCaptor<String>

    @Captor
    private lateinit var vastDataCaptor: ArgumentCaptor<VASTData>

    @Mock
    private lateinit var mockAdLoader: AdLoader<NetworkAdRequest>

    @Mock
    private lateinit var mockListener: VastAdProvider.Listener

    @Before
    fun setUp() {
        initMocks(this)
    }

    /**
     * Test to verify error is thrown if url is null
     */
    @Test
    fun testErrorIsThrownWhenURLisNull() {
        val networkVastAdProvider = NetworkVastAdProvider(mockAdLoader)

        val adBreak = VMAPUtil.createAdBreak("linear", "1")
        adBreak.adSource?.adTagURI = null

        /** cal getVastAd will null ad tag uri **/
        networkVastAdProvider.getVASTAd(adBreak, mockListener)
        /** verify error callback is called once **/
        verify(mockListener, times(1)).onVastFetchError(
            com.flipkart.madman.testutils.anyObject(),
            anyString()
        )

        reset(mockListener)

        adBreak.adSource = AdSource().apply {
            adTagURI = AdTagURI().apply {
                url = null
            }
        }

        /** cal getVastAd will null url **/
        networkVastAdProvider.getVASTAd(adBreak, mockListener)
        /** verify error callback is called once **/
        verify(mockListener, times(1)).onVastFetchError(
            com.flipkart.madman.testutils.anyObject(),
            anyString()
        )
    }

    /**
     * Test if ad loader's requestAd is called when a url is present
     */
    @Test
    fun testAdLoaderIsCalledWithValidURL() {
        val networkVastAdProvider = NetworkVastAdProvider(mockAdLoader)

        val adBreak = VMAPUtil.createAdBreak("linear", "1")
        adBreak.adSource = AdSource().apply {
            adTagURI = AdTagURI().apply {
                url = "test"
            }
        }

        /** cal getVastAd will given url **/
        networkVastAdProvider.getVASTAd(adBreak, mockListener)

        /** verify ad loader is called **/
        verify(mockAdLoader, times(1)).requestAds(
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject()
        )
    }

    /**
     * Test to verify error is thrown when the network call fails with the
     * appropriate error message
     */
    @Test
    fun testErrorIsThrownWhenNetworkFails() {
        val networkVastAdProvider = NetworkVastAdProvider(mockAdLoader)

        val adBreak = VMAPUtil.createAdBreak("linear", "1")
        adBreak.adSource = AdSource().apply {
            adTagURI = AdTagURI().apply {
                url = "test"
            }
        }

        val answer = Answer { invocation ->
            val listener =
                invocation.getArgument<(errorType: AdErrorType, message: String?) -> Unit>(2)
            /** return error when request ad gets called **/
            listener(AdErrorType.AD_REQUEST_NETWORK_FAILURE, "network failure")
        }
        doAnswer(answer).`when`(mockAdLoader).requestAds(
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject()
        )

        /** call getVastAd will given url **/
        networkVastAdProvider.getVASTAd(adBreak, mockListener)

        /** verify error callback is called once **/
        verify(mockListener, times(1)).onVastFetchError(
            com.flipkart.madman.testutils.anyObject(),
            capture(errorCaptor)
        )

        /** verify the error message **/
        assert(errorCaptor.value != null)
    }

    /**
     * Test to verify verify is called when the network call succeeds with a valid [VASTData] but with no ads
     */
    @Test
    fun testErrorIsCalledWhenNetworkSucceedsWithValidVASTWithNoAds() {
        val networkVastAdProvider = NetworkVastAdProvider(mockAdLoader)

        val adBreak = VMAPUtil.createAdBreak("linear", "1")
        adBreak.adSource = AdSource().apply {
            adTagURI = AdTagURI().apply {
                url = "test"
            }
        }

        val response = VMAPUtil.createVMAPWithNoAds()
        val answer = Answer { invocation ->
            val listener =
                invocation.getArgument<(data: VMAPData) -> Unit>(1)
            /** call success when request ad gets called **/
            listener(response)
        }
        doAnswer(answer).`when`(mockAdLoader).requestAds(
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject()
        )

        /** call getVastAd will given url **/
        networkVastAdProvider.getVASTAd(adBreak, mockListener)

        /** verify error callback is called **/
        verify(mockListener, times(1)).onVastFetchError(
            com.flipkart.madman.testutils.anyObject(),
            capture(errorCaptor)
        )

        /** verify success callback is not called **/
        verify(mockListener, times(0)).onVastFetchSuccess(com.flipkart.madman.testutils.anyObject())

        /** verify the error is present **/
        assert(errorCaptor.value != null)
    }

    /**
     * Test to verify success is called when the network call succeeds with no [VASTData]
     */
    @Test
    fun testSuccessIsCalledWhenNetworkSucceedsWithNoVAST() {
        val networkVastAdProvider = NetworkVastAdProvider(mockAdLoader)

        val adBreak = VMAPUtil.createAdBreak("linear", "1")
        adBreak.adSource = AdSource().apply {
            adTagURI = AdTagURI().apply {
                url = "test"
            }
        }

        val response = VMAPUtil.createVMAP("3.0", null)
        val answer = Answer { invocation ->
            val listener =
                invocation.getArgument<(data: VMAPData) -> Unit>(1)
            /** call success when request ad gets called **/
            listener(response)
        }
        doAnswer(answer).`when`(mockAdLoader).requestAds(
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject()
        )

        /** call getVastAd will given url **/
        networkVastAdProvider.getVASTAd(adBreak, mockListener)

        /** verify error callback is called **/
        verify(mockListener, times(1)).onVastFetchError(
            com.flipkart.madman.testutils.anyObject(),
            capture(errorCaptor)
        )

        /** verify success callback is not called **/
        verify(mockListener, times(0)).onVastFetchSuccess(
            capture(
                vastDataCaptor
            )
        )

        /** verify the error is present **/
        assert(errorCaptor.value != null)
    }
}
