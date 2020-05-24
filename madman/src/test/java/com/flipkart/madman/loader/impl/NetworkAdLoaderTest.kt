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

package com.flipkart.madman.loader.impl

import android.os.Handler
import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.network.NetworkLayer
import com.flipkart.madman.network.NetworkListener
import com.flipkart.madman.network.model.NetworkAdRequest
import com.flipkart.madman.parser.XmlParser
import com.flipkart.madman.testutils.CurrentThreadExecutor
import com.flipkart.madman.testutils.VMAPUtil
import com.flipkart.madman.validator.DefaultXmlValidator
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.stubbing.Answer
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test for [NetworkAdLoader]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class NetworkAdLoaderTest {

    @Mock
    private lateinit var mockNetworkLayer: NetworkLayer

    private val handler = Handler()

    private val executor = CurrentThreadExecutor()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Verify fetch is called once on the network layer
     */
    @Test
    fun testNetworkLayerFetchIsCalled() {
        val loader = NetworkAdLoader(
            mockNetworkLayer,
            XmlParser.Builder().build(handler, executor),
            DefaultXmlValidator()
        )

        loader.requestAds(NetworkAdRequest("").apply { url = "someurl" }, {

        }, { _: AdErrorType, _: String? ->
        })

        // verify fetch is called once on the network layer
        verify(mockNetworkLayer, times(1))
            .fetch(
                com.flipkart.madman.testutils.anyObject(),
                com.flipkart.madman.testutils.anyObject(),
                com.flipkart.madman.testutils.anyObject()
            )
    }

    /**
     * Verify that error is thrown when network call fails
     */
    @Test
    fun testWhenNetworkLayerReturnFailure() {
        val loader = NetworkAdLoader(
            mockNetworkLayer,
            XmlParser.Builder().build(handler, executor),
            DefaultXmlValidator()
        )

        val answer = Answer { invocation ->
            val listener = invocation.getArgument<NetworkListener<String>>(1)
            // mimic the network call failure case
            listener.onError(500, "Failed")
        }
        doAnswer(answer).`when`(mockNetworkLayer).fetch(
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject()
        )

        val request = NetworkAdRequest("").apply { url = "someurl" }
        loader.requestAds(request, {

        }, { _: AdErrorType, message: String? ->
            // since network call failed
            assert(message != null)
            assert(message == "Failed")
        })
    }

    /**
     * Verify the behaviour is thrown when network call succeeds
     */
    @Test
    fun testWhenNetworkLayerReturnSuccess() {
        val loader = NetworkAdLoader(
            mockNetworkLayer,
            XmlParser.Builder().build(handler, executor),
            DefaultXmlValidator()
        )

        var answer = Answer { invocation ->
            val listener = invocation.getArgument<NetworkListener<String>>(1)
            // return success with empty response, should throw error
            listener.onSuccess(200, null)
        }
        doAnswer(answer).`when`(mockNetworkLayer).fetch(
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject()
        )

        val request = NetworkAdRequest("").apply { url = "someurl" }
        loader.requestAds(request, {

        }, { _: AdErrorType, message: String? ->
            // since network call was successful, but gave empty response
            assert(message != null)
        })

        reset(mockNetworkLayer)

        answer = Answer { invocation ->
            val listener = invocation.getArgument<NetworkListener<String>>(1)
            // return success with valid response
            listener.onSuccess(200, VMAPUtil.readVMAPWithPreRoll())
        }
        doAnswer(answer).`when`(mockNetworkLayer).fetch(
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject(),
            com.flipkart.madman.testutils.anyObject()
        )

        loader.requestAds(request, {

        }, { _: AdErrorType, message: String? ->
            // since network call was successful
            assert(message == null)
        })
    }
}
