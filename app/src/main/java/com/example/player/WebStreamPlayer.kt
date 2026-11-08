package com.example.player

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.ui.theme.BrandRed

private fun rootHost(host: String?): String =
    host.orEmpty().lowercase().split('.').takeLast(2).joinToString(".")

/**
 * Plays pages and embeds (iframe players, YouTube, hosted movie pages) inside the app.
 *
 * - video fullscreen button works (custom view is hosted inside the app)
 * - autoplay allowed, mixed content allowed, third party cookies on
 * - popups and ad redirects to other sites are blocked
 * - back press first leaves fullscreen
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebStreamPlayer(
    url: String,
    modifier: Modifier = Modifier,
    embedHtml: String? = null,
    embedBaseUrl: String = "https://www.youtube.com",
    onMainFrameError: () -> Unit = {}
) {
    var customView by remember { mutableStateOf<View?>(null) }
    var customCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var progress by remember { mutableIntStateOf(0) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    fun leaveFullscreen() {
        if (customView != null) {
            val cb = customCallback
            customView = null
            customCallback = null
            cb?.onCustomViewHidden()
        }
    }

    BackHandler(enabled = customView != null) { leaveFullscreen() }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { webView?.onPause() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { webView?.onResume() }

    val originalRoot = remember(url) { rootHost(Uri.parse(url).host) }

    Box(modifier = modifier.background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        javaScriptCanOpenWindowsAutomatically = false
                        setSupportMultipleWindows(false)
                        userAgentString = PLAYER_USER_AGENT
                    }
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                        }

                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            if (view == null) {
                                callback?.onCustomViewHidden()
                                return
                            }
                            customView = view
                            customCallback = callback
                        }

                        override fun onHideCustomView() {
                            leaveFullscreen()
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val target = request?.url ?: return true
                            val scheme = target.scheme?.lowercase()
                            if (scheme != "http" && scheme != "https") return true
                            // Block ad style jumps to another site that the viewer did not ask for.
                            if (embedHtml == null && request.isForMainFrame && request.hasGesture() &&
                                rootHost(target.host) != originalRoot
                            ) return true
                            return false
                        }

                        override fun onPageStarted(view: WebView?, pageUrl: String?, favicon: Bitmap?) {
                            progress = 5
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            if (request?.isForMainFrame == true) onMainFrameError()
                        }
                    }

                    if (embedHtml != null) {
                        loadDataWithBaseURL(embedBaseUrl, embedHtml, "text/html", "UTF-8", null)
                    } else {
                        loadUrl(url)
                    }
                    webView = this
                }
            },
            onRelease = { view ->
                view.stopLoading()
                view.destroy()
            }
        )

        customView?.let { cv ->
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                factory = { cv }
            )
        }

        if (progress in 1..99 && customView == null) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = BrandRed,
                trackColor = Color.Transparent
            )
        }
    }
}

/** HTML wrapper for a YouTube iframe that fills the screen. */
fun youTubeEmbedHtml(videoId: String): String = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <style>
            * { margin: 0; padding: 0; }
            html, body { width: 100%; height: 100%; background: #000; overflow: hidden; }
            iframe { position: absolute; inset: 0; width: 100%; height: 100%; border: 0; }
        </style>
    </head>
    <body>
        <iframe
            src="https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1&rel=0&modestbranding=1&fs=1&origin=https://www.youtube.com"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen"
            allowfullscreen>
        </iframe>
    </body>
    </html>
""".trimIndent()
