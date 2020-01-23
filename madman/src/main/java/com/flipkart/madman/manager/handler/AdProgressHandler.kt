package com.flipkart.madman.manager.handler

import android.os.Handler
import android.os.Message
import com.flipkart.madman.provider.AdProgressProvider
import com.flipkart.madman.provider.Progress
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Handler which calls the [AdProgressProvider] every x seconds
 */
class AdProgressHandler(private val provider: AdProgressProvider) {

    private var handler: Handler? = null
    private var listeners: CopyOnWriteArrayList<AdProgressUpdateListener>? = null
    private val callback = AdProgressCallback()
    private var delay: Long = 0

    init {
        handler = Handler(callback)
    }

    fun setListener(listener: AdProgressUpdateListener) {
        if (listeners == null) {
            listeners = CopyOnWriteArrayList()
        }
        listeners?.add(listener)
    }

    fun removeListener(listener: AdProgressUpdateListener) {
        listeners?.remove(listener)
    }

    fun removeListeners() {
        listeners?.clear()
        listeners = null
    }

    fun sendMessage() {
        handler?.sendEmptyMessage(0)
    }

    fun sendMessageAfter(delayMs: Long) {
        this.delay = delayMs
        handler?.sendEmptyMessageDelayed(0, delayMs)
    }

    fun removeMessages() {
        handler?.removeCallbacksAndMessages(null)
    }

    inner class AdProgressCallback : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                0 -> {
                    val progress = provider.getAdProgress()
                    listeners?.forEach {
                        it.onAdProgressUpdate(progress)
                    }
                }
            }
            return true
        }
    }

    interface AdProgressUpdateListener {
        fun onAdProgressUpdate(progress: Progress)
    }
}
