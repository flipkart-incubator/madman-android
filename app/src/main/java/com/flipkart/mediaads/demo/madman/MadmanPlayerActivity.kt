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

import android.os.Bundle
import android.text.SpannableString
import androidx.appcompat.app.AppCompatActivity
import com.flipkart.mediaads.demo.R
import com.google.android.exoplayer2.ui.PlayerView

class MadmanPlayerActivity : AppCompatActivity() {
    private var playerView: PlayerView? = null
    private var player: MadmanPlayerManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.video_view)
        player = MadmanPlayerManager(
            this,
            intent.extras?.get("url").toString(),
            intent.extras?.getInt("response_option") ?: 0
        )
    }

    public override fun onResume() {
        super.onResume()
        player?.init(this, playerView)
    }

    public override fun onPause() {
        super.onPause()
        player?.reset()
    }

    public override fun onDestroy() {
        player?.release()
        super.onDestroy()
    }
}
