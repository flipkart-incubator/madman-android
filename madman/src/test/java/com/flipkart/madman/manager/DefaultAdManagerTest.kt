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

package com.flipkart.madman.manager

import com.flipkart.madman.component.enums.AdEventType
import com.flipkart.madman.listener.AdErrorListener
import com.flipkart.madman.listener.AdEventListener
import com.flipkart.madman.listener.impl.AdEvent
import com.flipkart.madman.loader.AdLoader
import com.flipkart.madman.network.NetworkLayer
import com.flipkart.madman.network.model.NetworkAdRequest
import com.flipkart.madman.provider.ContentProgressProvider
import com.flipkart.madman.provider.Progress
import com.flipkart.madman.renderer.AdRenderer
import com.flipkart.madman.renderer.player.AdPlayer
import com.flipkart.madman.renderer.settings.DefaultRenderingSettings
import com.flipkart.madman.testutils.VMAPUtil
import com.flipkart.madman.testutils.anyObject
import com.flipkart.madman.testutils.capture
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
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
    private lateinit var mockAdLoader: AdLoader<NetworkAdRequest>

    @Mock
    private lateinit var mockNetworkLayer: NetworkLayer

    @Mock
    private lateinit var mockAdEventListener: AdEventListener

    @Mock
    private lateinit var mockAdErrorListener: AdErrorListener

    @Spy
    private lateinit var mockAdPlayer: AdPlayer

    @Spy
    private lateinit var mockAdRenderer: AdRenderer

    @Spy
    private lateinit var mockContentProgressProvider: ContentProgressProvider

    @Captor
    private lateinit var adEventCaptor: ArgumentCaptor<AdEvent>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Test to verify the behaviour of the ad manager with a pre roll ad break
     */
    @Test
    fun testBehaviourWithPreRollAd() {
        val vmap = VMAPUtil.createVMAP()

        /** return undefined progress in start **/
        Mockito.`when`(mockContentProgressProvider.getContentProgress())
            .thenReturn(Progress(0, 0))

        /** return default rendering settings **/
        Mockito.`when`(mockAdRenderer.getRenderingSettings()).thenReturn(DefaultRenderingSettings())

        /** return default rendering settings **/
        Mockito.`when`(mockAdRenderer.getAdPlayer()).thenReturn(mockAdPlayer)

        val adManager = DefaultAdManager(
            vmap,
            mockAdLoader,
            mockNetworkLayer,
            mockAdRenderer,
            mockAdEventListener,
            mockAdErrorListener
        )

        /** initialise the ad manager **/
        adManager.init(mockContentProgressProvider)

        /** ad event listener is called since we have a pre-roll to play **/
        Mockito.verify(mockAdEventListener, Mockito.times(1)).onAdEvent(capture(adEventCaptor))

        /** verify loaded event is fired first and player's load is called **/
        assert(adEventCaptor.value.getType() == AdEventType.LOADED)
        Mockito.verify(mockAdPlayer, Mockito.times(1)).loadAd(anyObject())

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        /** ad event listener is called since we have a pre-roll to play twice **/
        Mockito.verify(mockAdEventListener, Mockito.times(2)).onAdEvent(capture(adEventCaptor))

        /** content pause event is fired and player's play is called **/
        assert(adEventCaptor.value.getType() == AdEventType.CONTENT_PAUSE_REQUESTED)
        Mockito.verify(mockAdPlayer, Mockito.times(1)).playAd()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    }
}
