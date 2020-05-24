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

package com.flipkart.madman.manager

import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.enums.AdEventType
import com.flipkart.madman.component.model.vast.VASTData
import com.flipkart.madman.listener.AdErrorListener
import com.flipkart.madman.listener.AdEventListener
import com.flipkart.madman.listener.impl.AdError
import com.flipkart.madman.listener.impl.AdEvent
import com.flipkart.madman.manager.data.VastAdProvider
import com.flipkart.madman.manager.data.providers.NetworkVastAdProvider
import com.flipkart.madman.manager.data.providers.StringVastAdProvider
import com.flipkart.madman.manager.data.providers.VastAdProviderImpl
import com.flipkart.madman.manager.finder.DefaultAdBreakFinder
import com.flipkart.madman.network.NetworkLayer
import com.flipkart.madman.parser.XmlParser
import com.flipkart.madman.provider.ContentProgressProvider
import com.flipkart.madman.provider.Progress
import com.flipkart.madman.renderer.AdRenderer
import com.flipkart.madman.renderer.player.AdPlayer
import com.flipkart.madman.renderer.settings.DefaultRenderingSettings
import com.flipkart.madman.testutils.VMAPUtil
import com.flipkart.madman.testutils.anyObject
import com.flipkart.madman.testutils.capture
import com.flipkart.madman.validator.XmlValidator
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.Mockito.*
import org.mockito.stubbing.Answer
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

/**
 * Test for [AdManager]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class DefaultAdManagerTest {

    @Mock
    private lateinit var mockNetworkLayer: NetworkLayer

    @Mock
    private lateinit var mockXmlParser: XmlParser

    @Mock
    private lateinit var mockXmlValidator: XmlValidator

    @Mock
    private lateinit var mockAdEventListener: AdEventListener

    @Mock
    private lateinit var mockAdErrorListener: AdErrorListener

    @Mock
    private lateinit var mockNetworkVastAdProvider: NetworkVastAdProvider

    @Mock
    private lateinit var mockStringVastAdProvider: StringVastAdProvider

    @Spy
    private lateinit var mockAdPlayer: AdPlayer

    @Spy
    private lateinit var mockAdRenderer: AdRenderer

    @Spy
    private lateinit var mockContentProgressProvider: ContentProgressProvider

    @Captor
    private lateinit var adEventCaptor: ArgumentCaptor<AdEvent>

    @Captor
    private lateinit var adErrorCaptor: ArgumentCaptor<AdError>

    private var adManager: DefaultAdManager? = null

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        /** return undefined progress in start **/
        `when`(mockContentProgressProvider.getContentProgress()).thenReturn(Progress.UNDEFINED)

        /** return undefined progress in start **/
        `when`(mockAdPlayer.getAdProgress()).thenReturn(Progress.UNDEFINED)

        /** return default rendering settings **/
        `when`(mockAdRenderer.getRenderingSettings()).thenReturn(DefaultRenderingSettings())

        /** return default rendering settings **/
        `when`(mockAdRenderer.getAdPlayer()).thenReturn(mockAdPlayer)

        val vmap = VMAPUtil.createVMAP(true)
        adManager = DefaultAdManager(
            vmap,
            mockNetworkLayer,
            mockXmlParser,
            mockXmlValidator,
            mockAdRenderer
        )
        adManager?.addAdErrorListener(mockAdErrorListener)
        adManager?.addAdEventListener(mockAdEventListener)
    }

    @After
    fun tearDown() {
        adManager?.destroy()
    }

    /**
     * Test to verify the behaviour with and without pre-roll present.
     * Expectation: CONTENT_RESUME_REQUESTED event should be fired when no pre-roll ad to play, else not
     */
    @Test
    fun testBehaviourWithAndWithoutPreRoll() {
        var vmap = VMAPUtil.createVMAP(false)

        adManager = DefaultAdManager(
            vmap,
            mockNetworkLayer,
            mockXmlParser,
            mockXmlValidator,
            mockAdRenderer
        )

        adManager?.addAdErrorListener(mockAdErrorListener)
        adManager?.addAdEventListener(mockAdEventListener)
        /** initialise the ad manager **/
        adManager?.init(mockContentProgressProvider)

        /** ad event listener is called with content resume requested as no pre-roll is present **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.CONTENT_RESUME_REQUESTED)
        reset(mockAdEventListener)

        vmap = VMAPUtil.createVMAP(true)

        adManager = DefaultAdManager(
            vmap,
            mockNetworkLayer,
            mockXmlParser,
            mockXmlValidator,
            mockAdRenderer
        )

        adManager?.addAdErrorListener(mockAdErrorListener)
        adManager?.addAdEventListener(mockAdEventListener)
        /** initialise the ad manager **/
        adManager?.init(mockContentProgressProvider)

        /** ad event listener is called with loaded event as pre-roll is present **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.LOADED)
    }

    /**
     * Test to verify the behaviour when the playing ad is paused and resumed.
     * Expectation: When the ad is paused, PAUSED event should be fired along with proper callbacks. On resuming, RESUME event should be fired.
     */
    @Test
    fun testBehaviourWhenAdIsPausedAndResumed() {
        /** initialise the ad manager **/
        adManager?.init(mockContentProgressProvider)

        /** ad event listener is called with LOADED event since we have a pre roll ad **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.LOADED)
        verify(mockAdPlayer, times(1)).loadAd(anyObject())

        /** start and play the ad manager **/
        adManager?.start()
        adManager?.onPlay()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        reset(mockAdEventListener)

        /** return the current progress of ad **/
        `when`(mockAdPlayer.getAdProgress()).thenReturn(Progress(3, 10))
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** pause the ad **/
        adManager?.pause()

        /** ad event listener is called with PAUSED event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.PAUSED)
        verify(mockAdPlayer, times(1)).pauseAd()
        reset(mockAdEventListener)
        reset(mockAdPlayer)

        /** return the current progress of ad after resumed **/
        `when`(mockAdPlayer.getAdProgress()).thenReturn(Progress(6, 10))
        /** resume the ad now **/
        adManager?.resume()

        /** ad event listener is called with RESUMED event **/
        verify(mockAdEventListener, times(2)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.RESUMED)
        verify(mockAdPlayer, times(1)).playAd()
    }

    /**
     * Test to verify the behaviour when the vast has no media files to play
     */
    @Test
    fun testWhenVASTHasNoMediaFiles() {
        val answer = Answer { invocation ->
            val listener = invocation.getArgument<VastAdProvider.Listener>(1)
            // mimics return empty vast data
            listener.onVastFetchSuccess(VASTData())
        }
        doAnswer(answer).`when`(mockStringVastAdProvider).getVASTAd(anyObject(), anyObject())

        /** initialise the ad manager **/
        adManager?.init(
            mockContentProgressProvider,
            DefaultAdBreakFinder(),
            VastAdProviderImpl(mockStringVastAdProvider, mockNetworkVastAdProvider)
        )

        /** ad event listener is not called **/
        verify(mockAdEventListener, times(0)).onAdEvent(capture(adEventCaptor))

        /** ad error listener is called with NO_MEDIA_URL error **/
        verify(mockAdErrorListener, times(1)).onAdError(capture(adErrorCaptor))
        assert(adErrorCaptor.value.getType() == AdErrorType.NO_MEDIA_URL)
    }

    /**
     * Test to verify the behaviour when the vast fetch fails with an error
     */
    @Test
    fun testWhenVastFetchFails() {
        val answer = Answer { invocation ->
            val listener = invocation.getArgument<VastAdProvider.Listener>(1)
            // mimics a vast fetch error callback
            listener.onVastFetchError(
                AdErrorType.VAST_SCHEMA_VALIDATION_ERROR,
                "Error while fetching"
            )
        }
        doAnswer(answer).`when`(mockStringVastAdProvider).getVASTAd(anyObject(), anyObject())

        /** initialise the ad manager **/
        adManager?.init(
            mockContentProgressProvider,
            DefaultAdBreakFinder(),
            VastAdProviderImpl(mockStringVastAdProvider, mockNetworkVastAdProvider)
        )

        /** ad event listener is not called **/
        verify(mockAdEventListener, times(0)).onAdEvent(capture(adEventCaptor))

        /** ad error listener is called with VAST_ERROR error **/
        verify(mockAdErrorListener, times(1)).onAdError(capture(adErrorCaptor))
        assert(adErrorCaptor.value.getType() == AdErrorType.VAST_SCHEMA_VALIDATION_ERROR)
    }

    /**
     * Test to verify the behaviour when ad is skipped
     */
    @Test
    fun testWhenAdIsSkipped() {
        /** initialise the ad manager **/
        adManager?.init(mockContentProgressProvider)

        /** ad event listener is called with LOADED event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.LOADED)
        verify(mockAdPlayer, times(1)).loadAd(anyObject())

        adManager?.start()
        adManager?.onPlay()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        reset(mockAdEventListener)

        /** skip ad **/
        adManager?.onSkipAdClick()

        /** ad event listener is called with SKIPPED event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.SKIPPED)
        reset(mockAdEventListener)

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** ad event listener is called with STOPPED and CONTENT_RESUME_REQUESTED event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.CONTENT_RESUME_REQUESTED)
    }

    /**
     * Test when vast has no ads
     */
    @Test
    fun testWhenVastHasNoAds() {
        val vmap = VMAPUtil.createVMAPWithNoAds()
        adManager = DefaultAdManager(
            vmap,
            mockNetworkLayer,
            mockXmlParser,
            mockXmlValidator,
            mockAdRenderer
        )
        adManager?.addAdErrorListener(mockAdErrorListener)
        adManager?.addAdEventListener(mockAdEventListener)

        /** initialise the ad manager **/
        adManager?.init(mockContentProgressProvider)

        /** ad event listener is called for CONTENT_RESUME **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))

        /** ad error listener is called with NO_AD error **/
        verify(mockAdErrorListener, times(1)).onAdError(capture(adErrorCaptor))
        assert(adErrorCaptor.value.getType() == AdErrorType.NO_AD)
    }

    /**
     * Test to verify the behaviour of [AdManager] for a post roll ad
     */
    @Test
    fun testBehaviourWithPostRollAd() {
        `when`(mockContentProgressProvider.getContentProgress()).thenReturn(Progress(10000, 10000))

        val answer = Answer { invocation ->
            val listener = invocation.getArgument<VastAdProvider.Listener>(1)
            // mimics a vast fetch error callback
            listener.onVastFetchSuccess(
                VMAPUtil.createVAST()
            )
        }
        doAnswer(answer).`when`(mockNetworkVastAdProvider).getVASTAd(anyObject(), anyObject())

        /** initialise the ad manager **/
        adManager?.init(
            mockContentProgressProvider,
            DefaultAdBreakFinder(),
            VastAdProviderImpl(mockStringVastAdProvider, mockNetworkVastAdProvider)
        )

        /** ad event listener is called with LOADED event since we have a post roll ad **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.LOADED)
        assert(adEventCaptor.value.getAdElement() != null)
        verify(mockAdPlayer, times(1)).loadAd(anyObject())
    }
}
