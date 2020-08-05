package com.karhoo.karhootraveller.presentation.login.signin

import com.karhoo.karhootraveller.presentation.base.BasePresenter
import com.karhoo.karhootraveller.presentation.splash.domain.AppVersionValidator
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.network.request.UserLogin
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.user.UserService
import com.karhoo.uisdk.analytics.Analytics
import org.joda.time.DateTime

class LoginPresenter(private val userService: UserService,
                     view: LoginMVP.View,
                     private val analytics: Analytics,
                     private val appVersionValidator: AppVersionValidator)
    : BasePresenter<LoginMVP.View>(), LoginMVP.Presenter {

    init {
        attachView(view)
    }

    override fun loginUser(email: String, password: String) {
        setLoginMode(false)
        userService.loginUser(UserLogin(email = email, password = password))
                .execute { result ->
                    when (result) {
                        is Resource.Success -> handleSuccessfulLogin(result.data)
                        is Resource.Failure -> handleLoginError(result.error)
                    }
                }
    }

    private fun handleSuccessfulLogin(userInfo: UserInfo) {
        analytics.userLoggedIn(userInfo)
        appVersionValidator.saveLoginTime(DateTime.now().millis)
        view?.loginSuccessful()
    }

    private fun handleLoginError(karhooError: KarhooError) {
        setLoginMode(true)
        view?.onError()
    }

    override fun setLoginMode(allFieldsValid: Boolean) {
        view?.enableLogin(allFieldsValid)
    }

}
