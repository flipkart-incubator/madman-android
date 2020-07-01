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
package com.flipkart.madman.okhttp.extension

import android.content.Context
import android.os.CancellationSignal
import com.flipkart.madman.network.NetworkLayer
import com.flipkart.madman.network.NetworkListener
import com.flipkart.madman.network.backoff.BackOffPolicy
import com.flipkart.madman.network.model.NetworkAdRequest
import com.flipkart.madman.okhttp.extension.helper.HeaderUtils
import com.flipkart.madman.okhttp.extension.helper.MainThreadExecutor
import okhttp3.*
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * Default implementation of the [NetworkLayer]
 *
 * It uses the [OkHttpClient] to make network calls. You can override the behaviour if required.
 */
open class DefaultNetworkLayer(private val context: Context, builder: Builder) : NetworkLayer {
    private val mainThreadExecutor: Executor = builder.mainThreadExecutor ?: MainThreadExecutor()
    private val callTimeoutInMs: Long = builder.vastTimeoutInMs ?: DEFAULT_TIMEOUT_IN_MS

    /** ok-http client for making request **/
    private val okHttpClient: OkHttpClient by lazy {
        createOkHttpClient()
    }

    private val userAgent: String by lazy {
        HeaderUtils.getUserAgent(context)
    }

    private val locale: String by lazy {
        HeaderUtils.getLanguage()
    }

    private val packageName: String by lazy {
        HeaderUtils.getPackageName(context)
    }

    override fun fetch(
        request: NetworkAdRequest,
        resultListener: NetworkListener<String>,
        cancellationSignal: CancellationSignal
    ) {
        val requestBuilder = Request.Builder().url(request.url)
        modifyRequest(requestBuilder)

        val newCall = okHttpClient.newCall(requestBuilder.build())
        newCall.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainThreadExecutor.execute {
                    resultListener.onError(
                        0,
                        e.message ?: DEFAULT_ERROR_MESSAGE
                    )
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body()?.string()
                if (response.isSuccessful) {
                    mainThreadExecutor.execute {
                        resultListener.onSuccess(
                            response.code(),
                            responseBody
                        )
                    }
                } else {
                    mainThreadExecutor.execute {
                        resultListener.onError(
                            response.code(),
                            responseBody ?: DEFAULT_ERROR_MESSAGE
                        )
                    }
                }
            }
        })

        cancellationSignal.setOnCancelListener {
            /** cancel the call **/
            newCall.cancel()
        }
    }

    override fun post(url: String, resultListener: NetworkListener<String>) {
        val requestBuilder = Request.Builder().url(url)
        modifyRequest(requestBuilder)

        okHttpClient.newCall(requestBuilder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainThreadExecutor.execute {
                    resultListener.onError(
                        0,
                        e.message ?: DEFAULT_ERROR_MESSAGE
                    )
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body()?.string()
                mainThreadExecutor.execute {
                    resultListener.onSuccess(
                        response.code(),
                        responseBody
                    )
                }
            }
        })
    }

    /**
     * creates a new ok http client
     */
    protected open fun createOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(callTimeoutInMs, TimeUnit.MILLISECONDS)
            .readTimeout(callTimeoutInMs * 4, TimeUnit.MILLISECONDS)
            .writeTimeout(callTimeoutInMs * 4, TimeUnit.MILLISECONDS)
        return builder.build()
    }

    /**
     * to modify the request builder
     * @param request
     */
    protected open fun modifyRequest(request: Request.Builder) {
        request.addHeader(ACCEPT_LANGUAGE_HEADER, locale)
        request.addHeader(X_REQUESTED_WITH_HEADER, packageName)
        request.addHeader(USER_AGENT_HEADER, userAgent)
        request.addHeader(ACCEPT_HEADER, ACCEPT_HEADER_VALUE)
    }

    /**
     * Builder class to build an instance of [DefaultNetworkLayer]
     */
    class Builder {
        internal var backOffPolicy: BackOffPolicy? = null
        internal var mainThreadExecutor: Executor? = null
        internal var vastTimeoutInMs: Long? = null

        /**
         * set a custom [BackOffPolicy]
         * if unset, [DefaultBackOffPolicy] will be used
         */
        fun setBackOffPolicy(backOffPolicy: BackOffPolicy): Builder {
            this.backOffPolicy = backOffPolicy
            return this
        }

        /**
         * set a main thread executor
         */
        fun setMainThreadExecutor(executor: Executor): Builder {
            this.mainThreadExecutor = executor
            return this
        }

        /**
         * set vast time out in milli sec
         */
        fun setVastTimeout(timeoutInMs: Long): Builder {
            this.vastTimeoutInMs = timeoutInMs
            return this
        }

        fun build(context: Context): DefaultNetworkLayer {
            return DefaultNetworkLayer(context, this)
        }
    }

    companion object {
        const val DEFAULT_ERROR_MESSAGE = "Something went wrong"
        const val DEFAULT_TIMEOUT_IN_MS = 5000L // 5 seconds timeout
        const val ACCEPT_LANGUAGE_HEADER = "Accept-Language"
        const val X_REQUESTED_WITH_HEADER = "X-Requested-With"
        const val ACCEPT_HEADER = "Accept"
        const val USER_AGENT_HEADER = "User-Agent"
        const val ACCEPT_HEADER_VALUE = "*/*"
    }
}
