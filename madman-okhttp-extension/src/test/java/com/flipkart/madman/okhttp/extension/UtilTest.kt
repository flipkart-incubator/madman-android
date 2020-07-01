package com.flipkart.madman.okhttp.extension

import com.flipkart.madman.okhttp.extension.helper.HeaderUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class UtilTest {
    @Test
    fun testLocale() {
        val language = HeaderUtils.getLanguage()
        assert(language == "en")
    }

    @Test
    fun testUserAgent() {
        val userAgent = HeaderUtils.getUserAgent(RuntimeEnvironment.application)
        assert(userAgent.isNotEmpty())
    }
}
