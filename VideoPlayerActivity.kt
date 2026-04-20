package com.fbvideoplayer

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var originalSystemUiVisibility = 0

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Full screen window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Root layout
        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }
        setContentView(rootLayout)

        // Progress bar
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            isIndeterminate = false
            max = 100
        }
        rootLayout.addView(progressBar, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 8
        ))

        // WebView setup
        webView = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
        }
        rootLayout.addView(webView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        setupWebView()

        val videoUrl = intent.getStringExtra("VIDEO_URL") ?: return
        loadFacebookVideo(videoUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
            setSupportMultipleWindows(false)
            // Use desktop user agent for better Facebook video support
            userAgentString = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }

        // Accept Facebook cookies
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE

                // Inject CSS to hide Facebook navigation, ads, feed
                // Show only the video player
                val css = """
                    /* Hide Facebook navigation & clutter */
                    [data-pagelet="LeftRail"],
                    [data-pagelet="RightRail"],
                    [data-pagelet="FeedUnit"],
                    [aria-label="Facebook"],
                    [role="navigation"],
                    [data-testid="Keycommands_Dialog_Header"],
                    .__fb-light-mode header,
                    .x1n2onr6.x1vjfegm,
                    [data-pagelet="ProfileActions"],
                    [aria-label="Stories"],
                    .storyStream,
                    .ego_section,
                    ._1dwg, ._4-u2, ._4-u8 {
                        display: none !important;
                    }
                    
                    /* Make video container full screen */
                    [data-pagelet="WatchPermalinkVideo"],
                    [data-pagelet="VideoPlayer"],
                    .x1yztbdb.x1n2onr6 {
                        width: 100vw !important;
                        height: 100vh !important;
                        position: fixed !important;
                        top: 0 !important;
                        left: 0 !important;
                        z-index: 9999 !important;
                        background: black !important;
                    }
                    
                    video {
                        width: 100% !important;
                        max-height: 100vh !important;
                        background: black !important;
                    }
                    
                    body {
                        background: black !important;
                        overflow: hidden !important;
                    }
                """.trimIndent()

                val js = """
                    (function() {
                        var style = document.createElement('style');
                        style.type = 'text/css';
                        style.innerHTML = `$css`;
                        document.head.appendChild(style);
                        
                        // Auto-click play button if video is paused
                        setTimeout(function() {
                            var playBtn = document.querySelector('[aria-label="Play"]') ||
                                          document.querySelector('[data-testid="media-controls-play"]');
                            if (playBtn) playBtn.click();
                            
                            // Try to auto-fullscreen the video
                            var video = document.querySelector('video');
                            if (video) {
                                video.play().catch(function(){});
                            }
                        }, 1500);
                    })();
                """.trimIndent()

                view.evaluateJavascript(js, null)
            }

            override fun onReceivedError(
                view: WebView, request: WebResourceRequest, error: WebResourceError
            ) {
                // Silently handle errors for blocked trackers etc.
            }
        }

        // Handle fullscreen video (landscape)
        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
                if (newProgress == 100) progressBar.visibility = View.GONE
                else progressBar.visibility = View.VISIBLE
            }

            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                // Enter fullscreen video
                customView = view
                customViewCallback = callback
                originalSystemUiVisibility = window.decorView.systemUiVisibility

                val decorView = window.decorView as FrameLayout
                decorView.addView(view, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ))
                webView.visibility = View.GONE
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
            }

            override fun onHideCustomView() {
                // Exit fullscreen video
                customView?.let {
                    (window.decorView as FrameLayout).removeView(it)
                }
                customView = null
                webView.visibility = View.VISIBLE
                window.decorView.systemUiVisibility = originalSystemUiVisibility
                customViewCallback?.onCustomViewHidden()
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }
        }
    }

    private fun loadFacebookVideo(url: String) {
        progressBar.visibility = View.VISIBLE

        // Convert fb.watch short URLs & normalize
        val normalizedUrl = when {
            url.contains("fb.watch") -> url // Will redirect automatically
            url.contains("/watch?") -> url
            url.contains("/reel/") -> url
            url.contains("/videos/") -> url
            else -> url
        }

        webView.loadUrl(normalizedUrl)
    }

    override fun onBackPressed() {
        // Exit fullscreen first if active
        if (customView != null) {
            webView.webChromeClient?.onHideCustomView()
            return
        }
        // Then navigate back in webview
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }
}
