package com.flipkart.madman.manager

import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.listener.AdEventListener
import com.flipkart.madman.loader.AdLoader
import com.flipkart.madman.manager.callback.AdPlayerCallback
import com.flipkart.madman.manager.data.VastAdProvider
import com.flipkart.madman.manager.data.providers.NetworkVastAdProvider
import com.flipkart.madman.manager.data.providers.StringVastAdProvider
import com.flipkart.madman.manager.data.providers.VastAdProviderImpl
import com.flipkart.madman.manager.event.Event
import com.flipkart.madman.manager.finder.AdBreakFinder
import com.flipkart.madman.manager.finder.DefaultAdBreakFinder
import com.flipkart.madman.manager.handler.AdProgressHandler
import com.flipkart.madman.manager.handler.ContentProgressHandler
import com.flipkart.madman.manager.helper.PlayerEventHelper
import com.flipkart.madman.manager.helper.TrackingEventHelper
import com.flipkart.madman.manager.model.VastAd
import com.flipkart.madman.manager.state.AdPlaybackState
import com.flipkart.madman.manager.tracking.DefaultTrackingHandler
import com.flipkart.madman.manager.tracking.TrackingHandler
import com.flipkart.madman.network.NetworkLayer
import com.flipkart.madman.network.model.NetworkAdRequest
import com.flipkart.madman.provider.ContentProgressProvider
import com.flipkart.madman.renderer.AdRenderer
import com.flipkart.madman.renderer.player.AdPlayer

/**
 * Base implementation of [AdManager]
 *
 * Implements [AdPlayer.AdPlayerCallback]
 */
abstract class BaseAdManager(
    private val data: VMAPData,
    private val adRenderer: AdRenderer,
    private val adLoader: AdLoader<NetworkAdRequest>,
    networkLayer: NetworkLayer,
    adEventListener: AdEventListener
) : AdManager, AdPlayerCallback(),
    ContentProgressHandler.ContentProgressUpdateListener,
    AdProgressHandler.AdProgressUpdateListener {
    private var contentProgressProvider: ContentProgressProvider? = null

    /** ad player interface **/
    private val player: AdPlayer by lazy {
        val player = adRenderer.getAdPlayer()
        player.registerAdPlayerCallback(this)
        player
    }

    /** player event handler helper class **/
    private val playerAdEventHelper: PlayerEventHelper by lazy {
        val handler = PlayerEventHelper(player)
        handler.setEventListener(adEventListener)
        handler
    }

    /** tracking handler helper class **/
    private val trackingEventHelper: TrackingEventHelper by lazy {
        TrackingEventHelper(DefaultTrackingHandler(networkLayer))
    }

    /** represent ad state **/
    protected var adPlaybackState: AdPlaybackState = AdPlaybackState()

    /** media progress handler to call the media progress every x seconds **/
    protected val contentProgressHandler: ContentProgressHandler by lazy {
        ContentProgressHandler(contentProgressProvider)
    }

    /** ad progress handler to call the ad progress every x seconds **/
    protected val adProgressHandler: AdProgressHandler by lazy {
        AdProgressHandler(player)
    }

    /** ad break finder, used to fetch next playable ad break **/
    protected var adBreakFinder: AdBreakFinder = DefaultAdBreakFinder()

    /** vast ad provider **/
    protected val vastAdProvider: VastAdProvider by lazy {
        createVastAdProvider()
    }

    /** current ad being played **/
    protected var currentAd: VastAd? = null

    override fun init(contentProgressProvider: ContentProgressProvider) {
        this.contentProgressProvider = contentProgressProvider

        /** initialise the ad playback state with data and break finder **/
        adPlaybackState = adPlaybackState.withData(data)

        onInit()
    }

    override fun init(
        contentProgressProvider: ContentProgressProvider,
        adBreakFinder: AdBreakFinder
    ) {
        this.adBreakFinder = adBreakFinder
        init(contentProgressProvider)
    }

    /**
     * override tracking handler
     */
    override fun addTrackingHandler(handler: TrackingHandler) {
        super.addTrackingHandler(handler)
        trackingEventHelper.setTrackingHandler(handler)
    }

    /**
     * destroy [AdManager]
     */
    override fun destroy() {
        player.unregisterAdPlayerCallback(this)
        playerAdEventHelper.destroy()
        removeAdMessageHandler()
        removeContentHandler()
    }

    /**
     * creates a [VastAdProvider]
     */
    protected open fun createVastAdProvider(): VastAdProvider {
        return VastAdProviderImpl(
            StringVastAdProvider(),
            NetworkVastAdProvider(adLoader)
        )
    }

    /**
     * notify all the registered event handlers for the given event
     */
    protected fun notifyAndTrackEvent(event: Event) {
        playerAdEventHelper.handleEvent(event, currentAd)
        trackingEventHelper.handleEvent(event, currentAd)
    }

    /**
     * notify all the registered event handlers for the given event
     */
    protected fun notifyAndTrackEvent(event: Event, errorCode: Int) {
        playerAdEventHelper.handleEvent(event, currentAd)
        trackingEventHelper.handleEvent(event, currentAd, errorCode)
    }

    /**
     * Start the content handler
     */
    protected fun startContentHandler() {
        removeContentHandler()
        contentProgressHandler.setListener(this)
        contentProgressHandler.sendMessage()
    }

    /**
     * remove the media handler
     */
    protected fun removeContentHandler() {
        contentProgressHandler.removeMessages()
        contentProgressHandler.removeListeners()
    }

    /**
     * start ad progress handlers
     */
    protected fun startAdMessageHandler() {
        removeAdMessageHandler()
        adProgressHandler.setListener(this)
        adProgressHandler.sendMessage()
    }

    /**
     * remove ad progress handlers
     */
    protected fun removeAdMessageHandler() {
        adProgressHandler.removeMessages()
        adProgressHandler.removeListeners()
    }

    /**
     * on init
     */
    protected abstract fun onInit()
}
