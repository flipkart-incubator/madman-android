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

package com.flipkart.madman.component.enums

/**
 * vast error codes
 */
object VastErrors {
    private const val XML_PARSING_ERROR = 100
    private const val VAST_SCHEMA_VALIDATION_ERROR = 101

    const val VAST_VERSION_NOT_SUPPORTED = 102
    const val TRAFFICKING_ERROR = 200
    const val DIFFERENT_LINEARITY_ERROR = 201
    const val DIFFERENT_DURATION_ERROR = 202
    const val DIFFERENT_SIZE_ERROR = 203

    const val GENERAL_WRAPPER_ERROR = 300
    const val TIMEOUT_ERROR = 301
    const val WRAPPER_LIMIT_ERROR = 302
    private const val NO_VAST_ADS = 303

    const val LINEAR_ERROR = 400
    private const val NO_MEDIA_FILE_ERROR = 401
    const val TIMEOUT_MEDIA_FILE_ERROR = 402
    const val NOT_SUPPORTED_MEDIA_FILE_ERROR = 403
    const val MEDIA_FILE_ERROR = 405

    private const val UNDEFINED_ERROR = 900

    fun mapErrorTypeToInt(errorType: AdErrorType): Int {
        when (errorType) {
            AdErrorType.NO_AD -> return NO_VAST_ADS
            AdErrorType.VAST_ERROR -> return UNDEFINED_ERROR
            AdErrorType.VMAP_MALFORMED_RESPONSE -> return XML_PARSING_ERROR
            AdErrorType.INVALID_VMAP -> return VAST_SCHEMA_VALIDATION_ERROR
            AdErrorType.NO_MEDIA_URL -> return NO_MEDIA_FILE_ERROR
        }
        return UNDEFINED_ERROR
    }
}
