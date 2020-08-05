package com.karhoo.uisdk.screen.web

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import kotlinx.android.synthetic.main.uisdk_activity_web.activityWebView
import kotlinx.android.synthetic.main.uisdk_activity_web.progressBar

class WebActivity : BaseActivity() {

    override val layout: Int
        get() = R.layout.uisdk_activity_web

    @SuppressLint("SetJavaScriptEnabled")
    override fun initialiseViews() {
        val url = extras?.getString(EXTRA_URL)

        activityWebView.apply {
            settings.apply {
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                allowFileAccess = true
                builtInZoomControls = false
                domStorageEnabled = true
                setSupportZoom(false)
            }
            isDrawingCacheEnabled = true
            setOnTouchListener(ScrollTouchListener())
            webViewClient = KarhooWebViewClient()
        }

        if (!url.isNullOrEmpty()) {
            activityWebView.loadUrl(url)
        } else {
            Log.e(TAG, "Unspecified URL")
        }

    }

    override fun initialiseViewListeners() {
    }

    public override fun handleExtras() {
    }

    private inner class KarhooWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            activityWebView?.loadUrl(url)
            return false
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            super.onReceivedSslError(view, handler, error)
            AlertDialog.Builder(this@WebActivity, R.style.AlertDialog)
                    .setMessage(R.string.notification_error_ssl_cert_invalid)
                    .setPositiveButton(R.string.continue_journey) { _, _ -> handler?.proceed() }
                    .setNegativeButton(R.string.cancel) { _, _ -> handler?.cancel() }
                    .create()
                    .show()
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            progressBar.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressBar.visibility = View.GONE
        }

    }

    private inner class ScrollTouchListener : View.OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return event.action == MotionEvent.ACTION_MOVE
        }
    }

    /**
     * Intent Builder
     */
    class Builder private constructor() {
        private val extras: Bundle = Bundle()

        fun url(url: String): Builder {
            extras.putString(EXTRA_URL, url)
            return this
        }

        fun build(context: Context): Intent {
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtras(extras)
            return intent
        }

        companion object {
            val builder: Builder
                get() = Builder()
        }
    }

    companion object {
        private val TAG = WebActivity::class.java.simpleName
        const val EXTRA_URL = "extra::url"
    }
}
