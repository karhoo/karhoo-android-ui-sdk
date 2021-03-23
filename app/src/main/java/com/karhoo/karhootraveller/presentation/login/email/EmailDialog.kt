package com.karhoo.karhootraveller.presentation.login.email

import android.app.AlertDialog
import android.content.Context
import com.karhoo.karhootraveller.R

class EmailDialog(context: Context, actions: EmailViewMVP.Actions?) : AlertDialog(context, R.style.DialogTheme), ValidEmailChangedListener {

    private val emailView = EmailView(context)

    init {
        emailView.validEmailChangedListener = this

        setTitle(R.string.kh_uisdk_forgot_password)
        setView(emailView)
        setButton(BUTTON_POSITIVE, context.getText(R.string.kh_uisdk_ok)) { _, _ -> actions?.resetEmail(emailView.email()) }
        setButton(BUTTON_NEGATIVE, context.getText(R.string.kh_uisdk_cancel)) { _, _ -> dismiss() }
    }

    override fun show() {
        super.show()
        getButton(BUTTON_POSITIVE).isEnabled = false
    }

    override fun onEmailValidChanged(isValid: Boolean) {
        getButton(BUTTON_POSITIVE).isEnabled = isValid
    }

}
