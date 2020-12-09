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
package com.flipkart.mediaads.demo.helper

import android.content.Context
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream

object Utils {
    @JvmStatic
    fun readFromAssets(context: Context, fileName: String): String {
        val inputStream = context.assets.open(fileName)

        val bis = BufferedInputStream(inputStream)
        val buf = ByteArrayOutputStream()
        var result = bis.read()
        while (result != -1) {
            val b = result.toByte()
            buf.write(b.toInt())
            result = bis.read()
        }

        return buf.toString()
    }

    @JvmStatic
    fun readResponseFor(context: Context, responseId: Int): String {
        return when (responseId) {
            0 -> {
                readFromAssets(context, "ad_response.xml")
            }
            else -> readFromAssets(context, "ad_response.xml")
        }
    }
}
