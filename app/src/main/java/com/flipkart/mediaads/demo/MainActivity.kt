package com.flipkart.mediaads.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flipkart.mediaads.demo.madman.PlayerActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            if (checkbox.isChecked) {
                val intent = Intent(this@MainActivity, PlayerActivity::class.java)
                intent.putExtra("url", url.text)
                intent.putExtra("response", response.text)
                startActivity(intent)
            } else {
                val intent = Intent(
                    this@MainActivity,
                    com.flipkart.mediaads.demo.ima.PlayerActivity::class.java
                )
                startActivity(intent)
            }
        }
    }
}
