package com.karhoo.karhootraveller.presentation.login.password

import com.karhoo.karhootraveller.presentation.base.BasePresenter
import com.karhoo.karhootraveller.util.logoutAndResetApp
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.user.UserService
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired

class ForgotPasswordPresenter(private val userService: UserService, view: ForgotPasswordMVP.View)
    : BasePresenter<ForgotPasswordMVP.View>(), ForgotPasswordMVP.Presenter {

    init {
        attachView(view)
    }

    override fun userForgotPassword() {
        view?.showEnterEmailDialog()
    }

    override fun sendResetLink(email: String) {
        userService.resetPassword(email).execute { result ->
            when (result) {
                is Resource.Success -> view?.resetEmailSent()
                is Resource.Failure -> handleError(result.error)
            }
        }
    }

    private fun handleError(karhooError: KarhooError) {
        handleAuthError(karhooError)
        view?.dismissLoading()
        view?.couldNotSendResetLink(error = returnErrorStringOrLogoutIfRequired(karhooError),
                                    karhooError = karhooError)
    }

    private fun handleAuthError(error: KarhooError) {
        when (error) {
            KarhooError.CouldNotReadAuthorisationToken,
            KarhooError.CouldNotParseAuthorisationToken,
            KarhooError.CouldNotAuthenticate -> logoutAndResetApp(isAutomaticLogout = true)
        }
    }
}
