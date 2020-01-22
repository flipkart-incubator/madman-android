package com.flipkart.madman.manager.handler

import android.os.Handler
import android.os.Message
import com.flipkart.madman.provider.ContentProgressProvider
import com.flipkart.madman.provider.Progress

/**
 * Handler which calls the [ContentProgressProvider] every x seconds
 */
class ContentProgressHandler(private val provider: ContentProgressProvider) {

    private var handler: Handler? = null
    private var listeners: MutableList<ContentProgressUpdateListener>? = null

    init {
        val callback = ContentProgressCallback()
        handler = Handler(callback)
    }

    fun setListener(listener: ContentProgressUpdateListener) {
        if (listeners == null) {
            listeners = ArrayList()
        }
        listeners?.add(listener)
    }

    fun removeListener(listener: ContentProgressUpdateListener) {
        listeners?.remove(listener)
    }

    fun removeListeners() {
        listeners?.clear()
    }

    fun sendMessage() {
        handler?.sendEmptyMessage(0)
    }

    fun sendMessageDelayed(delay: Long) {
        handler?.sendEmptyMessageDelayed(0, delay)
    }

    fun removeMessages() {
        handler?.removeCallbacksAndMessages(null)
    }

    inner class ContentProgressCallback : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                0 -> {
                    val progress = provider.getContentProgress()
                    listeners?.forEach {
                        it.onContentProgressUpdate(progress)
                    }
                }
            }
            return true
        }
    }

    interface ContentProgressUpdateListener {
        fun onContentProgressUpdate(progress: Progress)
    }
}
