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

import com.flipkart.madman.provider.AdProgressProvider
import com.flipkart.madman.provider.ContentProgressProvider
import com.flipkart.madman.provider.Progress
import com.flipkart.madman.testutils.anyObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test for [ContentProgressHandler]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class ContentProgressHandlerTest {

    @Mock
    private lateinit var adProgressProvider: AdProgressProvider

    @Mock
    private lateinit var contentProgressProvider: ContentProgressProvider

    @Mock
    private lateinit var progressUpdateListener: ContentProgressUpdateListener

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Test listeners gets called
     */
    @Test
    fun testListenerIsCalledOnProgressUpdate() {
        val progressHandler = ProgressHandler(contentProgressProvider, adProgressProvider, null)
        progressHandler.setContentProgressListener(progressUpdateListener)

        Mockito.`when`(contentProgressProvider.getContentProgress()).thenReturn(Progress(10, 200))

        progressHandler.sendMessageFor(ProgressHandler.MessageCode.CONTENT_MESSAGE)
        /** verify listener is called once **/
        Mockito.verify(progressUpdateListener, Mockito.times(1))
            .onContentProgressUpdate(anyObject())

        Mockito.reset(progressUpdateListener)

        progressHandler.sendMessageFor(ProgressHandler.MessageCode.CONTENT_MESSAGE)
        progressHandler.sendMessageFor(ProgressHandler.MessageCode.CONTENT_MESSAGE)
        progressHandler.sendMessageFor(ProgressHandler.MessageCode.CONTENT_MESSAGE)
        /** verify listener is called thrice **/
        Mockito.verify(progressUpdateListener, Mockito.times(3))
            .onContentProgressUpdate(anyObject())

        Mockito.reset(progressUpdateListener)

        progressHandler.removeContentProgressListeners()
        progressHandler.sendMessageFor(ProgressHandler.MessageCode.CONTENT_MESSAGE)
        /** verify listener is not called as not listeners attached **/
        Mockito.verify(progressUpdateListener, Mockito.times(0))
            .onContentProgressUpdate(anyObject())

        Mockito.reset(progressUpdateListener)

        progressHandler.setContentProgressListener(progressUpdateListener)
        progressHandler.removeMessagesFor(ProgressHandler.MessageCode.CONTENT_MESSAGE)
        progressHandler.sendMessageFor(ProgressHandler.MessageCode.CONTENT_MESSAGE)

        /** verify listener is called once **/
        Mockito.verify(progressUpdateListener, Mockito.times(1))
            .onContentProgressUpdate(anyObject())

        Mockito.reset(progressUpdateListener)

        progressHandler.sendMessageFor(ProgressHandler.MessageCode.CONTENT_MESSAGE)
        progressHandler.removeMessagesFor(ProgressHandler.MessageCode.CONTENT_MESSAGE)
        /** verify listener is called once **/
        Mockito.verify(progressUpdateListener, Mockito.times(1))
            .onContentProgressUpdate(anyObject())
    }
}
