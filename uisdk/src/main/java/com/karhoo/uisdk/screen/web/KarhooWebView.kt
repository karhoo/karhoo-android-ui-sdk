package com.karhoo.uisdk.screen.web

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.http.SslError
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.util.VersionUtil
import com.karhoo.uisdk.util.formattedTripId
import kotlinx.android.synthetic.main.uisdk_view_web.view.khWebViewToolbar
import kotlinx.android.synthetic.main.uisdk_view_web.view.progressBar
import kotlinx.android.synthetic.main.uisdk_view_web.view.webView

class KarhooWebView @JvmOverloads constructor(context: Context,
                                              attrs: AttributeSet? = null,
                                              defStyleAttr: Int = 0
                                             ) : FrameLayout(context, attrs, defStyleAttr) {

    private var tripId: String = ""

    init {
        View.inflate(context, R.layout.uisdk_view_web, this)

        (context as AppCompatActivity).setSupportActionBar(khWebViewToolbar)
        khWebViewToolbar.setNavigationOnClickListener {
            (context as AppCompatActivity).onBackPressed()
        }
        (context as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (context as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (context as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        if (!isInEditMode) {
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
                webViewClient = KarhooWebViewClient()
            }
        }
    }

    fun show(url: String,
             tripId: String = "",
             isScrollable: Boolean = true) {

        this.tripId = tripId

        webView.setOnTouchListener(DisableScrollTouchListener(isScrollable))

        if (url.isNotEmpty()) {
            if (url.contains(INTERCEPT_HELP_STRING)) {
                webView.loadData(prepopulateForUser(KarhooApi.userStore.currentUser,
                                                    formattedTripId(context, tripId) + "\n" +
                                                            VersionUtil.appAndDeviceInfo() +
                                                            VersionUtil.getVersionString(context), context),
                                 TYPE_TEXT_HTML, UTF_8)
            } else {
                webView.loadUrl(url)
            }
        }

        visibility = View.VISIBLE
    }

    fun hide() {
        this.tripId = ""
        webView.loadUrl("about:blank")
        visibility = View.GONE
    }

    private inner class KarhooWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            webView?.loadUrl(request.url.toString())
            return false
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            super.onReceivedSslError(view, handler, error)
            val config = KarhooAlertDialogConfig(
                    messageResId = R.string.kh_uisdk_notification_error_ssl_cert_invalid,
                    positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_continue_journey,
                                                             DialogInterface.OnClickListener { _, _ -> handler?.proceed() }),
                    negativeButton = KarhooAlertDialogAction(R.string.kh_uisdk_cancel,
                                                             DialogInterface.OnClickListener { _, _ -> handler?.cancel() }))
            KarhooAlertDialogHelper(context).showAlertDialog(config)

        }

        override fun onLoadResource(view: WebView?, url: String?) {
            if (url?.contains(INTERCEPT_HELP_STRING) == true) {
                webView.loadData(prepopulateForUser(KarhooApi.userStore.currentUser,
                                                    formattedTripId(context, tripId) + "\n" +
                                                            VersionUtil.appAndDeviceInfo() +
                                                            VersionUtil.getVersionString(context), context),
                                 TYPE_TEXT_HTML, UTF_8)
            }
            super.onLoadResource(view, url)
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

    private inner class DisableScrollTouchListener(val isScrollable: Boolean) : View.OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return if (isScrollable) {
                false
            } else {
                event.action == MotionEvent.ACTION_MOVE
            }
        }
    }

    companion object {
        @Deprecated("Use ContactEmailProvider instead")
        const val INTERCEPT_HELP_STRING = "CreateRecordForm"
        const val TYPE_TEXT_HTML = "text/html"
        const val UTF_8 = "UTF-8"
    }

}
