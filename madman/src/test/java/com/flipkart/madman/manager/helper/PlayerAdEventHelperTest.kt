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
package com.flipkart.madman.manager.helper

import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.enums.AdEventType
import com.flipkart.madman.listener.AdErrorListener
import com.flipkart.madman.listener.AdEventListener
import com.flipkart.madman.listener.impl.AdError
import com.flipkart.madman.listener.impl.AdEvent
import com.flipkart.madman.manager.event.Event
import com.flipkart.madman.manager.model.AdElementImpl
import com.flipkart.madman.manager.model.AdPodImpl
import com.flipkart.madman.manager.model.VastAd
import com.flipkart.madman.manager.model.VastAdImpl
import com.flipkart.madman.renderer.player.AdPlayer
import com.flipkart.madman.testutils.capture
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class PlayerAdEventHelperTest {

    @Mock
    private lateinit var mockPlayer: AdPlayer

    @Mock
    private lateinit var mockAdEventListener: AdEventListener

    @Mock
    private lateinit var mockAdErrorListener: AdErrorListener

    @Captor
    private lateinit var adEventCaptor: ArgumentCaptor<AdEvent>

    @Captor
    private lateinit var adErrorCaptor: ArgumentCaptor<AdError>

    @Captor
    private lateinit var playerLoadCaptor: ArgumentCaptor<List<String>>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Test to verify that the correct events are being fired to the client.
     * The [AdManager] notifies this class with internal event type, and this class is responsible
     * to map the internal events to the events consumed by client
     */
    @Test
    fun testIfCorrectEventsAreTriggered() {
        val playerAdEventHelper = PlayerAdEventHelper(mockPlayer)
        playerAdEventHelper.addEventListener(mockAdEventListener)
        playerAdEventHelper.addErrorListener(mockAdErrorListener)

        val vastAd = createVastAd()

        /** verify CONTENT_RESUME event **/
        playerAdEventHelper.handleEvent(Event.CONTENT_RESUME, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.CONTENT_RESUME_REQUESTED)
        reset(mockAdEventListener)

        /** verify CONTENT_PAUSE event **/
        playerAdEventHelper.handleEvent(Event.CONTENT_PAUSE, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.CONTENT_PAUSE_REQUESTED)
        reset(mockAdEventListener)

        /** verify LOAD_AD event **/
        playerAdEventHelper.handleEvent(Event.LOAD_AD, vastAd)
        verify(mockAdEventListener, times(0)).onAdEvent(capture(adEventCaptor))
        verify(mockPlayer, times(1)).loadAd(capture(playerLoadCaptor))
        assert(playerLoadCaptor.value == vastAd.getAdMediaUrls())
        reset(mockAdEventListener)

        /** verify PLAY_AD event **/
        playerAdEventHelper.handleEvent(Event.PLAY_AD, vastAd)
        verify(mockAdEventListener, times(0)).onAdEvent(capture(adEventCaptor))
        verify(mockPlayer, times(1)).playAd()
        reset(mockAdEventListener)
        reset(mockPlayer)

        /** verify RESUME_AD event **/
        playerAdEventHelper.handleEvent(Event.RESUME_AD, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.RESUMED)
        verify(mockPlayer, times(1)).playAd()
        reset(mockAdEventListener)

        /** verify PAUSE_AD event **/
        playerAdEventHelper.handleEvent(Event.PAUSE_AD, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.PAUSED)
        verify(mockPlayer, times(1)).pauseAd()
        reset(mockAdEventListener)

        /** verify FIRST_QUARTILE event **/
        playerAdEventHelper.handleEvent(Event.FIRST_QUARTILE, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.FIRST_QUARTILE)
        reset(mockAdEventListener)

        /** verify MIDPOINT event **/
        playerAdEventHelper.handleEvent(Event.MIDPOINT, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.MIDPOINT)
        reset(mockAdEventListener)

        /** verify THIRD_QUARTILE event **/
        playerAdEventHelper.handleEvent(Event.THIRD_QUARTILE, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.THIRD_QUARTILE)
        reset(mockAdEventListener)

        /** verify AD_LOADED event **/
        playerAdEventHelper.handleEvent(Event.AD_LOADED, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.LOADED)
        reset(mockAdEventListener)

        /** verify AD_PROGRESS event **/
        playerAdEventHelper.handleEvent(Event.AD_PROGRESS, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.PROGRESS)
        reset(mockAdEventListener)

        /** verify AD_STARTED event **/
        playerAdEventHelper.handleEvent(Event.AD_STARTED, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.STARTED)
        reset(mockAdEventListener)

        /** verify AD_SKIPPED event **/
        playerAdEventHelper.handleEvent(Event.AD_SKIPPED, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.SKIPPED)
        reset(mockAdEventListener)

        /** verify AD_STOPPED event **/
        playerAdEventHelper.handleEvent(Event.AD_STOPPED, vastAd)
        verify(mockAdEventListener, times(0)).onAdEvent(capture(adEventCaptor))
        verify(mockPlayer, times(1)).stopAd()
        reset(mockAdEventListener)

        /** verify AD_COMPLETED event **/
        playerAdEventHelper.handleEvent(Event.AD_COMPLETED, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.COMPLETED)
        reset(mockAdEventListener)

        /** verify AD_TAPPED event **/
        playerAdEventHelper.handleEvent(Event.AD_TAPPED, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.TAPPED)
        reset(mockAdEventListener)

        /** verify AD_BREAK_STARTED event **/
        playerAdEventHelper.handleEvent(Event.AD_BREAK_STARTED, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.AD_BREAK_STARTED)
        reset(mockAdEventListener)

        /** verify AD_BREAK_LOADED event **/
        playerAdEventHelper.handleEvent(Event.AD_BREAK_LOADED, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.AD_BREAK_READY)
        reset(mockAdEventListener)

        /** verify AD_BREAK_ENDED event **/
        playerAdEventHelper.handleEvent(Event.AD_BREAK_ENDED, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.AD_BREAK_ENDED)
        reset(mockAdEventListener)

        /** verify AD_CTA_CLICKED event **/
        playerAdEventHelper.handleEvent(Event.AD_CTA_CLICKED, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.CLICKED)
        reset(mockAdEventListener)

        /** verify ALL_AD_COMPLETED event **/
        playerAdEventHelper.handleEvent(Event.ALL_AD_COMPLETED, vastAd)
        verify(mockAdEventListener, times(1)).onAdEvent(capture(adEventCaptor))
        assert(adEventCaptor.value.getType() == AdEventType.ALL_AD_COMPLETED)
        reset(mockAdEventListener)

        /** verify AD_ERROR event **/
        playerAdEventHelper.handleError(AdErrorType.AD_ERROR, "")
        verify(mockAdErrorListener, times(1)).onAdError(capture(adErrorCaptor))
        assert(adErrorCaptor.value.getType() == AdErrorType.AD_ERROR)
        reset(mockAdErrorListener)

        playerAdEventHelper.removeAdErrorListener(mockAdErrorListener)
        playerAdEventHelper.removeAdEventListener(mockAdEventListener)

        /** verify ALL_AD_COMPLETED event, but listener is not notified **/
        playerAdEventHelper.handleEvent(Event.ALL_AD_COMPLETED, vastAd)
        verify(mockAdEventListener, times(0)).onAdEvent(capture(adEventCaptor))
        reset(mockAdEventListener)
    }

    private fun createVastAd(): VastAd {
        val mediaUrl = mutableListOf<String>()
        mediaUrl.add("https://www.flipkart.com")

        val adPod = AdPodImpl(1, 1, false, 10.0, 1, 0.0)
        val adElement = AdElementImpl("1", true, 0.0, false, 10.0, "", "", "", "", adPod)

        return VastAdImpl(
            adElement,
            mediaUrl,
            VastAdImpl.AdTrackingImpl(null, null, null, null, null)
        )
    }
}
