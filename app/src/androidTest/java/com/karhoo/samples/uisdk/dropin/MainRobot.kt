package com.karhoo.samples.uisdk.dropin

import com.karhoo.samples.uisdk.dropin.common.BaseTestRobot
import com.karhoo.samples.uisdk.dropin.common.Launch
import com.karhoo.uisdk.util.TestData.Companion.USER_UPDATED

fun main(func: MainRobot.() -> Unit) = MainRobot().apply {
    func()
}

fun main(launch: Launch, func: MainRobot.() -> Unit) = MainRobot().apply {
    launch.launch()
    func()
}

class MainRobot : BaseTestRobot() {

    fun userClicksOnBookATripLogin() {
        clickButton(R.id.bookTripButtonLogin)
    }

    fun validEmailAndPasswordLogin() {
        fillValidEmailLogin()
        mediumSleep()
        fillValidPasswordLogin()
    }

    fun fillValidEmailLogin() {
        fillEditText(
                resId = R.id.emailInput,
                text = USER_UPDATED.email
        )
    }

    fun fillValidPasswordLogin() {
        fillEditText(
                resId = R.id.passwordInput,
                text = "AsA!12345678Aa"
        )
    }

    fun userClicksOnSignInButton() {
        clickButton(R.id.signInButton)
    }
}
