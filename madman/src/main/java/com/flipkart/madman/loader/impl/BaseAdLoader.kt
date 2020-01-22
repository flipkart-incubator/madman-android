/*
 *
 *  * Copyright (C) 2019 Flipkart Internet Pvt Ltd
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.flipkart.madman.loader.impl

import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.enums.StringErrorConstants
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.loader.AdLoader
import com.flipkart.madman.network.model.Request
import com.flipkart.madman.parser.XmlParser
import com.flipkart.madman.validator.XmlValidator

/**
 * Base class of [AdLoader]
 */
abstract class BaseAdLoader<T : Request>(
    private val parser: XmlParser,
    private val xmlValidator: XmlValidator
) : AdLoader<T> {

    protected fun parseResponse(
        response: String,
        onSuccess: (data: VMAPData) -> Unit,
        onFailure: (errorType: AdErrorType, message: String?) -> Unit
    ) {
        parser.parse(response, object : XmlParser.ParserListener<VMAPData> {
            override fun onFailure(type: Int, message: String?) {
                /** parsing failed **/
                onFailure(AdErrorType.VMAP_MALFORMED_RESPONSE, message)
            }

            override fun onSuccess(t: VMAPData?) {
                t?.let {
                    val result = xmlValidator.validateVMAP(it)
                    if (result.isValid()) {
                        /** valid vmap **/
                        onSuccess(it)
                    } else {
                        /** invalid vmap **/
                        onFailure(AdErrorType.INVALID_VMAP, result.getMessage())
                    }
                } ?: run {
                    /** vmap is empty **/
                    onFailure(AdErrorType.INVALID_VMAP, StringErrorConstants.EMPTY_VMAP)
                }
            }
        })
    }
}
