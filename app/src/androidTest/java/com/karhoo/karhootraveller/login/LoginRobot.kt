package com.karhoo.karhootraveller.login

import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.common.BaseTestRobot
import com.karhoo.karhootraveller.common.Launch
import com.karhoo.uisdk.util.TestData.Companion.FILL_EMAIL
import com.karhoo.uisdk.util.TestData.Companion.MEDIUM
import com.karhoo.uisdk.util.TestData.Companion.SHOW_PASSWORD
import com.karhoo.uisdk.util.TestData.Companion.USER_UPDATED

fun login(func: LoginRobot.() -> Unit) = LoginRobot().apply {
    func()
}

fun login(launch: Launch, func: LoginRobot.() -> Unit) = LoginRobot().apply {
    launch.launch()
    func()
}

class LoginRobot : BaseTestRobot() {

    fun userClicksOnForgottenPassword() {
        clickButton(R.id.forgotPasswordWidget)
    }

    fun userClicksOnSignInButton() {
        clickButton(R.id.signInButton)
    }

    fun invalidEmailFormatLogin() {
        fillInvalidEmailLogin()
        fillValidPasswordLogin()
    }

    fun emptyPasswordFieldLogin() {
        fillValidEmailLogin()
        leavePasswordBlankLogin()
    }

    fun emptyEmailFieldLogin() {
        leaveEmailBlankLogin()
        fillValidPasswordLogin()
    }

    fun validEmailOneLetterPasswordLogin() {
        fillValidEmailLogin()
        fillOneCharacterInPasswordLogin()
    }

    fun validEmailAndPasswordLogin() {
        fillValidEmailLogin()
        sleep(MEDIUM)
        fillValidPasswordLogin()
    }

    fun fillInvalidEmailLogin() {
        fillEditText(
                resId = R.id.emailInput,
                text = FILL_EMAIL
                    )
    }

    fun fillValidEmailLogin() {
        fillEditText(
                resId = R.id.emailInput,
                text = USER_UPDATED.email
                    )
    }

    fun leaveEmailBlankLogin() {
        fillEditText(
                resId = R.id.emailInput,
                text = ""
                    )
    }

    fun leavePasswordBlankLogin() {
        fillEditText(
                resId = R.id.passwordInput,
                text = ""
                    )
    }

    fun fillOneCharacterInPasswordLogin() {
        fillEditText(
                resId = R.id.passwordInput,
                text = "a"
                    )
    }

    fun fillValidPasswordLogin() {
        fillEditText(
                resId = R.id.passwordInput,
                text = "AsA!12345678Aa"
                    )
    }

    fun pressOkButton() {
        dialogClickButtonByText(R.string.ok)
    }

    fun pressGotItSnackbarButton() {
        clickButtonByString(R.string.got_it)
    }

    infix fun result(func: ResultRobot.() -> Unit): ResultRobot {
        return ResultRobot().apply { func() }
    }
}

class ResultRobot : BaseTestRobot() {

    fun forgottenPasswordPopupIsShown() {
        dialogTextIsVisibleString("Enter your email address below to reset your password.")
    }

    fun signInButtonIsClickable() {
        buttonIsClickable(R.id.signInButton)
    }

    fun signInButtonIsDisabled() {
        buttonIsDisabled(R.id.signInButton)
    }

    fun loginFailedErrorIsVisible() {
        textIsVisible(R.string.error_invalid_password)
    }

    fun resetPasswordOkButtonIsEnabled() {
        dialogButtonByTextIsEnabled(R.string.ok)
    }

    fun resetPasswordOkButtonIsDisabled() {
        dialogButtonByTextIsDisabled(R.string.ok)
    }

    fun resetPasswordConfirmationVisible() {
        textIsVisible(R.string.password_reset_success)
    }

    fun resetPasswordConfirmationIsNotVisible() {
        textIsNotVisible(R.string.password_reset_success)
    }

    fun showPasswordButtonIsClickable() {
        buttonByContentDescriptionIsClickable(SHOW_PASSWORD)
    }
}