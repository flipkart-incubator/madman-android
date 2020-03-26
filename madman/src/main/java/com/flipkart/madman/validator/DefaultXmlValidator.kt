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
package com.flipkart.madman.validator

import com.flipkart.madman.component.model.vmap.AdBreak
import com.flipkart.madman.component.model.vmap.VMAPData

/**
 * Default implementation of [XmlValidator]
 */
class DefaultXmlValidator : XmlValidator {

    override fun validateVMAP(data: VMAPData): XmlValidator.Result {
        return if (data.version != null && !data.adBreaks.isNullOrEmpty()) {
            /** valid vmap **/
            Result(true, null)
        } else {
            if (data.version == null && !data.adBreaks.isNullOrEmpty()) {
                /** version is null **/
                Result(
                    false,
                    VMAP_VERSION_ERROR_MSG
                )
            } else if (data.adBreaks == null && data.version != null) {
                /** no ad break present **/
                Result(
                    false,
                    NO_AD_BREAK_ERROR_MSG
                )
            } else {
                /** both are empty **/
                Result(
                    false,
                    INVALID_VMAP_ERROR_MSG
                )
            }
        }
    }

    override fun validAdBreak(adBreak: AdBreak): XmlValidator.Result {
        return if (adBreak.breakType != null && adBreak.timeOffset != null && validAdBreakType(
                adBreak
            )
        ) {
            /** valid ad break **/
            Result(true, null)
        } else {
            if (adBreak.breakType == null || !validAdBreakType(adBreak)) {
                /** ad break is null, or invalid ad break **/
                Result(
                    false,
                    "$INVALID_AD_BREAK_ERROR_MSG $adBreak"
                )
            } else {
                /** time offset is empty **/
                Result(
                    false,
                    "$AD_BREAK_TIME_OFFSET_ERROR_MSG $adBreak"
                )
            }
        }
    }

    private fun validAdBreakType(adBreak: AdBreak): Boolean {
        return adBreak.breakType == AdBreak.BreakTypes.LINEAR || adBreak.breakType == AdBreak.BreakTypes.NON_LINEAR || adBreak.breakType == AdBreak.BreakTypes.DISPLAY
    }

    class Result(private val isValid: Boolean, private val message: String?) :
        XmlValidator.Result {
        override fun isValid(): Boolean {
            return isValid
        }

        override fun getMessage(): String? {
            return message
        }
    }

    companion object {
        const val INVALID_VMAP_ERROR_MSG = "vmap should have version and at-least one ad break"
        const val NO_AD_BREAK_ERROR_MSG = "vmap should have at-least one ad break"
        const val VMAP_VERSION_ERROR_MSG = "vmap version cannot be empty"
        const val AD_BREAK_TIME_OFFSET_ERROR_MSG = "timeOffset cannot be empty for"
        const val INVALID_AD_BREAK_ERROR_MSG =
            "break type for cannot be empty or should be one of {LINEAR, NON_LINEAR, DISPLAY} for"
    }
}
