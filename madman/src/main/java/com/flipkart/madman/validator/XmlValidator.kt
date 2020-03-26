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
 * Validator which validates xml data structure
 */
interface XmlValidator {

    /** validate vmap [VMAPData] **/
    fun validateVMAP(data: VMAPData): Result

    /** validate ad break [AdBreak] **/
    fun validAdBreak(adBreak: AdBreak): Result

    interface Result {
        /** is valid **/
        fun isValid(): Boolean

        /** reason of the failure **/
        fun getMessage(): String?
    }
}
