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

import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.listener.AdErrorListener
import com.flipkart.madman.listener.AdEventListener
import com.flipkart.madman.loader.impl.NetworkAdLoader
import com.flipkart.madman.manager.callback.AdPlayerCallback
import com.flipkart.madman.manager.data.VastAdProvider
import com.flipkart.madman.manager.data.providers.NetworkVastAdProvider
import com.flipkart.madman.manager.data.providers.StringVastAdProvider
import com.flipkart.madman.manager.data.providers.VastAdProviderImpl
import com.flipkart.madman.manager.finder.AdBreakFinder
import com.flipkart.madman.manager.finder.DefaultAdBreakFinder
import com.flipkart.madman.manager.handler.AdProgressUpdateListener
import com.flipkart.madman.manager.handler.ContentProgressUpdateListener
import com.flipkart.madman.manager.handler.ProgressHandler
import com.flipkart.madman.manager.helper.PlayerAdEventHelper
import com.flipkart.madman.manager.helper.TrackingEventHelper
import com.flipkart.madman.manager.state.AdPlaybackState
import com.flipkart.madman.manager.tracking.DefaultTrackingHandler
import com.flipkart.madman.network.NetworkLayer
import com.flipkart.madman.parser.XmlParser
import com.flipkart.madman.provider.ContentProgressProvider
import com.flipkart.madman.renderer.AdRenderer
import com.flipkart.madman.renderer.player.AdPlayer
import com.flipkart.madman.validator.XmlValidator

/**
 * Base implementation of [AdManager]
 *
 * Implements [AdPlayer.AdPlayerCallback]
 */
abstract class BaseAdManager(
    data: VMAPData,
    adRenderer: AdRenderer,
    networkLayer: NetworkLayer,
    xmlParser: XmlParser,
    xmlValidator: XmlValidator
) : AdManager, AdPlayerCallback(),
    ContentProgressUpdateListener,
    AdProgressUpdateListener {
    private lateinit var contentProgressProvider: ContentProgressProvider

    /** [AdPlayer] provided by the [AdRenderer], registers this class as a callback **/
    private val player: AdPlayer by lazy {
        val player = adRenderer.getAdPlayer()
        player.registerAdPlayerCallback(this)
        player
    }

    /** NetworkAdLoader to fetch ads from network **/
    private val networkAdLoader = NetworkAdLoader(networkLayer, xmlParser, xmlValidator)

    /** [AdBreakFinder] used to fetch next playable ad break **/
    protected lateinit var adBreakFinder: AdBreakFinder

    /** [VastAdProvider] which provides the [VastAd] **/
    protected lateinit var vastAdProvider: VastAdProvider

    /** Helper class which notifies all registered [AdEventListener] **/
    protected val playerAdEventHelper: PlayerAdEventHelper by lazy {
        PlayerAdEventHelper(player)
    }

    /** tracking handler helper class **/
    protected val trackingEventHelper: TrackingEventHelper by lazy {
        TrackingEventHelper(DefaultTrackingHandler(networkLayer))
    }

    /** represent ad state **/
    protected var adPlaybackState: AdPlaybackState = AdPlaybackState(data.adBreaks ?: emptyList())

    /** [ProgressHandler] to fetch progress for content and ad **/
    protected val progressHandler: ProgressHandler by lazy {
        ProgressHandler(contentProgressProvider, player, null)
    }

    override fun init(contentProgressProvider: ContentProgressProvider) {
        this.contentProgressProvider = contentProgressProvider
        this.adBreakFinder = DefaultAdBreakFinder()
        this.vastAdProvider =
            VastAdProviderImpl(StringVastAdProvider(), NetworkVastAdProvider(networkAdLoader))
        init()
    }

    override fun init(
        contentProgressProvider: ContentProgressProvider,
        adBreakFinder: AdBreakFinder,
        vastAdProvider: VastAdProvider
    ) {
        this.contentProgressProvider = contentProgressProvider
        this.adBreakFinder = adBreakFinder
        this.vastAdProvider = vastAdProvider
        init()
    }

    override fun addAdEventListener(listener: AdEventListener) {
        playerAdEventHelper.addEventListener(listener)
    }

    override fun removeAdEventListener(listener: AdEventListener) {
        playerAdEventHelper.removeAdEventListener(listener)
    }

    override fun addAdErrorListener(listener: AdErrorListener) {
        playerAdEventHelper.addErrorListener(listener)
    }

    override fun removeAdErrorListener(listener: AdErrorListener) {
        playerAdEventHelper.removeAdErrorListener(listener)
    }

    override fun destroy() {
        player.unregisterAdPlayerCallback(this)
        playerAdEventHelper.destroy()
        progressHandler.destroy()
    }

    /**
     * Called when [AdManager] is initialized
     */
    protected abstract fun init()

    /**
     * start the content handler
     */
    protected fun startContentHandler() {
        removeContentHandler()
        progressHandler.setContentProgressListener(this)
        progressHandler.sendMessageFor(ProgressHandler.MessageCode.CONTENT_MESSAGE)
    }

    /**
     * remove the content handler
     */
    protected fun removeContentHandler() {
        progressHandler.removeMessagesFor(ProgressHandler.MessageCode.CONTENT_MESSAGE)
        progressHandler.removeContentProgressListeners()
    }

    /**
     * start ad progress handlers
     */
    protected fun startAdMessageHandler() {
        removeAdMessageHandler()
        progressHandler.setAdProgressListener(this)
        progressHandler.sendMessageFor(ProgressHandler.MessageCode.AD_MESSAGE)
    }

    /**
     * remove ad progress handlers
     */
    protected fun removeAdMessageHandler() {
        progressHandler.removeMessagesFor(ProgressHandler.MessageCode.AD_MESSAGE)
        progressHandler.removeAdProgressListeners()
    }
}
