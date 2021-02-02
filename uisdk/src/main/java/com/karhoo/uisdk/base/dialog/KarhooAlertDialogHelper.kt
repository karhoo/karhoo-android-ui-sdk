package com.karhoo.uisdk.base.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.karhoo.uisdk.R

class KarhooAlertDialogHelper(val context: Context) {

    val builder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.DialogTheme)

    fun showAlertDialog(config: KarhooAlertDialogConfig): AlertDialog {
        with(builder) {
            config.view?.let { setView(it) }
            setTitle(formatTitle(config))
            setMessage(formatMessage(config))
            config.positiveButton?.let {
                setPositiveButton(config.positiveButton.buttonLabel) { dialog, which -> config
                        .positiveButton.buttonListener.onClick(dialog, which) }
            }
            config.negativeButton?.let {
                setNegativeButton(config.negativeButton.buttonLabel) { dialog, which -> config
                        .negativeButton.buttonListener.onClick(dialog, which) }
            }
            setCancelable(config.cancellable)
        }
        return builder.show()
    }

    private fun formatMessage(config: KarhooAlertDialogConfig): String {
        var message = config.message.orEmpty()
        if (message.isEmpty() && config.messageResId > 0) {
            message = context.getString(config.messageResId)
        }
        config.karhooError?.let { error ->
            message = "$message [${error.code}]"
        }
        return message
    }

    private fun formatTitle(config: KarhooAlertDialogConfig): String {
        var title = config.title.orEmpty()
        if (title.isEmpty() && config.titleResId > 0) {
            title = context.getString(config.titleResId)
        }
        return title
    }
}