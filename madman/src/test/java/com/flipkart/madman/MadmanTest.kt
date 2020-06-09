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
package com.flipkart.madman

import android.content.Context
import android.os.Handler
import com.flipkart.madman.listener.AdLoadListener
import com.flipkart.madman.network.NetworkLayer
import com.flipkart.madman.network.model.StringAdRequest
import com.flipkart.madman.parser.XmlParser
import com.flipkart.madman.renderer.AdRenderer
import com.flipkart.madman.testutils.CurrentThreadExecutor
import com.flipkart.madman.testutils.VMAPUtil
import com.flipkart.madman.testutils.anyObject
import com.flipkart.madman.validator.DefaultXmlValidator
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test for [DefaultXmlValidator]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class MadmanTest {

    @Mock
    private lateinit var mockAdLoadListener: AdLoadListener

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockAdRenderer: AdRenderer

    @Mock
    private lateinit var mockNetworkLayer: NetworkLayer

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Test to verify that [AdLoadListener onAdManagerLoaded] is called for a valid request
     */
    @Test
    fun `testToVerifyAdLoadListeners's'OnAdManagerLoadedIsCalledOnValidRequest`() {
        val mockParserBuilder = Mockito.spy(XmlParser.Builder::class.java)
        val mockParser = mockParserBuilder.build(Handler(), CurrentThreadExecutor())
        val validVMAP = VMAPUtil.readVMAPWithPreRoll()

        val madman = Madman.Builder()
            .setNetworkLayer(mockNetworkLayer)
            .setXmlParser(mockParser)
            .setAdLoadListener(mockAdLoadListener)
            .build(mockContext)

        madman.requestAds(StringAdRequest(validVMAP), mockAdRenderer)
        /** verify the listener is called once **/
        Mockito.verify(mockAdLoadListener, times(1)).onAdManagerLoaded(anyObject())
        reset(mockAdLoadListener)

        madman.addAdLoadListener(mockAdLoadListener)
        madman.addAdLoadListener(mockAdLoadListener)

        madman.requestAds(StringAdRequest(validVMAP), mockAdRenderer)
        /** verify the listener is called thrice **/
        Mockito.verify(mockAdLoadListener, times(3)).onAdManagerLoaded(anyObject())
        reset(mockAdLoadListener)

        madman.removeAdLoadListener(mockAdLoadListener)

        madman.requestAds(StringAdRequest(validVMAP), mockAdRenderer)
        /** verify the listener is called twice **/
        Mockito.verify(mockAdLoadListener, times(2)).onAdManagerLoaded(anyObject())
        reset(mockAdLoadListener)

        madman.removeAdLoadListener(mockAdLoadListener)
        madman.removeAdLoadListener(mockAdLoadListener)

        madman.requestAds(StringAdRequest(validVMAP), mockAdRenderer)
        /** verify the listener is not called as no listeners attached **/
        Mockito.verify(mockAdLoadListener, times(0)).onAdManagerLoaded(anyObject())
        reset(mockAdLoadListener)
    }

    /**
     * Test to verify that [AdLoadListener onAdManagerLoadFailed] is called for a in valid request
     */
    @Test
    fun `testToVerifyAdLoadListeners's'OnAdManagerLoadFailedIsCalledOnValidRequest`() {
        val mockParserBuilder = Mockito.spy(XmlParser.Builder::class.java)
        val mockParser = mockParserBuilder.build(Handler(), CurrentThreadExecutor())
        val corruptVMAP = VMAPUtil.readCorruptVMAP()

        val madman = Madman.Builder()
            .setNetworkLayer(mockNetworkLayer)
            .setXmlParser(mockParser)
            .setAdLoadListener(mockAdLoadListener)
            .build(mockContext)

        madman.requestAds(StringAdRequest(corruptVMAP), mockAdRenderer)
        /** verify the listener is called once **/
        Mockito.verify(mockAdLoadListener, times(1)).onAdManagerLoadFailed(anyObject())
        reset(mockAdLoadListener)

        madman.addAdLoadListener(mockAdLoadListener)
        madman.addAdLoadListener(mockAdLoadListener)

        madman.requestAds(StringAdRequest(corruptVMAP), mockAdRenderer)
        /** verify the listener is called thrice **/
        Mockito.verify(mockAdLoadListener, times(3)).onAdManagerLoadFailed(anyObject())
        reset(mockAdLoadListener)

        madman.removeAdLoadListener(mockAdLoadListener)

        madman.requestAds(StringAdRequest(corruptVMAP), mockAdRenderer)
        /** verify the listener is called twice **/
        Mockito.verify(mockAdLoadListener, times(2)).onAdManagerLoadFailed(anyObject())
        reset(mockAdLoadListener)

        madman.removeAdLoadListener(mockAdLoadListener)
        madman.removeAdLoadListener(mockAdLoadListener)

        madman.requestAds(StringAdRequest(corruptVMAP), mockAdRenderer)
        /** verify the listener is not called as no listeners attached **/
        Mockito.verify(mockAdLoadListener, times(0)).onAdManagerLoadFailed(anyObject())
        reset(mockAdLoadListener)
    }
}
