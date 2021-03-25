package com.karhoo.uisdk.base

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.karhoo.sdk.analytics.AnalyticsManager
import com.karhoo.sdk.analytics.Event
import com.karhoo.sdk.api.KarhooError
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.base.listener.ErrorView
import com.karhoo.uisdk.base.listener.NetworkReceiver
import com.karhoo.uisdk.base.snackbar.SnackbarAction
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.base.snackbar.SnackbarPriority
import com.karhoo.uisdk.base.snackbar.SnackbarType
import com.karhoo.uisdk.util.extension.hideSoftKeyboard
import kotlinx.android.synthetic.main.uisdk_activity_base.khWebView
import kotlinx.android.synthetic.main.uisdk_activity_base.snackBarContainer
import kotlinx.android.synthetic.main.uisdk_activity_base.topNotificationWidget

abstract class BaseActivity : AppCompatActivity(), LocationLock, ErrorView, NetworkReceiver.Actions {

    private var backgroundFade: TransitionDrawable? = null
    private var networkReceiver: NetworkReceiver? = null
    private var errorShown: SnackbarType = SnackbarType.TEMPORARY
    private var snackbar: Snackbar? = null
    private var networkLostError: Boolean = false
    protected var extras: Bundle? = null

    @get:LayoutRes
    abstract val layout: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        backgroundFade = snackBarContainer?.background as TransitionDrawable?
        extras = intent?.extras

        initialiseViews()
        handleExtras()
        initialiseViewListeners()
        bindViews()
    }

    override fun onResume() {
        super.onResume()
        resetErrorLock()
        resetNetworkLost()
        if (networkReceiver == null) {
            networkReceiver = NetworkReceiver(this)
            registerReceiver(networkReceiver, networkReceiver?.intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver)
            networkReceiver = null
        }
    }

    override fun onBackPressed() {
        if (khWebView?.visibility == View.VISIBLE) {
            khWebView?.hide()
        } else {
            super.onBackPressed()
        }
    }

    override fun showSnackbar(snackbarConfig: SnackbarConfig) {
        if (errorShown == SnackbarType.TEMPORARY) {

            currentFocus.hideSoftKeyboard()
            val text = formatMessage(snackbarConfig)

            errorShown = snackbarConfig.type
            if (snackbarConfig.action != null) {
                snackbar = Snackbar.make(snackBarContainer, text, Snackbar.LENGTH_INDEFINITE)
                        .setAction(snackbarConfig.action.text) { snackbarConfig.action.action() }
                        .setActionTextColor(ContextCompat.getColor(this, R.color.text_white))
                val snackText = snackbar?.view?.findViewById(R.id.snackbar_text) as TextView
                snackText.maxLines = 3

                snackbar?.show()

            } else {
                Snackbar.make(snackBarContainer, text, Snackbar.LENGTH_LONG).show()
            }

            if (snackbarConfig.type == SnackbarType.BLOCKING) {
                enableErrorLock()
            }
        }
    }

    private fun formatMessage(snackbarConfig: SnackbarConfig): String {
        var message = snackbarConfig.text.orEmpty()
        if (message.isEmpty() && snackbarConfig.messageResId > 0) {
            message = getString(snackbarConfig.messageResId)
        }

        snackbarConfig.karhooError?.let { error ->
            message = "$message [${error.code}]"
        }
        return message
    }

    override fun showTopBarNotification(stringId: Int) {
        showTopBarNotification(getString(stringId))
    }

    override fun showTopBarNotification(value: String) {
        topNotificationWidget.setNotificationText(value)
    }

    override fun showErrorDialog(stringId: Int, karhooError: KarhooError?) {
        val config = KarhooAlertDialogConfig(
                messageResId = stringId,
                karhooError = karhooError,
                cancellable = true,
                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_ok,
                                                         DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }))
        KarhooAlertDialogHelper(this).showAlertDialog(config)
    }

    override fun dismissSnackbar() {
        snackbar?.dismiss()
    }

    override fun showLocationLock() {
        AnalyticsManager.fireEvent(Event.REJECT_LOCATION_SERVICES)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", packageName, null))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val action = SnackbarAction(getString(R.string.kh_uisdk_settings)) { startActivity(intent) }

        val snackbarConfig = SnackbarConfig(type = SnackbarType.BLOCKING,
                                            priority = SnackbarPriority.HIGH,
                                            action = action,
                                            text = getString(R.string.kh_uisdk_permission_rationale_location))
        showSnackbar(snackbarConfig)
    }

    override fun connectionChanged(isConnected: Boolean) {
        if (isConnected) {
            if (networkLostError) {
                dismissSnackbar()
            }
            resetNetworkLost()
            resetErrorLock()
        } else if (!isConnected) {
            val action = SnackbarAction(getString(R.string.kh_uisdk_settings)) { startActivity(networkReceiver?.settingsIntent) }
            val snackbarConfig = SnackbarConfig(type = SnackbarType.BLOCKING,
                                                priority = SnackbarPriority.HIGHEST,
                                                action = action,
                                                text = getString(R.string.kh_uisdk_network_error))
            showSnackbar(snackbarConfig)
            setNetworkLost()
            enableErrorLock()
        }
    }

    private fun resetNetworkLost() {
        networkLostError = false
    }

    private fun setNetworkLost() {
        networkLostError = true
    }

    private fun resetErrorLock() {
        errorShown = SnackbarType.TEMPORARY
        backgroundFade?.resetTransition()
        snackBarContainer?.isClickable = false
    }

    private fun enableErrorLock() {
        backgroundFade?.startTransition(resources.getInteger(R.integer.snackbar_background_fade))
        snackBarContainer?.isClickable = true
    }

    protected abstract fun handleExtras()

    protected open fun bindViews() {}

    protected open fun initialiseViews() {}

    protected open fun initialiseViewListeners() {}

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}
