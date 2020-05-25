package com.flipkart.madman.manager.helper

import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.model.common.Tracking
import com.flipkart.madman.manager.event.Event
import com.flipkart.madman.manager.model.*
import com.flipkart.madman.manager.tracking.TrackingHandler
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
class TrackingEventHelperTest {

    @Mock
    private lateinit var mockTrackingHandler: TrackingHandler

    @Captor
    private lateinit var trackingUrlCaptor: ArgumentCaptor<List<String>>

    @Captor
    private lateinit var trackingEventCaptor: ArgumentCaptor<Tracking.TrackingEvent>

    @Captor
    private lateinit var adElementCaptor: ArgumentCaptor<AdElement>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testIfCorrectTrackingIsBeingSent() {
        val trackingEventHelper = TrackingEventHelper(mockTrackingHandler)

        val vastAd = createVastAd()

        /** verify AD_STARTED event, tracking handler is called 2 times ie 1 for start, and 1 for impressions with 3 urls **/
        trackingEventHelper.handleEvent(Event.AD_STARTED, vastAd)
        verify(mockTrackingHandler, times(2))
            .trackEvent(
                capture(trackingUrlCaptor),
                capture(trackingEventCaptor),
                capture(adElementCaptor)
            )
        assert(trackingUrlCaptor.value.size == 2) // called with 2 urls for impressions
        reset(mockTrackingHandler)

        /** verify FIRST_QUARTILE event, tracking handler is called 1 times  **/
        trackingEventHelper.handleEvent(Event.FIRST_QUARTILE, vastAd)
        verify(mockTrackingHandler, times(1))
            .trackEvent(
                capture(trackingUrlCaptor),
                capture(trackingEventCaptor),
                capture(adElementCaptor)
            )
        assert(trackingUrlCaptor.value.size == 2)
        reset(mockTrackingHandler)

        /** verify MIDPOINT event, tracking handler is called 1 times  **/
        trackingEventHelper.handleEvent(Event.MIDPOINT, vastAd)
        verify(mockTrackingHandler, times(1))
            .trackEvent(
                capture(trackingUrlCaptor),
                capture(trackingEventCaptor),
                capture(adElementCaptor)
            )
        assert(trackingUrlCaptor.value.size == 1)
        reset(mockTrackingHandler)

        /** verify THIRD_QUARTILE event, tracking handler is called 0 times as no tracking present **/
        trackingEventHelper.handleEvent(Event.THIRD_QUARTILE, vastAd)
        verify(mockTrackingHandler, times(0))
            .trackEvent(
                capture(trackingUrlCaptor),
                capture(trackingEventCaptor),
                capture(adElementCaptor)
            )
        reset(mockTrackingHandler)

        /** verify AD_CTA_CLICKED event, tracking handler is called 1 time **/
        trackingEventHelper.handleEvent(Event.AD_CTA_CLICKED, vastAd)
        verify(mockTrackingHandler, times(1))
            .trackEvent(
                capture(trackingUrlCaptor),
                capture(trackingEventCaptor),
                capture(adElementCaptor)
            )
        assert(trackingUrlCaptor.value.size == 1)
        reset(mockTrackingHandler)

        /** verify error for NO_AD **/
        trackingEventHelper.handleError(AdErrorType.NO_AD, vastAd)
        verify(mockTrackingHandler, times(1))
            .trackEvent(
                capture(trackingUrlCaptor),
                capture(trackingEventCaptor),
                capture(adElementCaptor)
            )
        assert(trackingUrlCaptor.value.size == 1)
        assert(trackingUrlCaptor.value[0] == "http://vast_error303") // replaced macro with error code
        reset(mockTrackingHandler)

        /** verify error for AD_ERROR **/
        trackingEventHelper.handleError(AdErrorType.AD_ERROR, vastAd)
        verify(mockTrackingHandler, times(1))
            .trackEvent(
                capture(trackingUrlCaptor),
                capture(trackingEventCaptor),
                capture(adElementCaptor)
            )
        assert(trackingUrlCaptor.value.size == 2)
        assert(trackingUrlCaptor.value[0] == "http://error")
        assert(trackingUrlCaptor.value[1] == "http://error_1")
    }

    private fun createVastAd(): VastAd {
        val mediaUrl = mutableListOf<String>()
        mediaUrl.add("https://www.flipkart.com")

        val adPod = AdPodImpl(1, 1, false, 10.0, 1, 0.0)
        val adElement = AdElementImpl("1", true, 0.0, false, 10.0, "", "", "", "", adPod)

        return VastAdImpl(adElement, mediaUrl, createAdTracking())
    }

    private fun createAdTracking(): VastAd.AdTracking {
        val trackingMap = mutableMapOf<Tracking.TrackingEvent, List<String>>()
        trackingMap[Tracking.TrackingEvent.FIRST_QUARTILE] =
            listOf("https://first_quartile", "https://first_quartile_1")
        trackingMap[Tracking.TrackingEvent.MIDPOINT] = listOf("https://midpoint_quartile")
        trackingMap[Tracking.TrackingEvent.START] = listOf("https://start")

        return VastAdImpl.AdTrackingImpl(
            trackingMap.toMap(),
            listOf("http://impression", "http://impression_1"),
            listOf("http://error", "http://error_1"),
            listOf("http://vast_error[ERRORCODE]"),
            listOf("http://click_tracking")
        )
    }
}
