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
import com.flipkart.madman.helper.Assertions
import com.flipkart.madman.listener.AdLoadListener
import com.flipkart.madman.listener.impl.AdError
import com.flipkart.madman.loader.AdLoader
import com.flipkart.madman.loader.impl.NetworkAdLoader
import com.flipkart.madman.loader.impl.StringAdLoader
import com.flipkart.madman.logger.DebugLevelLogger
import com.flipkart.madman.logger.LogUtil
import com.flipkart.madman.logger.Logger
import com.flipkart.madman.manager.DefaultAdManager
import com.flipkart.madman.manager.event.Error
import com.flipkart.madman.network.NetworkLayer
import com.flipkart.madman.network.model.NetworkAdRequest
import com.flipkart.madman.network.model.Request
import com.flipkart.madman.network.model.StringAdRequest
import com.flipkart.madman.parser.XmlParser
import com.flipkart.madman.renderer.AdRenderer
import com.flipkart.madman.renderer.DefaultAdRenderer
import com.flipkart.madman.validator.DefaultXmlValidator
import com.flipkart.madman.validator.XmlValidator

/**
 * This is the main class for using Madman. Madman instance is created using the
 * [Madman.Builder] class and then invoking [requestAds] on it. These methods should be called on the
 * main thread.
 *
 * <p>Here is an example of how Madman is used:
 *
 * <pre>
 * val madman = Madman.Builder()
 *      .setAdLoadListener(this)
 *      .setNetworkLayer(networkLayer)
 *      .build(context)
 *
 * madman.requestAds(StringAdRequest("response"), adRenderer)
 * </pre>
 *
 * The [AdLoadListener] is notified as soon as [AdManager] is created. The client uses the [AdManager]
 * instance to interact with the library.
 */
class Madman internal constructor(
    builder: Builder,
    context: Context
) {
    private var networkLayer: NetworkLayer
    private var adLoadListener: AdLoadListener
    private var mainThreadHandler: Handler
    private var xmlValidator: XmlValidator
    private var xmlParser: XmlParser

    init {
        LogUtil.setLogger(builder.logger ?: DebugLevelLogger())
        adLoadListener = builder.adLoadListener
            ?: throw IllegalStateException("adLoadListener cannot be null, implement AdLoadListener interface")
        networkLayer = builder.networkLayer
            ?: throw IllegalStateException("network layer cannot be null, implement NetworkLayer interface")
        mainThreadHandler = builder.mainThreadHandler ?: Handler(Looper.getMainLooper())
        xmlParser =
            builder.xmlParser ?: XmlParser.Builder().build(
                mainThreadHandler,
                AsyncTask.THREAD_POOL_EXECUTOR
            )
        xmlValidator = builder.xmlValidator ?: DefaultXmlValidator()
    }

    /**
     * Call this method when the ads response is already present
     *
     * @param [StringAdRequest] request object which contains the response
     * @param [AdRenderer] to render the ui
     */
    @MainThread
    fun requestAds(request: StringAdRequest, renderer: AdRenderer) {
        Assertions.checkMainThread()
        val stringAdLoader = StringAdLoader(xmlParser, xmlValidator)
        loadRequest(stringAdLoader, request, renderer)
    }

    /**
     * Call this method when ads response has to be fetched from the url
     *
     * @param [NetworkAdRequest] request object which contains the url
     * @param [AdRenderer] to render the ui
     */
    @MainThread
    fun requestAds(request: NetworkAdRequest, renderer: AdRenderer) {
        Assertions.checkMainThread()
        val networkAdLoader = NetworkAdLoader(networkLayer, xmlParser, xmlValidator)
        loadRequest(networkAdLoader, request, renderer)
    }

    private fun <T : Request> loadRequest(adLoader: AdLoader<T>, request: T, renderer: AdRenderer) {
        adLoader.requestAds(request, {
            /**
             * Given [VMAPData] is valid, create the [AdManager]
             * fire the onAdManagerLoaded event
             */
            LogUtil.log("AdManager created.")
            val adsManager = DefaultAdManager(
                it,
                networkLayer,
                xmlParser,
                xmlValidator,
                renderer
            )
            adLoadListener.onAdManagerLoaded(adsManager)
        }, { adErrorType: AdErrorType, message: String? ->
            /**
             * [VMAPData] is invalid due to the following error
             * fire the onAdManagerLoadFailed event
             */
            LogUtil.log("AdManager creation failed due to $adErrorType")
            adLoadListener.onAdManagerLoadFailed(
                AdError(
                    adErrorType,
                    message ?: Error.UNKNOWN_ERROR.errorMessage
                )
            )
        })
    }

    class Builder {
        internal var xmlValidator: XmlValidator? = null
        internal var logger: Logger? = null
        internal var mainThreadHandler: Handler? = null
        internal var renderer: DefaultAdRenderer? = null
        internal var adLoadListener: AdLoadListener? = null
        internal var xmlParser: XmlParser? = null
        internal var networkLayer: NetworkLayer? = null

        fun setNetworkLayer(networkLayer: NetworkLayer): Builder {
            this.networkLayer = networkLayer
            return this
        }

        fun setXmlParser(parser: XmlParser): Builder {
            this.xmlParser = parser
            return this
        }

        fun setAdLoadListener(listener: AdLoadListener): Builder {
            this.adLoadListener = listener
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

        fun setXmlValidator(xmlValidator: XmlValidator): Builder {
            this.xmlValidator = xmlValidator
            return this
        }

        fun build(context: Context): Madman {
            return Madman(this, context)
        }
    }
}
