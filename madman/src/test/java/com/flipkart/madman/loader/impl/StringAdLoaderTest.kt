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
import com.flipkart.madman.network.model.StringAdRequest
import com.flipkart.madman.parser.XmlParser
import com.flipkart.madman.testutils.CurrentThreadExecutor
import com.flipkart.madman.testutils.VMAPUtil
import com.flipkart.madman.testutils.anyObject
import com.flipkart.madman.validator.DefaultXmlValidator
import com.flipkart.madman.validator.XmlValidator
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


/**
 * Test for [StringAdLoader]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class StringAdLoaderTest {

    private val handler = Handler()
    private val executor = CurrentThreadExecutor()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Test for [StringAdRequest] request ad with given response (response could be valid or invalid)
     */
    @Test
    fun testRequestAdsWithSomeResponse() {
        val mockParserBuilder = Mockito.spy(XmlParser.Builder::class.java)
        val mockParser = mockParserBuilder.build(handler, executor)

        val mockValidator = DefaultXmlValidator()
        val loader = StringAdLoader(mockParser, mockValidator)

        loader.requestAds(StringAdRequest("").apply { response = "<VMAP></VMAP>" }, {

        }, { _: AdErrorType, message: String? ->
            // since response is invalid
            assert(message != null)
        })

        loader.requestAds(StringAdRequest("").apply { response = VMAPUtil.readVMAPWithPreRoll() }, {

        }, { _: AdErrorType, message: String? ->
            // since response is valid
            assert(message == null)
        })
    }

    /**
     * Test for [StringAdRequest] request ad with no response
     */
    @Test
    fun testRequestAdsWithNoResponse() {
        val mockParser = Mockito.mock(XmlParser::class.java)
        val mockValidator = Mockito.mock(XmlValidator::class.java)
        val loader = StringAdLoader(mockParser, mockValidator)

        loader.requestAds(StringAdRequest("").apply { response = null }, {

        }, { _: AdErrorType, message: String? ->
            assert(message != null)
        })

        // makes sure parse method is not called
        Mockito.verify(mockParser, times(0))
            .parse(
                anyString(),
                anyObject()
            )
    }

    /**
     * Test for [StringAdRequest] request ad with validation passing and failing
     */
    @Test
    fun testRequestAdWhenValidationPassesAndFails() {
        val mockValidator = Mockito.mock(XmlValidator::class.java)
        val parser = XmlParser.Builder().build(
            handler,
            executor
        )
        val loader = StringAdLoader(parser, mockValidator)

        val vmap = VMAPUtil.readVMAPWithPreRoll()

        // when ever validateVMAP gets called, return a valid result
        Mockito.`when`(mockValidator.validateVMAP(anyObject())).thenReturn(object :
            XmlValidator.Result {
            override fun isValid(): Boolean {
                return true
            }

            override fun getMessage(): String? {
                return null
            }
        })

        loader.requestAds(StringAdRequest("").apply { response = vmap }, {
            // verify vmap is valid
            assert(it.version == "1.0")
        }, { _: AdErrorType, message: String? ->
            // verify message is null
            assert(message == null)
        })

        // verify validateVMAP is called once
        Mockito.verify(mockValidator, times(1)).validateVMAP(anyObject())

        reset(mockValidator)

        // when ever validateVMAP gets called, return a valid result
        Mockito.`when`(mockValidator.validateVMAP(anyObject())).thenReturn(object :
            XmlValidator.Result {
            override fun isValid(): Boolean {
                return false
            }

            override fun getMessage(): String? {
                return "VMAP is not valid"
            }
        })

        loader.requestAds(StringAdRequest("").apply { response = vmap }, {
        }, { _: AdErrorType, message: String? ->
            // verify message is not null
            assert(message == "VMAP is not valid")
        })

        // verify validateVMAP is called once
        Mockito.verify(mockValidator, times(1)).validateVMAP(anyObject())
    }

    /**
     * Test for [StringAdRequest] request ad with a corrupt vmap
     */
    @Test
    fun testRequestAdWhenCorruptVMAPReceived() {
        val mockValidator = Mockito.mock(XmlValidator::class.java)
        val parser = XmlParser.Builder().build(
            handler,
            executor
        )
        val loader = StringAdLoader(parser, mockValidator)

        val vmap = VMAPUtil.readCorruptVMAP()

        loader.requestAds(StringAdRequest("").apply { response = vmap }, {
        }, { _: AdErrorType, message: String? ->
            // verify message is null
            assert(message != null)
        })

        // verify validateVMAP is not called once, since parsing has failed
        Mockito.verify(mockValidator, times(0)).validateVMAP(anyObject())
    }
}
