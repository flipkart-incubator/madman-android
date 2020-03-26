/*
 * Copyright (C) 2020 Flipkart Internet Pvt Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
