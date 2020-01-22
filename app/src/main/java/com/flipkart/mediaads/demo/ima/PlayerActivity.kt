package com.flipkart.mediaads.demo.ima

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flipkart.mediaads.demo.R
import com.google.android.exoplayer2.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    private var playerView: PlayerView? = null
    private var player: PlayerManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.video_view);
        player = PlayerManager(this);
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
