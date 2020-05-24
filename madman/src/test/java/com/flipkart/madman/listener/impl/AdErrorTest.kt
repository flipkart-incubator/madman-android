package com.flipkart.madman.listener.impl

import com.flipkart.madman.component.enums.AdErrorType
import org.junit.Test

class AdErrorTest {

    @Test
    fun testSettersAndGetters() {
        val adError = AdError(AdErrorType.AD_ERROR, "ad error")
        assert(adError.getType() == AdErrorType.AD_ERROR)
        assert(adError.getMessage() == "ad error")
    }
}
