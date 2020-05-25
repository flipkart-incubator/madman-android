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
import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.listener.AdErrorListener
import com.flipkart.madman.listener.AdEventListener
import com.flipkart.madman.listener.impl.AdError
import com.flipkart.madman.listener.impl.AdEvent
import com.flipkart.madman.manager.data.VastAdProvider
import com.flipkart.madman.manager.data.providers.NetworkVastAdProvider
import com.flipkart.madman.manager.data.providers.StringVastAdProvider
import com.flipkart.madman.manager.data.providers.VastAdProviderImpl
import com.flipkart.madman.manager.finder.DefaultAdBreakFinder
import com.flipkart.madman.manager.state.AdPlaybackState
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
     * Test to verify the behaviour when there is a pre-roll add
     */
    @Test
    fun testBehaviorWithPreRollAd() {
        /** initialise the ad manager **/
        adManager?.init(mockContentProgressProvider)

        /** verify there is ad break to be played and other properties **/
        assert(adManager?.adPlaybackState?.getAdGroup()?.getAdBreak() != null)
        assert(adManager?.adPlaybackState?.getAdGroup()?.getAdBreak()?.timeOffsetInSec == 0f)
        assert(
            adManager?.adPlaybackState?.getAdGroup()
                ?.getAdBreak()?.timeOffset == AdBreak.TimeOffsetTypes.START
        )

        /** ad event listener is called with LOADED as there is a pre-roll ad **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.LOADED)
        reset(mockAdEventListener)

        adManager?.start()
        adManager?.onPlay()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** ad event listener is called with STARTED event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
    }

    /**
     * Test to verify the behaviour when there is a no pre-roll add
     */
    @Test
    fun testBehaviourWithoutPreRollAd() {
        val vmap = VMAPUtil.createVMAP(false)
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

        /** ad event listener is called with CONTENT_RESUME_REQUESTED as no pre-roll is present **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.CONTENT_RESUME_REQUESTED)
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
        reset(mockAdEventListener)

        /** start and play the ad manager **/
        adManager?.start()
        adManager?.onPlay()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** return the current progress of ad **/
        `when`(mockAdPlayer.getAdProgress()).thenReturn(Progress(3, 10))
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** ad event listener is called with STARTED event since we have a pre roll ad **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.STARTED)
        reset(mockAdEventListener)

        /** pause the ad **/
        adManager?.pause()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** ad event listener is called with PAUSED event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.PAUSED)
        verify(mockAdPlayer, times(1)).pauseAd()
        reset(mockAdEventListener)
        reset(mockAdPlayer)

        /** return the current progress of ad after resumed **/
        `when`(mockAdPlayer.getAdProgress()).thenReturn(Progress(3, 10))

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
        reset(mockAdEventListener)

        adManager?.start()
        adManager?.onPlay()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** ad event listener is called with STARTED event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.STARTED)
        reset(mockAdEventListener)

        /** skip ad **/
        adManager?.onSkipAdClick()

        /** ad event listener is called with SKIPPED event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.SKIPPED)
        reset(mockAdEventListener)

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** ad event listener is called with CONTENT_RESUME_REQUESTED event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.CONTENT_RESUME_REQUESTED)
        verify(mockAdPlayer, times(1)).stopAd()
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
     * Test to verify the entire ad playback flow.
     * ie all the correct events are fired, renderer & player interactions
     */
    @Test
    fun testBehaviourForAdPlayback() {
        /** initialise the ad manager **/
        adManager?.init(mockContentProgressProvider)

        var adEventCaptorCount = 0

        /** ad event listener is called with LOADED event as pre-roll is present **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        adEventCaptorCount += 1
        assert(adEventCaptor.value.getType() == AdEventType.LOADED)
        reset(mockAdEventListener)

        /** start the ad manager, called when loaded event is fired **/
        adManager?.start()

        adManager?.onPlay()
        `when`(mockAdPlayer.getAdProgress()).thenReturn(Progress.UNDEFINED)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** verify ad renderer's create view is called once **/
        verify(mockAdRenderer, times(1)).createView()
        reset(mockAdRenderer)

        /** ad event listener is called with STARTED event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        adEventCaptorCount += 1
        assert(adEventCaptor.value.getType() == AdEventType.STARTED)
        reset(mockAdEventListener)

        /** mark the state as playing and verify further events **/
        adManager?.onPlay()
        adManager?.adPlaybackState?.updateAdState(AdPlaybackState.AdState.PLAYING)
        adManager?.previousAdProgress = Progress(500, 10000)
        `when`(mockAdPlayer.getAdProgress()).thenReturn(Progress(1000, 10000))
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** ad event listener is called with PROGRESS event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        adEventCaptorCount += 1
        assert(adEventCaptor.value.getType() == AdEventType.PROGRESS)
        reset(mockAdEventListener)

        adManager?.onPlay()
        adManager?.adPlaybackState?.updateAdState(AdPlaybackState.AdState.PLAYING)
        adManager?.previousAdProgress = Progress(1000, 10000)
        `when`(mockAdPlayer.getAdProgress()).thenReturn(Progress(3000, 10000))
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** ad event listener is called with FIRST_QUARTILE event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        adEventCaptorCount += 1
        assert(adEventCaptor.value.getType() == AdEventType.FIRST_QUARTILE)
        reset(mockAdEventListener)

        adManager?.onPlay()
        adManager?.adPlaybackState?.updateAdState(AdPlaybackState.AdState.PLAYING)
        adManager?.previousAdProgress = Progress(3000, 10000)
        `when`(mockAdPlayer.getAdProgress()).thenReturn(Progress(5500, 10000))
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** ad event listener is called with MIDPOINT event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        adEventCaptorCount += 1
        assert(adEventCaptor.value.getType() == AdEventType.MIDPOINT)
        reset(mockAdEventListener)

        adManager?.onPlay()
        adManager?.adPlaybackState?.updateAdState(AdPlaybackState.AdState.PLAYING)
        adManager?.previousAdProgress = Progress(5500, 10000)
        `when`(mockAdPlayer.getAdProgress()).thenReturn(Progress(8000, 10000))
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** ad event listener is called with THIRD_QUARTILE event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        adEventCaptorCount += 1
        assert(adEventCaptor.value.getType() == AdEventType.THIRD_QUARTILE)
        reset(mockAdEventListener)

        adManager?.onPlay()
        adManager?.adPlaybackState?.updateAdState(AdPlaybackState.AdState.PLAYING)
        adManager?.previousAdProgress = Progress(8000, 10000)
        `when`(mockAdPlayer.getAdProgress()).thenReturn(Progress(9500, 10000))
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** ad event listener is called with PROGRESS event **/
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        adEventCaptorCount += 1
        assert(adEventCaptor.value.getType() == AdEventType.PROGRESS)
        reset(mockAdEventListener)

        /** ad ended **/
        adManager?.onPlay()
        adManager?.adPlaybackState?.updateAdState(AdPlaybackState.AdState.PLAYING)
        adManager?.onEnded()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** verify ad renderer remove view is called once ad has ended **/
        verify(mockAdRenderer, times(1)).removeView()

        /** ad event listener is called with COMPLETED and CONTENT_RESUME_REQUESTED event **/
        verify(mockAdEventListener, times(2)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.allValues[adEventCaptorCount].getType() == AdEventType.COMPLETED)
        assert(adEventCaptor.allValues[adEventCaptorCount + 1].getType() == AdEventType.CONTENT_RESUME_REQUESTED)
        reset(mockAdEventListener)
    }
}
