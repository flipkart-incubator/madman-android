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
package com.flipkart.mediaads.demo.madman

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.flipkart.madman.exo.extension.MadmanAdLoader
import com.flipkart.madman.okhttp.extension.DefaultNetworkLayer
import com.flipkart.mediaads.demo.R
import com.flipkart.mediaads.demo.helper.Utils.readResponseFor
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MadmanPlayerManager(
    context: Context,
    url: String?,
    responseId: Int
) : AdsMediaSource.MediaSourceFactory {
    private var adsLoader: MadmanAdLoader? = null
    private val dataSourceFactory: DataSource.Factory
    private var player: SimpleExoPlayer? = null
    private var contentPosition: Long = 0

    fun init(
        context: Context,
        playerView: PlayerView?
    ) {
        // Create a player instance.
        player = ExoPlayerFactory.newSimpleInstance(context)
        adsLoader?.setPlayer(player)
        playerView?.player = player

        // This is the MediaSource representing the content media (i.e. not the ad).
        val contentUrl = context.getString(R.string.content_url)
        val contentMediaSource = buildMediaSource(Uri.parse(contentUrl))
        // Compose the content media source into a new AdsMediaSource with both ads and content.
        val mediaSourceWithAds: MediaSource = AdsMediaSource(
            contentMediaSource,  /* adMediaSourceFactory= */this, adsLoader, playerView
        )
        // Prepare the player with the source.
        player?.seekTo(contentPosition)
        player?.prepare(mediaSourceWithAds)
        player?.playWhenReady = true
    }

    fun reset() {
        contentPosition = player?.contentPosition ?: 0
        player?.release()
        player = null
        adsLoader?.setPlayer(null)
    }

    fun release() {
        player?.release()
        player = null
        adsLoader?.release()
    }

    // AdsMediaSource.MediaSourceFactory implementation.
    override fun createMediaSource(uri: Uri): MediaSource {
        return buildMediaSource(uri)
    }

    override fun getSupportedTypes(): IntArray { // IMA does not support Smooth Streaming ads.
        return intArrayOf(
            C.TYPE_DASH,
            C.TYPE_HLS,
            C.TYPE_OTHER
        )
    }

    // Internal methods.
    private fun buildMediaSource(uri: Uri): MediaSource {
        @C.ContentType val type =
            Util.inferContentType(uri)
        return when (type) {
            C.TYPE_DASH -> DashMediaSource.Factory(
                dataSourceFactory
            ).createMediaSource(uri)
            C.TYPE_SS -> SsMediaSource.Factory(
                dataSourceFactory
            ).createMediaSource(uri)
            C.TYPE_HLS -> HlsMediaSource.Factory(
                dataSourceFactory
            ).createMediaSource(uri)
            C.TYPE_OTHER -> ExtractorMediaSource.Factory(
                dataSourceFactory
            ).createMediaSource(uri)
            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }

    init {
        val builder =
            MadmanAdLoader.Builder(
                context,
                DefaultNetworkLayer.Builder().build(context)
            )
        adsLoader = if (!TextUtils.isEmpty(url)) {
            builder.buildForAdUri(Uri.parse(url))
        } else {
            val stringResponse = readResponseFor(context, responseId)
            builder.buildForAdsResponse(stringResponse)
        }
        dataSourceFactory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(
                context,
                context.getString(R.string.app_name)
            )
        )
    }
}
