package com.karhoo.karhootraveller.splash

import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.common.BaseTestRobot
import com.karhoo.karhootraveller.common.Launch

fun splash(func: SplashRobot.() -> Unit) = SplashRobot().apply {
    func()
}

fun splash(launch: Launch, func: SplashRobot.() -> Unit) = SplashRobot().apply {
    launch.launch()
    func()
}

class SplashRobot : BaseTestRobot() {

    fun clickOnRequestInviteButton() {
        clickButton(R.id.registerButton)
    }

    fun clickOnSignInButton() {
        clickButton(R.id.signInButton)
    }

    fun clickGotItButton() {
        clickButtonByString(R.string.got_it)
    }

    infix fun result(func: ResultRobot.() -> Unit): ResultRobot {
        return ResultRobot().apply { func() }
    }
}

class ResultRobot : BaseTestRobot() {

    fun splashScreenIsVisible() {
        registerButtonIsEnabled()
        signInButtonIsEnabled()
    }

    fun registerButtonIsEnabled() {
        buttonIsEnabled(R.id.registerButton)
    }

    fun signInButtonIsEnabled() {
        buttonIsEnabled(R.id.signInButton)
    }

    fun registrationPageIsShown() {
        checkToolbarTitleString("Register")
    }

    fun loginPageIsShown() {
        checkToolbarTitleString("Sign In")
    }

    fun registrationConfirmationMessageIsShown() {
        checkSnackbarWithText(R.string.requested_invite)
    }

    fun registrationSnackbarIsNotVisible() {
        viewIsNotVisible(R.string.got_it)
    }
}
