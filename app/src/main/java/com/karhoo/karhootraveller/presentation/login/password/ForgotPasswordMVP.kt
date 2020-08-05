package com.karhoo.karhootraveller.presentation.login.password

import androidx.annotation.StringRes
import com.karhoo.uisdk.base.listener.ErrorView

interface ForgotPasswordMVP {

    interface Presenter {

        fun userForgotPassword()

        fun sendResetLink(email: String)

    }

    interface View {

        fun showEnterEmailDialog()

        fun resetEmailSent()

        fun couldNotSendResetLink(@StringRes error: Int)

        fun dismissLoading()

    }

    interface Actions : ErrorView {

        fun showProgress()

        fun hideProgress()

    }

}
