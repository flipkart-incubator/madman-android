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
package com.flipkart.madman.renderer.binder

import android.view.View
import android.widget.TextView

/**
 * Ad ViewHolder class which holds the view inflated with help of [AdViewBinder]
 */
class AdViewHolder {
    /** skip view **/
    var skipView: TextView? = null

    /** ad count down view **/
    var adCountDownView: TextView? = null

    /** click through view **/
    var clickThroughView: TextView? = null

    fun from(container: View, binder: AdViewBinder): AdViewHolder {
        skipView = binder.skipViewId?.let { container.findViewById(it) }
        skipView?.visibility = View.GONE

        adCountDownView = binder.adCountDownViewId?.let { container.findViewById(it) }
        adCountDownView?.visibility = View.GONE

        clickThroughView = binder.clickThroughViewId?.let { container.findViewById(it) }
        clickThroughView?.visibility = View.GONE
        return this
    }
}
