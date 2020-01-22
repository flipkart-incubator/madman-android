package com.flipkart.mediaads.demo.madman

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flipkart.mediaads.demo.R
import com.google.android.exoplayer2.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    private var playerView: PlayerView? = null
    private var player: MadmanPlayerManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.video_view);
        player = MadmanPlayerManager(
            this, intent.extras?.get("url").toString(),
            intent.extras?.get("response").toString()
        );
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
