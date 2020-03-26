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
package com.flipkart.madman.parser

import android.os.Handler
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.flipkart.madman.component.model.vast.VASTData
import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.component.model.vmap.AdSource
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.parser.exception.ParserException
import com.flipkart.madman.parser.helper.ParserErrorCode
import com.flipkart.madman.parser.helper.XmlParserHelper
import com.flipkart.madman.parser.helper.XmlParserHelper.skip
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.StringReader
import java.util.concurrent.Executor

/**
 * The default parser class delegates the parsing of VMAP and VAST to their respective parsers
 *
 * 1. [VMAPParser] for [VMAPData]
 * 2. [VASTParser] for [VASTData]
 *
 * It allows to override the implementation of both the parsers if required.
 *
 * @author anirudhramanan
 */
class XmlParser private constructor(
    private val pullParser: XmlPullParser,
    private val vmapParser: Parser<VMAPData>,
    private val vastParser: Parser<VASTData>,
    private val handler: Handler,
    private val executor: Executor
) {

    interface ParserListener<T> {
        /**
         * Called when parsing is complete
         *
         * called on main thread
         */
        @MainThread
        fun onSuccess(t: T?)

        /**
         * Called when parsing fails
         *
         * called on main thread
         */
        @MainThread
        fun onFailure(type: Int, message: String?)
    }

    fun parse(xmlString: String, listener: ParserListener<VMAPData>) {
        /** execute on background thread **/
        executor.execute {
            try {
                val data = parseOnBackgroundThread(xmlString)
                handler.post {
                    /** post on main thread **/
                    listener.onSuccess(data)
                }
            } catch (e: ParserException) {
                handler.post {
                    /** post on main thread **/
                    listener.onFailure(e.errorCode, e.message)
                }
            } catch (e: IOException) {
                handler.post {
                    /** post on main thread **/
                    listener.onFailure(ParserErrorCode.PARSING_ERROR, e.message)
                }
            }
        }
    }

    @WorkerThread
    @Throws(ParserException::class, IOException::class)
    private fun parseOnBackgroundThread(xmlString: String): VMAPData? {
        pullParser.setInput(StringReader(xmlString))
        var event = pullParser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (pullParser.name) {
                        VMAPData.VMAP_XML_TAG -> {
                            /** delegate to [VMAPParser] **/
                            return vmapParser.parse(xmlString)
                        }
                        VASTData.VAST_XML_TAG -> {
                            /** delegate to [VASTParser] **/
                            val vastData = vastParser.parse(xmlString)
                            return wrapVASTInVMAP(vastData)
                        }
                        else -> {
                            skip(pullParser)
                        }
                    }
                }
            }
            event = pullParser.next()
        }
        return null
    }

    private fun wrapVASTInVMAP(vastData: VASTData?): VMAPData {
        val data = VMAPData()
        data.adBreaks =
            listOf(AdBreak().apply {
                adSource = AdSource().apply {
                    vastAdData = vastData
                }
            })
        data.version = "1.0"
        return data
    }

    class Builder {
        private var pullParser: XmlPullParser? = null
        private var vmapParser: Parser<VMAPData>? = null
        private var vastParser: Parser<VASTData>? = null

        fun setXmlPullParser(pullParser: XmlPullParser): Builder {
            this.pullParser = pullParser
            return this
        }

        fun setVMAPParser(vmapParser: Parser<VMAPData>): Builder {
            this.vmapParser = vmapParser
            return this
        }

        fun setVASTParser(vastParser: Parser<VASTData>): Builder {
            this.vastParser = vastParser
            return this
        }

        fun build(handler: Handler, executor: Executor): XmlParser {
            val pullParser = pullParser ?: XmlParserHelper.createNewParser()
            val vastParser = vastParser ?: VASTParser(
                pullParser
            )
            val vmapParser = vmapParser ?: VMAPParser(
                pullParser,
                vastParser
            )
            return XmlParser(
                pullParser,
                vmapParser,
                vastParser,
                handler,
                executor
            )
        }
    }
}
