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
package com.flipkart.madman.manager.callback

import com.flipkart.madman.renderer.player.AdPlayer

/**
 * Default implementation of [AdPlayer.AdPlayerCallback]
 */
abstract class AdPlayerCallback : AdPlayer.AdPlayerCallback {
    override fun onPlay() {
        onAdPlay()
    }

    override fun onVolumeChanged(volume: Int) {

    }

    override fun onPause() {
        onAdPause()
    }

    override fun onLoaded() {

    }

    override fun onResume() {
        onAdResume()
    }

    override fun onEnded() {
        onAdEnded()
    }

    override fun onError() {
        onAdError()
    }

    override fun onBuffering() {

    }

    /**
     * on ad play callback from the player
     */
    protected abstract fun onAdPlay()

    /**
     * on ad ended callback from the player
     */
    protected abstract fun onAdEnded()

    /**
     * on ad error callback from the player
     */
    protected abstract fun onAdError()

    /**
     * on ad pause callback from the player
     */
    protected abstract fun onAdPause()

    /**
     * on ad resume callback from the player
     */
    protected abstract fun onAdResume()
}
