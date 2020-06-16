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

/**
 * View Binder class to specify ids for the UI elements.
 *
 * Pass this in the builder of DefaultAdRenderer
 */
class AdViewBinder private constructor(
    builder: Builder,
    layoutId: Int
) {
    /** layout id **/
    val layoutToInflateId: Int = layoutId

    /** skip view button id **/
    var skipViewId: Int? = null

    /** click through id eg learn more **/
    var clickThroughViewId: Int? = null

    /** ad count down in button id **/
    var adCountDownViewId: Int? = null

    init {
        skipViewId = builder.skipViewId
        adCountDownViewId = builder.adCountDownViewId
        clickThroughViewId = builder.clickThroughViewId
    }

    class Builder {
        internal var skipViewId: Int? = null
        internal var clickThroughViewId: Int? = null
        internal var adCountDownViewId: Int? = null

        fun setSkipViewId(id: Int): Builder {
            this.skipViewId = id
            return this
        }

        fun setClickThroughViewId(id: Int): Builder {
            this.clickThroughViewId = id
            return this
        }

        fun setAdCountDownViewId(id: Int): Builder {
            this.adCountDownViewId = id
            return this
        }

        fun build(layoutId: Int): AdViewBinder {
            return AdViewBinder(this, layoutId)
        }
    }
}
