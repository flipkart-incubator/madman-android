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
package com.flipkart.madman.renderer

import com.flipkart.madman.manager.model.AdElement
import com.flipkart.madman.renderer.callback.ViewClickListener
import com.flipkart.madman.renderer.player.AdPlayerProvider
import com.flipkart.madman.renderer.settings.RenderingSettings

/**
 * AdRenderer
 *
 * Houses the logic for rendering the UI components for ad playback such as
 * skip ad button, learn more functionality.
 *
 * You can also register for click events via [ViewClickListener]
 */
interface AdRenderer : AdPlayerProvider {
    /**
     * Provides the [RenderingSettings] for ad
     */
    fun getRenderingSettings(): RenderingSettings

    /**
     * creates the view
     */
    fun createView()

    /**
     * render the view using [AdElement]
     */
    fun renderView(adElement: AdElement)

    /**
     * remove the view
     */
    fun removeView()

    /**
     * Called by [AdManager] to notify ad progress updates.
     * You can use this method to listen to progress events and customise views such as showing count down timer etc
     *
     * @param ad element
     * @param progress of the ad
     * @param duration of the ad
     */
    fun onAdProgressUpdate(adElement: AdElement, progress: Float, duration: Float)

    /**
     * destroy
     */
    fun destroy()

    /**
     * register [ViewClickListener]
     */
    fun registerViewClickListener(clickListener: ViewClickListener)

    /**
     * unregister [ViewClickListener]
     */
    fun unregisterViewClickListener(clickListener: ViewClickListener)
}
