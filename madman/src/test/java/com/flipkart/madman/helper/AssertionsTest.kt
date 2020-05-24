package com.flipkart.madman.helper

import org.junit.Test

class AssertionsTest {

    /**
     * Check if [IllegalArgumentException] is thrown if arg is null
     */
    @Test(expected = IllegalArgumentException::class)
    fun testPrecondition() {
        Assertions.checkNotNull(null, "")
    }
}
