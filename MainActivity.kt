package com.fbvideoplayer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri: Uri? = intent?.data

        if (uri != null) {
            val videoUrl = uri.toString()

            // Check if this is a Facebook video/watch/reel URL
            if (isFacebookVideoUrl(videoUrl)) {
                openVideoPlayer(videoUrl)
            } else {
                // Not a video URL - open in browser
                openInBrowser(videoUrl)
            }
        } else {
            Toast.makeText(this, "No URL received", Toast.LENGTH_SHORT).show()
        }

        finish() // Close this transparent activity
    }

    private fun isFacebookVideoUrl(url: String): Boolean {
        return url.contains("facebook.com/watch") ||
               url.contains("facebook.com/video") ||
               url.contains("facebook.com/reel") ||
               url.contains("fb.watch") ||
               url.contains("facebook.com/videos") ||
               url.contains("fbid=") ||
               url.contains("story_fbid=")
    }

    private fun openVideoPlayer(url: String) {
        val intent = Intent(this, VideoPlayerActivity::class.java).apply {
            putExtra("VIDEO_URL", url)
        }
        startActivity(intent)
    }

    private fun openInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            // Exclude our own app to avoid infinite loop
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}
