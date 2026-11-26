package com.halilkilic.canlitvpro

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: FrameLayout
    private lateinit var webView: WebView
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            fileUploadCallback?.onReceiveValue(uris)
            fileUploadCallback = null
        }
    private var customVideoView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.parseColor("#0b1426")
        window.navigationBarColor = Color.parseColor("#0b1426")
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        rootLayout = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#0b1426"))
        }
        webView = WebView(this).apply {
            setBackgroundColor(Color.parseColor("#0b1426"))
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        rootLayout.addView(
            webView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        setContentView(rootLayout)
        applySystemBars()

        configureWebView()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (customVideoView != null) {
                    webView.webChromeClient?.onHideCustomView()
                    return
                }
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl("file:///android_asset/index.html")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val uri = request?.url ?: return false
                val scheme = uri.scheme?.lowercase() ?: return false
                if (scheme == "file" || scheme == "http" || scheme == "https") {
                    return false
                }
                return try {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (view == null) return
                if (customVideoView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customVideoView = view
                customViewCallback = callback
                rootLayout.addView(
                    view,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                webView.visibility = View.GONE
            }

            override fun onHideCustomView() {
                customVideoView?.let {
                    rootLayout.removeView(it)
                    customVideoView = null
                }
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
                webView.visibility = View.VISIBLE
                applySystemBars()
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = filePathCallback
                return try {
                    val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/*"
                    }
                    fileChooserLauncher.launch(intent)
                    true
                } catch (_: Exception) {
                    fileUploadCallback = null
                    false
                }
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }
    }

    private fun applySystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, rootLayout)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applySystemBars()
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val mappedKey = when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_NEXT -> "MediaTrackNext"
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> "MediaTrackPrevious"
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> "MediaPlayPause"
            KeyEvent.KEYCODE_MEDIA_PLAY -> "MediaPlay"
            KeyEvent.KEYCODE_MEDIA_PAUSE -> "MediaPause"
            KeyEvent.KEYCODE_MEDIA_STOP -> "MediaStop"
            KeyEvent.KEYCODE_CHANNEL_UP -> "ChannelUp"
            KeyEvent.KEYCODE_CHANNEL_DOWN -> "ChannelDown"
            else -> null
        }
        if (mappedKey != null) {
            val js = "document.dispatchEvent(new KeyboardEvent('keydown', {key: '$mappedKey', bubbles: true}));"
            webView.evaluateJavascript(js, null)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
