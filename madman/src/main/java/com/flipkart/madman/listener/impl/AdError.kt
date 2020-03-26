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
package com.flipkart.madman.listener.impl

import com.flipkart.madman.component.enums.AdErrorType
import com.flipkart.madman.listener.AdErrorListener

/**
 * Ad error implementation, passed as an argument in the error callback interface [AdErrorListener]
 *
 * errorType: [AdErrorType]
 * errorMessage: [String]
 */
class AdError(private val errorType: AdErrorType, private val errorMessage: String) :
    AdErrorListener.AdError {
    override fun getType(): AdErrorType {
        return errorType
    }

    override fun getMessage(): String {
        return errorMessage
    }
}
