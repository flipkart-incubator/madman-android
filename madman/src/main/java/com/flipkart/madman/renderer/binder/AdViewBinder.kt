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

import com.flipkart.mediaads.sdk.R

/**
 * View Binder class to specify ids for the UI elements.
 *
 * Pass this in the builder of DefaultAdRenderer
 */
class AdViewBinder private constructor(builder: Builder) {

    /** layout id **/
    val layoutToInflateId: Int

    /** skip view button id **/
    var skipViewId: Int? = null

    /** click through id eg learn more **/
    var clickThroughViewId: Int? = null

    /** ad starting in button id **/
    var adStartingInViewId: Int? = null

    init {
        layoutToInflateId = builder.layoutToInflateId
        skipViewId = builder.skipViewId
        adStartingInViewId = builder.adStartingInViewId
        clickThroughViewId = builder.clickThroughViewId
    }

    class Builder {
        internal var skipViewId: Int = R.id.skip_view
        internal var clickThroughViewId: Int = R.id.click_through
        internal var layoutToInflateId: Int = R.layout.ad_layout
        internal var adStartingInViewId: Int = R.id.ad_starting_in_view

        fun setLayoutId(id: Int): Builder {
            this.layoutToInflateId = id
            return this
        }

        fun setSkipViewId(id: Int): Builder {
            this.skipViewId = id
            return this
        }

        fun setClickThroughViewId(id: Int): Builder {
            this.clickThroughViewId = id
            return this
        }

        fun setAdStartingInViewId(id: Int): Builder {
            this.adStartingInViewId = id
            return this
        }

        fun build(): AdViewBinder {
            return AdViewBinder(this)
        }
    }
}
