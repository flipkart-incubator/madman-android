package com.flipkart.madman.manager.helper

import com.flipkart.madman.manager.model.AdElementImpl
import com.flipkart.madman.manager.model.AdPodImpl
import com.flipkart.madman.manager.model.VastAd
import com.flipkart.madman.manager.model.VastAdImpl
import com.flipkart.madman.manager.tracking.TrackingHandler
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class TrackingEventHelperTest {

    @Mock
    private lateinit var mockTrackingHandler: TrackingHandler

    @Test
    fun testIfCorrectTrackingIsBeingSent() {
        val trackingEventHelper = TrackingEventHelper(mockTrackingHandler)

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
