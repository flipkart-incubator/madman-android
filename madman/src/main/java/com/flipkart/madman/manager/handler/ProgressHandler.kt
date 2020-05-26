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
import com.flipkart.madman.provider.ContentProgressProvider
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Uses [Handler] to query the progress of the content and ad every x seconds, and notifies the listeners.
 */
class ProgressHandler(
    private val contentProgressProvider: ContentProgressProvider,
    private val adProgressProvider: AdProgressProvider,
    handler: Handler?
) {
    private var contentProgressListeners: CopyOnWriteArrayList<ContentProgressUpdateListener>? =
        null
    private var adProgressListeners: CopyOnWriteArrayList<AdProgressUpdateListener>? =
        null
    private var handler: Handler? = null

    init {
        this.handler = handler ?: Handler(ProgressCallback())
    }

    fun setContentProgressListener(listener: ContentProgressUpdateListener) {
        if (contentProgressListeners == null) {
            contentProgressListeners = CopyOnWriteArrayList()
        }
        contentProgressListeners?.add(listener)
    }

    fun setAdProgressListener(listener: AdProgressUpdateListener) {
        if (adProgressListeners == null) {
            adProgressListeners = CopyOnWriteArrayList()
        }
        adProgressListeners?.add(listener)
    }

    fun removeContentProgressListeners() {
        contentProgressListeners?.clear()
        contentProgressListeners = null
    }

    fun removeAdProgressListeners() {
        adProgressListeners?.clear()
        adProgressListeners = null
    }

    fun sendMessageFor(what: MessageCode) {
        handler?.sendEmptyMessage(what.value)
    }

    fun sendDelayedMessageFor(delayMs: Long, what: MessageCode) {
        handler?.sendEmptyMessageDelayed(what.value, delayMs)
    }

    fun removeMessagesFor(what: MessageCode) {
        handler?.removeMessages(what.value)
    }

    fun destroy() {
        removeContentProgressListeners()
        removeAdProgressListeners()
        handler?.removeCallbacksAndMessages(null)
    }

    inner class ProgressCallback : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                /**
                 * fetch the content progress from [ContentProgressProvider]
                 */
                MessageCode.CONTENT_MESSAGE.value -> {
                    val progress = contentProgressProvider.getContentProgress()
                    contentProgressListeners?.forEach {
                        it.onContentProgressUpdate(progress)
                    }
                }
                /**
                 * fetch the ad progress from [AdProgressProvider]
                 */
                MessageCode.AD_MESSAGE.value -> {
                    val progress = adProgressProvider.getAdProgress()
                    adProgressListeners?.forEach {
                        it.onAdProgressUpdate(progress)
                    }
                }
            }
            return true
        }
    }

    enum class MessageCode(val value: Int) {
        CONTENT_MESSAGE(0),
        AD_MESSAGE(1)
    }
}
