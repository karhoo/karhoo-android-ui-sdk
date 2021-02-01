package com.karhoo.karhootraveller.presentation.login.password

import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.uisdk.base.listener.ErrorView

interface ForgotPasswordMVP {

    interface Presenter {

        fun userForgotPassword()

        fun sendResetLink(email: String)

    }

    interface View {

        fun showEnterEmailDialog()

        fun resetEmailSent()

        fun couldNotSendResetLink(@StringRes error: Int, karhooError: KarhooError?)

        fun dismissLoading()

    }

    interface Actions : ErrorView {

        fun showProgress()

        fun hideProgress()

    }

}
