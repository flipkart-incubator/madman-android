package com.flipkart.madman.listener.impl

import com.flipkart.madman.component.enums.AdEventType
import org.junit.Test

class AdEventTest {

    @Test
    fun testGettersAndSetters() {
        val adEvent = AdEvent(AdEventType.TAPPED, null)
        assert(adEvent.getType() == AdEventType.TAPPED)
        assert(adEvent.getAdElement() == null)
    }
}
