package com.flipkart.madman.helper

import android.os.Looper

/**
 * Provides methods for asserting the truth of expressions and properties.
 */
object Assertions {
    /**
     * Throws [IllegalStateException] if the calling thread is not the application's main
     * thread.
     */
    fun checkMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw IllegalStateException("Should be called from main thread")
        }
    }

    /**
     * Throws [IllegalArgumentException] if argument is null.
     */
    fun checkNotNull(arg: Any?, message: String) {
        requireNotNull(arg) { message }
    }
}
