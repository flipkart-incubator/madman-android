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
package com.flipkart.madman.okhttp.extension.helper

import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.webkit.WebSettings
import java.util.*

object HeaderUtils {

    /**
     * returns the [Locale]
     */
    fun getLanguage(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault().toLanguageTags()
        } else {
            Locale.getDefault().language
        }
    }

    /**
     * returns package name of the running application
     * @param context
     */
    fun getPackageName(context: Context): String {
        return context.applicationContext.packageName
    }

    /**
     * returns the user agent
     * @param context
     */
    fun getUserAgent(context: Context): String {
        return try {
            WebSettings.getDefaultUserAgent(context);
        } catch (e: Exception) {
            getDefaultUserAgent()
        }
    }

    /**
     * builds a custom user agent in cases where the
     * [WebSettings].getDefaultUserAgent(context) throws an exception
     */
    private fun getDefaultUserAgent(): String {
        val result = StringBuilder(128)
        result.append("Mozilla/5.0 (Linux; Android ") //NON-NLS

        //Android version
        val version = Build.VERSION.RELEASE
        result.append(if (version.isNotEmpty()) version else "8.0")

        // add the model for the release build
        if ("REL" == Build.VERSION.CODENAME) { //NON-NLS
            val model = Build.MODEL
            if (model.isNotEmpty()) {
                result.append("; ")
                result.append(model)
            }
        }
        val id = Build.ID
        if (id.isNotEmpty()) {
            result.append(" Build/") //NON-NLS
            result.append(id)
        }
        result.append(")")
        return result.toString()
    }
}
