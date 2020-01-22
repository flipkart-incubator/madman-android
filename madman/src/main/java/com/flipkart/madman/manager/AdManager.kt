package com.flipkart.madman.manager

import com.flipkart.madman.manager.finder.AdBreakFinder
import com.flipkart.madman.manager.tracking.TrackingHandler
import com.flipkart.madman.provider.ContentProgressProvider

interface AdManager {
    fun init(contentProgressProvider: ContentProgressProvider)

    fun init(contentProgressProvider: ContentProgressProvider, adBreakFinder: AdBreakFinder)

    fun start()

    fun pause()

    fun resume()

    fun destroy()

    fun contentComplete()

    fun getCuePoints(): List<Float>

    fun addTrackingHandler(handler: TrackingHandler) {
        // override it if required
    }
}
