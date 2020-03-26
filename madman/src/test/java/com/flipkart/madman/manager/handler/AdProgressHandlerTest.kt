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

package com.flipkart.madman.manager.handler

import com.flipkart.madman.testutils.anyObject
import com.flipkart.madman.provider.AdProgressProvider
import com.flipkart.madman.provider.Progress
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


/**
 * Test for [AdProgressHandler]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class AdProgressHandlerTest {

    @Mock
    private lateinit var adProgressProvider: AdProgressProvider

    @Mock
    private lateinit var progressUpdateListener: AdProgressHandler.AdProgressUpdateListener

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Test listeners gets called
     */
    @Test
    fun testListenerIsCalledOnProgressUpdate() {
        val progressHandler = AdProgressHandler(adProgressProvider)
        progressHandler.setListener(progressUpdateListener)

        `when`(adProgressProvider.getAdProgress()).thenReturn(Progress(10, 200))

        progressHandler.sendMessage()
        /** verify listener is called once **/
        verify(progressUpdateListener, times(1)).onAdProgressUpdate(anyObject())

        reset(progressUpdateListener)

        progressHandler.sendMessage()
        progressHandler.sendMessage()
        progressHandler.sendMessage()
        /** verify listener is called thrice **/
        verify(progressUpdateListener, times(3)).onAdProgressUpdate(anyObject())

        reset(progressUpdateListener)

        progressHandler.removeListener(progressUpdateListener)
        progressHandler.sendMessage()
        /** verify listener is not called as not listeners attached **/
        verify(progressUpdateListener, times(0)).onAdProgressUpdate(anyObject())

        reset(progressUpdateListener)

        progressHandler.setListener(progressUpdateListener)
        progressHandler.removeMessages()
        progressHandler.sendMessage()
        /** verify listener is called once **/
        verify(progressUpdateListener, times(1)).onAdProgressUpdate(anyObject())

        reset(progressUpdateListener)

        progressHandler.sendMessage()
        progressHandler.removeMessages()
        /** verify listener is called once **/
        verify(progressUpdateListener, times(1)).onAdProgressUpdate(anyObject())
    }
}
