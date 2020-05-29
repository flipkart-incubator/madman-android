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
import com.flipkart.madman.network.backoff.DefaultBackOffPolicy
import com.flipkart.madman.network.helper.Util.getLanguage
import com.flipkart.madman.network.helper.Util.getPackageName
import com.flipkart.madman.network.helper.Util.getUserAgent
import com.flipkart.madman.network.model.NetworkAdRequest
import okhttp3.*
import java.io.IOException

/**
 * Default implementation of the [NetworkLayer]
 *
 * It uses the [OkHttpClient] to make network calls. You can override the behaviour if required.
 */
open class DefaultNetworkLayer(private val context: Context) : NetworkLayer,
    Callback {
    /** ok-http client for making request **/
    protected open val okHttpClient: OkHttpClient by lazy {
        createOkHttpClient()
    }

    /** back off policy **/
    protected open val backOffPolicy: BackOffPolicy by lazy {
        createBackOffPolicy()
    }

    /** callback listener, passed in fetch **/
    private var resultListener: NetworkListener<String>? = null

    private val userAgent: String by lazy {
        getUserAgent(context)
    }

    override fun fetch(
        request: NetworkAdRequest,
        resultListener: NetworkListener<String>,
        cancellationSignal: CancellationSignal
    ) {
        request.url?.let {
            this.resultListener = resultListener

            val requestBuilder = Request.Builder().url(it)
            modifyRequest(requestBuilder)

            val currentCall = okHttpClient.newCall(requestBuilder.build())
            enqueueCall(currentCall)

            cancellationSignal.setOnCancelListener {
                /** cancel the call **/
                currentCall.cancel()
            }
        } ?: run {
            resultListener.onError(
                0,
                "url is empty"
            )
        }
    }

    override fun post(url: String) {
        val requestBuilder = Request.Builder().url(url)
        modifyRequest(requestBuilder)

        okHttpClient.newCall(requestBuilder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // no-op
            }

            override fun onResponse(call: Call, response: Response) {
                // no-op
            }
        })
    }

    override fun onFailure(call: Call, e: IOException) {
        resultListener?.onError(
            0,
            ERROR_MESSAGE
        )
    }

    override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
            resultListener?.onSuccess(
                response.code(),
                response.body()?.string()
            )
        } else {
            resultListener?.onError(
                response.code(),
                response.body()?.string() ?: ERROR_MESSAGE
            )
        }
    }

    /**
     * creates a new ok http client
     */
    protected open fun createOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.retryOnConnectionFailure(true)
        return OkHttpClient()
    }

    /**
     * creates a [BackOffPolicy]
     */
    protected open fun createBackOffPolicy(): BackOffPolicy {
        return DefaultBackOffPolicy()
    }

    /**
     * override this method to modify the request
     * for eg adding headers etc
     */
    protected open fun modifyRequest(request: Request.Builder) {
        request.addHeader(ACCEPT_LANGUAGE_HEADER, getLanguage())
        request.addHeader(X_REQUESTED_WITH_HEADER, getPackageName(context))
        request.addHeader(
            ACCEPT_HEADER,
            ACCEPT_HEADER_VALUE
        )
        request.addHeader(USER_AGENT_HEADER, userAgent)
    }

    private fun enqueueCall(call: Call?) {
        call?.enqueue(this)
    }

    companion object {
        const val ERROR_MESSAGE = "Something went wrong"
        const val ACCEPT_LANGUAGE_HEADER = "Accept-Language"
        const val X_REQUESTED_WITH_HEADER = "X-Requested-With"
        const val ACCEPT_HEADER = "Accept"
        const val USER_AGENT_HEADER = "User-Agent"
        const val ACCEPT_HEADER_VALUE = "*/*"
    }
}
