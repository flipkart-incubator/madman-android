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
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.parser.helper.ParserErrorCode
import com.flipkart.madman.testutils.CurrentThreadExecutor
import com.flipkart.madman.testutils.VMAPUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class XmlParserTest {

    /**
     * Test to validate if correct parsers are invoked for vmap/vast
     */
    @Test
    fun testIfParsingIsDelegatedToCorrectParsers() {
        val mockVMAPParser = Mockito.mock(VMAPParser::class.java)
        val mockVASTParser = Mockito.mock(VASTParser::class.java)
        val mockParserListener = Mockito.mock(XmlParser.ParserListener::class.java)

        val parser = XmlParser.Builder()
            .setVMAPParser(mockVMAPParser)
            .setVASTParser(mockVASTParser)
            .build(Handler(), CurrentThreadExecutor())

        var response = VMAPUtil.readVMAPWithPreRoll()
        parser.parse(response, mockParserListener as XmlParser.ParserListener<VMAPData>)

        // verify that vmap parser's parse method gets called one time
        Mockito.verify(mockVMAPParser, times(1)).parse(anyString())
        // verify that on success is called once
        Mockito.verify(mockParserListener, times(1)).onSuccess(any())

        reset(mockParserListener)
        reset(mockVMAPParser)

        response = VMAPUtil.readVAST()
        parser.parse(response, mockParserListener)

        // verify that vast parser's parse method gets called one time
        Mockito.verify(mockVASTParser, times(1)).parse(anyString())
        // verify that on success is called once
        Mockito.verify(mockParserListener, times(1)).onSuccess(any())
    }

    /**
     * Test to validate exception is onFailure is called for corrupt xml
     */
    @Test
    fun testIfExceptionThrownForCorruptXml() {
        val mockVASTParser = Mockito.mock(VASTParser::class.java)
        val mockParserListener = Mockito.mock(XmlParser.ParserListener::class.java)

        val parser = XmlParser.Builder()
            .setVASTParser(mockVASTParser)
            .build(Handler(), CurrentThreadExecutor())

        val response = VMAPUtil.readCorruptVMAP()
        parser.parse(response, mockParserListener as XmlParser.ParserListener<VMAPData>)

        // verify that on success is called once
        Mockito.verify(mockParserListener, times(1))
            .onFailure(eq(ParserErrorCode.VMAP_PARSING_ERROR), any())
    }
}
