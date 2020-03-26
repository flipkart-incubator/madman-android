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
package com.flipkart.madman

import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.enums.StringErrorConstants
import com.flipkart.madman.helper.Precondition
import com.flipkart.madman.listener.AdErrorListener
import com.flipkart.madman.listener.AdEventListener
import com.flipkart.madman.listener.AdLoadListener
import com.flipkart.madman.listener.impl.AdError
import com.flipkart.madman.loader.AdLoader
import com.flipkart.madman.loader.impl.NetworkAdLoader
import com.flipkart.madman.loader.impl.StringAdLoader
import com.flipkart.madman.logger.DebugLevelLogger
import com.flipkart.madman.logger.LogUtil
import com.flipkart.madman.logger.Logger
import com.flipkart.madman.manager.DefaultAdManager
import com.flipkart.madman.network.NetworkLayer
import com.flipkart.madman.network.model.NetworkAdRequest
import com.flipkart.madman.network.model.Request
import com.flipkart.madman.network.model.StringAdRequest
import com.flipkart.madman.parser.XmlParser
import com.flipkart.madman.renderer.AdRenderer
import com.flipkart.madman.renderer.DefaultAdRenderer
import com.flipkart.madman.validator.DefaultXmlValidator

class Madman internal constructor(
    builder: Builder,
    context: Context
) {
    private var parser: XmlParser
    private var networkLayer: NetworkLayer
    private var adErrorListener: AdErrorListener
    private var adLoadListener: AdLoadListener
    private var adEventListener: AdEventListener
    private var mainThreadHandler: Handler
    private var networkAdLoader: AdLoader<NetworkAdRequest>
    private var stringAdLoader: AdLoader<StringAdRequest>

    init {
        adErrorListener = builder.adErrorListener
            ?: throw IllegalStateException("adErrorListener cannot be null, implement AdErrorListener interface")
        adLoadListener = builder.adLoadListener
            ?: throw IllegalStateException("adLoadListener cannot be null, implement AdLoadListener interface")
        adEventListener = builder.adEventListener
            ?: throw IllegalStateException("adEventListener cannot be null, implement AdEventListener interface")
        networkLayer = builder.networkLayer
            ?: throw IllegalStateException("network layer cannot be null, implement NetworkLayer interface")
        mainThreadHandler = builder.mainThreadHandler ?: Handler(Looper.getMainLooper())
        parser =
            builder.parser ?: XmlParser.Builder().build(
                mainThreadHandler,
                AsyncTask.THREAD_POOL_EXECUTOR
            )
        LogUtil.setLogger(builder.logger ?: DebugLevelLogger())
        val validator = DefaultXmlValidator()
        networkAdLoader = NetworkAdLoader(networkLayer, parser, validator)
        stringAdLoader = StringAdLoader(parser, validator)
    }

    /**
     * call it when the vmap response is already present
     */
    @MainThread
    fun requestAds(request: StringAdRequest, renderer: AdRenderer) {
        Precondition.checkNonNull(request, "request cannot be null")
        if (request.response == null) {
            adErrorListener.onAdError(
                AdError(
                    AdErrorType.AD_REQUEST_MALFORMED,
                    StringErrorConstants.STRING_REQUEST_MALFORMED
                )
            )
            LogUtil.log("ad response cannot be empty")
        }
        loadRequest(stringAdLoader, request, renderer)
    }

    /**
     * call it when the vmap has to be fetched from the given url
     */
    @MainThread
    fun requestAds(request: NetworkAdRequest, renderer: AdRenderer) {
        Precondition.checkNonNull(request, "request cannot be null")
        if (request.url == null) {
            adErrorListener.onAdError(
                AdError(
                    AdErrorType.AD_REQUEST_MALFORMED,
                    StringErrorConstants.NETWORK_REQUEST_MALFORMED
                )
            )
        }
        loadRequest(networkAdLoader, request, renderer)
    }

    private fun <T : Request> loadRequest(adLoader: AdLoader<T>, request: T, renderer: AdRenderer) {
        adLoader.requestAds(request, {
            /** vmap is valid, fire onAdManagerLoaded callback **/
            LogUtil.log("onAdManagerLoaded triggered, adsManager created")
            val adsManager = DefaultAdManager(
                it,
                networkAdLoader,
                networkLayer,
                renderer,
                adEventListener,
                adErrorListener
            )
            adLoadListener.onAdManagerLoaded(adsManager)
        }, { adErrorType: AdErrorType, message: String? ->
            /** failure case **/
            adErrorListener.onAdError(AdError(adErrorType, message ?: StringErrorConstants.GENERIC_ERROR))
        })
    }

    class Builder {
        internal var logger: Logger? = null
        internal var mainThreadHandler: Handler? = null
        internal var renderer: DefaultAdRenderer? = null
        internal var adLoadListener: AdLoadListener? = null
        internal var adErrorListener: AdErrorListener? = null
        internal var adEventListener: AdEventListener? = null
        internal var parser: XmlParser? = null
        internal var networkLayer: NetworkLayer? = null

        fun setNetworkLayer(networkLayer: NetworkLayer): Builder {
            this.networkLayer = networkLayer
            return this
        }

        fun setParser(parser: XmlParser): Builder {
            this.parser = parser
            return this
        }

        fun setAdErrorListener(listener: AdErrorListener): Builder {
            this.adErrorListener = listener
            return this
        }

        fun setAdLoadListener(listener: AdLoadListener): Builder {
            this.adLoadListener = listener
            return this
        }

        fun setAdEventListener(listener: AdEventListener): Builder {
            this.adEventListener = listener
            return this
        }

        fun setLogger(logger: Logger): Builder {
            this.logger = logger
            return this
        }

        fun setMainThreadHandler(handler: Handler): Builder {
            this.mainThreadHandler = handler
            return this
        }

        fun build(context: Context): Madman {
            return Madman(this, context)
        }
    }
}
