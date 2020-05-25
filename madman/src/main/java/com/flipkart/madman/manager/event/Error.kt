package com.flipkart.madman.manager.event

import com.flipkart.madman.component.enums.AdErrorType

enum class Error(val errorCode: Int, val errorMessage: String) {
    VAST_XML_PARSING_ERROR(100, "Vast xml parsing error"),
    VAST_SCHEMA_VALIDATION_ERROR(101, "VAST schema validation error"),
    VAST_VERSION_NOT_SUPPORTED(102, "VAST version of response not supported"),
    NO_AD_VAST_RESPONSE(303, "No Ads VAST response after one or more Wrappers"),
    NO_MEDIA_FILE_ERROR(401, "File not found. Unable to find Linear/MediaFile from URI."),

    VMAP_EMPTY_RESPONSE(100, "VMAP data cannot be null"),
    UNIDENTIFIED_ERROR(900, "Undefined Error");

    companion object {
        fun mapErrorTypeToError(errorType: AdErrorType): Error {
            when (errorType) {
                AdErrorType.NO_MEDIA_URL -> return NO_MEDIA_FILE_ERROR
                AdErrorType.NO_AD -> return NO_AD_VAST_RESPONSE
                else -> {
                }
            }
            return UNIDENTIFIED_ERROR
        }
    }
}
