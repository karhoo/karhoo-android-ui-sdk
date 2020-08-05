package com.karhoo.karhootraveller.presentation.register.registration

import com.karhoo.karhootraveller.presentation.base.ErrorMessageView
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.service.user.UserService

interface RegistrationMVP {

    interface View : ErrorMessageView {

        fun registrationPossible(isRegistrationPossible: Boolean)

        fun allFieldsValid(): Boolean

        fun userRegistered(userInfo: UserInfo)

        fun userRegistering()

        fun completeRegistration()

        fun goToTerms()

        fun goToPrivacy()

    }

    interface Presenter {

        fun registrationStarted()

        fun registerUser()

        fun completeRegistration(userInfo: UserInfo, password: String, userService: UserService)

        fun setRegistrationMode(isPossible: Boolean)

        fun goToTerms()

        fun goToPrivacy()

        fun validateMobileNumber(code: String, number: String): String

    }

}
