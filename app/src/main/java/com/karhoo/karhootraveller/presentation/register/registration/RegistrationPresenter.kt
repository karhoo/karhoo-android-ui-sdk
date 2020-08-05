package com.karhoo.karhootraveller.presentation.register.registration

import com.karhoo.karhootraveller.presentation.base.BasePresenter
import com.karhoo.karhootraveller.util.logoutAndResetApp
import com.karhoo.uisdk.util.formatMobileNumber
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.network.request.UserRegistration
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.user.UserService
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired

class RegistrationPresenter(
        view: RegistrationMVP.View,
        private val analytics: Analytics)
    : BasePresenter<RegistrationMVP.View>(), RegistrationMVP.Presenter {

    init {
        attachView(view)
    }

    override fun registrationStarted() {
        analytics.registrationStarted()
    }

    override fun registerUser() {
        setRegistrationMode(false)
        view?.userRegistering()
    }

    override fun completeRegistration(userInfo: UserInfo, password: String, userService: UserService) {
        userService
                .register(UserRegistration(
                        firstName = userInfo.firstName,
                        lastName = userInfo.lastName,
                        email = userInfo.email,
                        phoneNumber = userInfo.phoneNumber,
                        password = password))
                .execute { result ->
                    when (result) {
                        is Resource.Success -> onSuccessfulRegistration(result.data)
                        is Resource.Failure -> handleRegistrationError(result.error, userInfo)
                    }
                }
    }

    private fun onSuccessfulRegistration(userInfo: UserInfo) {
        analytics.registrationComplete()
        view?.userRegistered(userInfo)
    }

    private fun handleRegistrationError(error: KarhooError, userInfo: UserInfo) {
        when (error) {
            KarhooError.RequiredRolesNotAvailable -> {
                analytics.registrationComplete()
                view?.userRegistered(userInfo)
            }
            else -> {
                handleAuthError(error)
                view?.showError(returnErrorStringOrLogoutIfRequired(error))
                setRegistrationMode(true)
            }
        }
    }

    override fun setRegistrationMode(isPossible: Boolean) {
        view?.registrationPossible(isPossible)
    }

    override fun goToTerms() {
        analytics.termsReviewed()
        view?.goToTerms()
    }

    override fun goToPrivacy() {
        analytics.termsReviewed()
        view?.goToPrivacy()
    }

    override fun validateMobileNumber(code: String, number: String): String {
        return formatMobileNumber(code, number)
    }

    private fun handleAuthError(error: KarhooError) {
        when (error) {
            KarhooError.CouldNotReadAuthorisationToken,
                KarhooError.CouldNotParseAuthorisationToken,
                KarhooError.CouldNotAuthenticate -> logoutAndResetApp(isAutomaticLogout = true)
        }
    }
}
