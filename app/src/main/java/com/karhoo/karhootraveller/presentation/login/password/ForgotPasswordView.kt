package com.karhoo.karhootraveller.presentation.login.password

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.login.email.EmailDialog
import com.karhoo.karhootraveller.presentation.login.email.EmailViewMVP
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.uisdk.base.snackbar.SnackbarAction
import com.karhoo.uisdk.base.snackbar.SnackbarConfig

class ForgotPasswordView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr), ForgotPasswordMVP.View,
                                 EmailViewMVP.Actions {

    private val presenter: ForgotPasswordMVP.Presenter by lazy { ForgotPasswordPresenter(KarhooApi.userService, this) }

    var actions: ForgotPasswordMVP.Actions? = null

    init {
        attachListeners()
    }

    private fun attachListeners() {
        this.setOnClickListener { presenter.userForgotPassword() }
    }

    override fun showEnterEmailDialog() {
        EmailDialog(context, this).show()
    }

    override fun resetEmail(email: String) {
        presenter.sendResetLink(email)
        actions?.showProgress()
    }

    override fun resetEmailSent() {
        actions?.hideProgress()
        actions?.showSnackbar(SnackbarConfig(text = resources.getString(R.string.password_reset_success),
                                             action = SnackbarAction(resources.getString(R.string.got_it)) {}))
    }

    override fun dismissLoading() {
        actions?.hideProgress()
    }

    override fun couldNotSendResetLink(@StringRes error: Int, karhooError: KarhooError?) {
        actions?.showSnackbar(SnackbarConfig(text = null, messageResId = error, karhooError = karhooError))
    }

}
