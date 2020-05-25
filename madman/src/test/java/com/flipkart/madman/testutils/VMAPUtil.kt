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

package com.flipkart.madman.testutils

import android.os.Handler
import com.flipkart.madman.component.model.vast.VASTData
import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.component.model.vmap.AdSource
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.parser.Parser
import com.flipkart.madman.parser.VASTParser
import com.flipkart.madman.parser.XmlParser
import com.flipkart.madman.parser.helper.XmlParserHelper
import java.io.StringReader
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object VMAPUtil {
    private val lock = CountDownLatch(1)

    fun createVMAP(version: String?, adBreaks: List<AdBreak>?): VMAPData {
        val data = VMAPData()
        data.version = version
        data.adBreaks = adBreaks
        return data
    }

    fun createAdBreak(breakType: String?, timeOffset: String?): AdBreak {
        val adBreak = AdBreak()
        adBreak.breakType = breakType
        adBreak.timeOffset = timeOffset
        return adBreak
    }

    fun createVMAPWithNoAds(): VMAPData {
        val data = VMAPData()
        data.version = "3.0"

        val adBreaks = mutableListOf<AdBreak>()
        adBreaks.add(AdBreak().apply {
            adSource = AdSource().apply {
                vastAdData = VASTData().apply {
                    version = "1.0"
                }
            }
        })

        data.adBreaks = adBreaks
        return data
    }

    fun createVMAPWithAdTagURI(): VMAPData {
        var result = VMAPData()
        XmlParser.Builder().build(Handler(), CurrentThreadExecutor())
            .parse(
                XmlUtil.readString("vmap_with_ad_uri.xml"),
                object : XmlParser.ParserListener<VMAPData> {
                    override fun onSuccess(t: VMAPData?) {
                        result = t ?: result
                        lock.countDown()
                    }

                    override fun onFailure(type: Int, message: String?) {
                    }
                })
        lock.await(2000, TimeUnit.MILLISECONDS)
        return result
    }

    fun createVMAP(preRoll: Boolean): VMAPData {
        var result = VMAPData()
        XmlParser.Builder().build(Handler(), CurrentThreadExecutor())
            .parse(
                if (preRoll) readVMAPWithPreRoll() else readVMAP(),
                object : XmlParser.ParserListener<VMAPData> {
                    override fun onSuccess(t: VMAPData?) {
                        result = t ?: result
                        lock.countDown()
                    }

                    override fun onFailure(type: Int, message: String?) {
                    }
                })
        lock.await(2000, TimeUnit.MILLISECONDS)
        return result
    }

    fun createVAST(): VASTData {
        val value = readVAST()
        return createVASTParser(value).parse(value) ?: VASTData()
    }

    fun readVMAPWithPreRoll(): String {
        return XmlUtil.readString("vmap_with_preroll.xml")
    }

    fun readVMAPWithoutPostRoll(): String {
        return XmlUtil.readString("vmap_without_post_roll.xml")
    }

    fun readVMAP(): String {
        return XmlUtil.readString("vmap.xml")
    }

    fun readVMAPWithReorderedTags(): String {
        return XmlUtil.readString("vmap_reordered.xml")
    }

    fun readVMAPWithNoAdSource(): String {
        return XmlUtil.readString("vmap_with_no_ad_source.xml")
    }

    fun readVAST(): String {
        return XmlUtil.readString("vast.xml")
    }

    fun readCorruptVAST(): String {
        return XmlUtil.readString("corrupt_vast.xml")
    }

    fun readCorruptVMAP(): String {
        return XmlUtil.readString("corrupt_vmap.xml")
    }

    private fun createVASTParser(xmlString: String): Parser<VASTData> {
        val xmlPullParser = XmlParserHelper.createNewParser()
        xmlPullParser.setInput(StringReader(xmlString))
        xmlPullParser.nextTag()
        return VASTParser(xmlPullParser)
    }
}
