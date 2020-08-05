package com.karhoo.karhootraveller.presentation.login.signin

interface LoginMVP {

    interface View {

        fun loginSuccessful()

        fun enableLogin(loginAvailable: Boolean)

        fun onError()

    }

    interface Presenter {

        fun loginUser(email: String, password: String)

        fun setLoginMode(allFieldsValid: Boolean)
    }

}
