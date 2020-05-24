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
package com.flipkart.madman.component.enums

/**
 * List of all ad errors.
 */
enum class AdErrorType {
    AD_REQUEST_NETWORK_FAILURE,
    EMPTY_VMAP_RESPONSE,
    VMAP_XML_PARSING_ERROR,
    VMAP_SCHEMA_VALIDATION_ERROR,
    EMPTY_VAST_RESPONSE,
    VAST_XML_PARSING_ERROR,
    VAST_SCHEMA_VALIDATION_ERROR,

    NO_MEDIA_URL,
    INVALID_CUE_POINTS,
    NO_AD,
    INTERNAL_ERROR,
    AD_ERROR,
    UNKNOWN_ERROR
}
