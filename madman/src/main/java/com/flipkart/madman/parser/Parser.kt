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

import androidx.annotation.WorkerThread
import com.flipkart.madman.parser.exception.ParserException
import java.io.IOException

interface Parser<T> {

    /**
     * Parse and return [T]
     *
     * called on background thread
     * @throws IOException
     */
    @WorkerThread
    @Throws(ParserException::class)
    fun parse(xmlString: String): T?
}
