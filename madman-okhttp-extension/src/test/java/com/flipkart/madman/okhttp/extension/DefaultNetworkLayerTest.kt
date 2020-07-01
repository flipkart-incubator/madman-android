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

package com.flipkart.madman.okhttp.extension

import android.os.CancellationSignal
import com.flipkart.madman.network.NetworkListener
import com.flipkart.madman.network.model.NetworkAdRequest
import com.flipkart.madman.okhttp.extension.helper.MainThreadExecutor
import okhttp3.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.stubbing.Answer
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.IOException

/**
 * Test for [DefaultNetworkLayer]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class DefaultNetworkLayerTest {

    @Mock
    private lateinit var mockOkHttpClient: OkHttpClient

    @Mock
    private lateinit var mockOkHttpCall: Call

    @Mock
    private lateinit var mockCancellationSignal: CancellationSignal

    @Mock
    private lateinit var mockNetworkListener: NetworkListener<String>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Test to verify on error of [NetworkListener] is called when network call fails.
     */
    @Test
    fun testIfErrorIsCalledWhenNoNetwork() {
        val networkLayer = TestNetworkLayer()

        /** return mock call when new call is called on ok http client **/
        `when`(mockOkHttpClient.newCall(anyObject())).thenReturn(
            mockOkHttpCall
        )

        var answer = Answer { invocation ->
            val callback = invocation.getArgument<Callback>(0)
            // mimic the network call failure case
            callback.onFailure(mockOkHttpCall, IOException("invalid response"))
        }
        doAnswer(answer).`when`(mockOkHttpCall).enqueue(anyObject())

        networkLayer.fetch(
            NetworkAdRequest("").apply { url = "https://www.google.com" },
            mockNetworkListener,
            mockCancellationSignal
        )

        /** verify on error is called as network failed **/
        verify(mockNetworkListener, times(1))
            .onError(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString())

        /** verify on success is not called as network failed **/
        verify(mockNetworkListener, times(0))
            .onSuccess(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString())

        reset(mockOkHttpCall)
        reset(mockNetworkListener)

        answer = Answer { invocation ->
            val callback = invocation.getArgument<Callback>(0)
            // mimic the network call failure case
            callback.onResponse(
                mockOkHttpCall,
                Response.Builder().request(Request.Builder().url("https://www.google.com").build())
                    .protocol(
                        Protocol.HTTP_1_1
                    ).message("internal server").code(500).build()
            )
        }
        doAnswer(answer).`when`(mockOkHttpCall).enqueue(anyObject())

        networkLayer.fetch(
            NetworkAdRequest("").apply { url = "https://www.google.com" },
            mockNetworkListener,
            mockCancellationSignal
        )

        /** verify on error is called as network gave 500 status code **/
        verify(mockNetworkListener, times(1))
            .onError(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString())
    }

    /**
     * Test to verify on success of [NetworkListener] is called when network call succeeds.
     */
    @Test
    fun testIfSuccessIsCalledWithValidResponse() {
        val networkLayer = TestNetworkLayer()

        /** return mock call when new call is called on ok http client **/
        `when`(mockOkHttpClient.newCall(anyObject())).thenReturn(
            mockOkHttpCall
        )

        val answer = Answer { invocation ->
            val callback = invocation.getArgument<Callback>(0)
            // mimic the network call success case
            callback.onResponse(
                mockOkHttpCall,
                Response.Builder().request(Request.Builder().url("https://www.google.com").build())
                    .protocol(
                        Protocol.HTTP_1_1
                    ).message("valid").code(202).build()
            )
        }
        doAnswer(answer).`when`(mockOkHttpCall).enqueue(anyObject())

        networkLayer.fetch(
            NetworkAdRequest("").apply { url = "https://www.google.com" },
            mockNetworkListener,
            mockCancellationSignal
        )

        /** verify on success is called as network succeeded **/
        verify(mockNetworkListener, times(1))
            .onSuccess(ArgumentMatchers.eq(202), ArgumentMatchers.eq(null))
    }

    inner class TestNetworkLayer :
        com.flipkart.madman.okhttp.extension.DefaultNetworkLayer(
            RuntimeEnvironment.application,
            Builder().setMainThreadExecutor(MainThreadExecutor()).setVastTimeout(5000L)
        ) {
        override fun createOkHttpClient(): OkHttpClient {
            return mockOkHttpClient
        }
    }

    private fun <T> anyObject(): T {
        Mockito.anyObject<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T
}
