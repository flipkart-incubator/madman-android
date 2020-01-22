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

package com.flipkart.madman.renderer.callback

/**
 * View callback interface
 *
 * Attach callback to listen to click and other events
 */
interface ViewClickListener {
    /**
     * called when skip ad is clicked
     */
    fun onSkipAdClick()

    /**
     * called when ad view is clicked
     */
    fun onAdViewClick()

    /**
     * called when learn more is clicked
     */
    fun onClickThroughClick()
}
