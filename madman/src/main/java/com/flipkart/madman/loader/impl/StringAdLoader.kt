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
package com.flipkart.madman.loader.impl

import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.component.model.vmap.VMAPData
import com.flipkart.madman.network.model.StringAdRequest
import com.flipkart.madman.parser.XmlParser
import com.flipkart.madman.validator.XmlValidator

/**
 * The [StringAdLoader] expects that the xml response is already present.
 *
 * This directly delegates the response string to the parser
 */
class StringAdLoader(
    parser: XmlParser,
    xmlValidator: XmlValidator
) : BaseAdLoader<StringAdRequest>(parser, xmlValidator) {

    override fun requestAds(
        param: StringAdRequest,
        onSuccess: (data: VMAPData) -> Unit,
        onFailure: (errorType: AdErrorType, message: String?) -> Unit
    ) {
        parseResponse(param, param.response, onSuccess, onFailure)
    }
}
