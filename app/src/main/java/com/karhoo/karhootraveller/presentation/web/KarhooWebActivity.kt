package com.karhoo.karhootraveller.presentation.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
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
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.util.VersionUtil
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.screen.web.prepopulateForUser
import com.karhoo.uisdk.util.formattedTripId
import kotlinx.android.synthetic.main.activity_web.progressBar
import kotlinx.android.synthetic.main.activity_web.webView

class KarhooWebActivity : BaseActivity() {

    private var tripId: String? = null
    private var isScrollable: Boolean = false

    override val layout: Int
        get() = R.layout.activity_web

    @SuppressLint("SetJavaScriptEnabled")
    override fun initialiseViews() {
        tripId = extras?.getString(EXTRA_TRIP_ID, null)
        isScrollable = extras?.getBoolean(EXTRA_SCROLLABLE, true) ?: true
        val url = extras?.getString(EXTRA_URL)

        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                allowFileAccess = true
                builtInZoomControls = false
                domStorageEnabled = true
                setSupportZoom(false)
            }
            isDrawingCacheEnabled = true
            setOnTouchListener(DisableScrollTouchListener())
            webViewClient = KarhooWebViewClient()
        }

        if (!url.isNullOrEmpty()) {
            if (url.contains(INTERCEPT_HELP_STRING) == true) {
                webView.loadData(prepopulateForUser(KarhooApi.userStore.currentUser,
                                                    formattedTripId(this, tripId) + "\n" +
                                                            VersionUtil.appAndDeviceInfo() +
                                                            VersionUtil.getVersionString(applicationContext), applicationContext),
                                 TYPE_TEXT_HTML, UTF_8)
            } else {
                webView.loadUrl(url)
            }

        } else {
            Log.e(TAG, "Unspecified URL")
        }

    }

    override fun initialiseViewListeners() {
        // Do nothing
    }

    override fun handleExtras() {
        // Do nothing
    }

    private inner class KarhooWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            webView?.loadUrl(url)
            return false
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            super.onReceivedSslError(view, handler, error)

            val config = KarhooAlertDialogConfig(
                    messageResId = R.string.notification_error_ssl_cert_invalid,
                    positiveButton = KarhooAlertDialogAction(R.string.continue_journey,
                                                             DialogInterface.OnClickListener { _, _ -> handler?.proceed() }),
                    negativeButton = KarhooAlertDialogAction(R.string.cancel,
                                                             DialogInterface.OnClickListener { _, _ -> handler?.cancel() }))
            KarhooAlertDialogHelper(this@KarhooWebActivity).showAlertDialog(config)
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
            if (url?.contains(INTERCEPT_HELP_STRING) == true) {
                webView.loadData(prepopulateForUser(KarhooApi.userStore.currentUser,
                                                    formattedTripId(this@KarhooWebActivity, tripId) + "\n" +
                                                            VersionUtil.appAndDeviceInfo() +
                                                            VersionUtil.getVersionString(applicationContext), applicationContext),
                                 TYPE_TEXT_HTML, UTF_8)
            } else {
                super.onLoadResource(view, url)
            }
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

    private inner class DisableScrollTouchListener : View.OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return if (isScrollable) {
                false
            } else {
                event.action == MotionEvent.ACTION_MOVE
            }
        }
    }

    /**
     * Intent Builder
     */
    class Builder private constructor() {
        private val extras: Bundle = Bundle()
        private var isScrollable: Boolean = false

        fun url(url: String): Builder {
            extras.putString(EXTRA_URL, url)
            return this
        }

        fun tripId(tripId: String): Builder {
            extras.putString(EXTRA_TRIP_ID, tripId)
            return this
        }

        fun setScrollable(scrollable: Boolean): Builder {
            this.isScrollable = scrollable
            extras.putBoolean(EXTRA_SCROLLABLE, isScrollable)
            return this
        }

        fun build(context: Context): Intent {
            val intent = Intent(context, KarhooWebActivity::class.java)
            intent.putExtras(extras)
            return intent
        }

        companion object {
            val builder: Builder
                get() = Builder()
        }
    }

    companion object {
        private val TAG = KarhooWebActivity::class.java.simpleName
        const val EXTRA_URL = "extra::url"
        const val EXTRA_TRIP_ID = "extra::tripid"
        const val EXTRA_SCROLLABLE = "extra::scrollable"
        @Deprecated("Use ContactEmailProvider instead")
        const val INTERCEPT_HELP_STRING = "CreateRecordForm"
        const val TYPE_TEXT_HTML = "text/html"
        const val UTF_8 = "UTF-8"
    }
}
