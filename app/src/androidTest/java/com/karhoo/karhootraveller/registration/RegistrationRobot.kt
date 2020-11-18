package com.karhoo.karhootraveller.registration

import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.common.BaseTestRobot
import com.karhoo.karhootraveller.common.Launch
import com.karhoo.uisdk.util.TestData.Companion.FILL_EMAIL
import com.karhoo.uisdk.util.TestData.Companion.INVALID_PHONE
import com.karhoo.uisdk.util.TestData.Companion.KARHOO_PRIVACY
import com.karhoo.uisdk.util.TestData.Companion.KARHOO_TCS
import com.karhoo.uisdk.util.TestData.Companion.SHOW_PASSWORD
import com.karhoo.uisdk.util.TestData.Companion.USER_UPDATED
import com.karhoo.uisdk.util.TestData.Companion.USER_UPDATED_PHONE_NUMBER

fun registration(func: RegistrationRobot.() -> Unit) = RegistrationRobot().apply {
    func()
}

fun registration(launch: Launch, func: RegistrationRobot.() -> Unit) = RegistrationRobot().apply {
    launch.launch()
    func()
}

class RegistrationRobot : BaseTestRobot() {

    fun clickOnTermsAndConditionsButton() {
        clickButton(R.id.termsAndConditionsLabel)
    }

    fun clickOnPrivacyPolicyButton() {
        clickButton(R.id.privacyLabel)
    }

    fun enterRegistrationDetailsAlreadyUsedEmail() {
        fillFirstNameRegistration()
        fillLastNameRegistration()
        fillEmailRegistration()
        fillPhoneNumberRegistration()
        fillPasswordRegistration()
        clickContinueButtonRegistration()
    }

    fun fillRegistrationInvalidEmail() {
        fillFirstNameRegistration()
        fillLastNameRegistration()
        fillPhoneNumberRegistration()
        fillPasswordRegistration()
        fillInvalidEmailRegistration()
    }

    fun fillRegistrationFirstNameBlank() {
        leaveFirstNameBlank()
        fillLastNameRegistration()
        fillEmailRegistration()
        fillPhoneNumberRegistration()
        fillPasswordRegistration()
        clickContinueButtonRegistration()
    }

    fun fillRegistrationLastNameBlank() {
        fillFirstNameRegistration()
        leaveLastNameBlank()
        fillEmailRegistration()
        fillPhoneNumberRegistration()
        fillPasswordRegistration()
        clickContinueButtonRegistration()
    }

    fun fillRegistrationEmailBlank() {
        fillFirstNameRegistration()
        fillLastNameRegistration()
        leaveEmailBlank()
        fillPhoneNumberRegistration()
        fillPasswordRegistration()
        clickContinueButtonRegistration()
    }

    fun fillRegistrationPhoneNumberBlank() {
        fillFirstNameRegistration()
        fillLastNameRegistration()
        fillEmailRegistration()
        leavePhoneNumberBlank()
        fillPasswordRegistration()
        clickContinueButtonRegistration()
    }

    fun fillRegistrationPasswordBlank() {
        fillFirstNameRegistration()
        fillLastNameRegistration()
        fillEmailRegistration()
        fillPhoneNumberRegistration()
        leavePasswordBlank()
        clickContinueButtonRegistration()
    }

    fun fillRegistrationPasswordInvalid() {
        fillFirstNameRegistration()
        fillLastNameRegistration()
        fillEmailRegistration()
        fillPhoneNumberRegistration()
        fillInvalidPasswordRegistration()
        clickContinueButtonRegistration()
    }

    fun enterRegistrationDetailsInvalidNumber() {
        fillFirstNameRegistration()
        fillLastNameRegistration()
        fillEmailRegistration()
        fillPasswordRegistration()
        clickContinueButtonRegistration()
    }

    fun fillFullRegistration() {
        fillFirstNameRegistration()
        fillLastNameRegistration()
        fillEmailRegistration()
        fillPhoneNumberRegistration()
        fillPasswordRegistration()
    }

    fun fillFirstNameRegistration() {
        fillEditText(
                resId = R.id.firstNameInput,
                text = USER_UPDATED.firstName)
    }

    fun fillLastNameRegistration() {
        fillEditText(
                resId = R.id.lastNameInput,
                text = USER_UPDATED.lastName
                    )
    }

    fun fillEmailRegistration() {
        fillEditText(
                resId = R.id.emailInput,
                text = USER_UPDATED.email
                    )
    }

    fun fillPhoneNumberRegistration() {
        fillEditText(
                resId = R.id.mobileNumberInput,
                text = USER_UPDATED_PHONE_NUMBER
                    )
    }

    fun fillPasswordRegistration() {
        fillEditText(
                resId = R.id.passwordInput,
                text = "12345678Aa"
                    )
    }

    fun fillInvalidEmailRegistration() {
        fillEditText(
                resId = R.id.emailInput,
                text = FILL_EMAIL
                    )
    }

    fun fillInvalidPasswordRegistration() {
        fillEditText(
                resId = R.id.passwordInput,
                text = "1234567890"
                    )
    }

    fun fillInvalidPhoneNumberRegistration() {
        fillEditText(
                resId = R.id.mobileNumberInput,
                text = "123"
                    )
    }

    fun clickContinueButtonRegistration() {
        clickButton(R.id.registerButton)
    }

    fun leaveFirstNameBlank() {
        fillEditText(
                resId = R.id.firstNameInput,
                text = ""
                    )
    }

    fun leaveLastNameBlank() {
        fillEditText(
                resId = R.id.lastNameInput,
                text = ""
                    )
    }

    fun leaveEmailBlank() {
        fillEditText(
                resId = R.id.emailInput,
                text = ""
                    )
    }

    fun leavePhoneNumberBlank() {
        fillEditText(
                resId = R.id.mobileNumberInput,
                text = ""
                    )
    }

    fun leavePasswordBlank() {
        fillEditText(
                resId = R.id.passwordInput,
                text = ""
                    )
    }

    fun pressContinueButton() {
        clickButton(R.id.registerButton)
    }

    infix fun result(func: ResultRobot.() -> Unit): ResultRobot {
        return ResultRobot().apply { func() }
    }
}

class ResultRobot : BaseTestRobot() {

    fun termsAndConditionsPageIsShown() {
        stringIsVisibleIsDescendant(KARHOO_TCS, R.id.webView)
    }

    fun privacyPolicyPageIsShown() {
        stringIsVisibleIsDescendant(KARHOO_PRIVACY, R.id.webView)
    }

    fun cannotRegisterUserErrorIsShown() {
        checkSnackbarWithText(R.string.K1001)
    }

    fun continueButtonIsDisabled() {
        buttonIsDisabled(R.id.registerButton)
    }

    fun invalidNumberErrorIsShown() {
        stringIsVisibleIsDescendant(INVALID_PHONE, R.id.textinput_error)
    }

    fun showPasswordButtonIsClickable() {
        buttonByContentDescriptionIsClickable(SHOW_PASSWORD)
    }
}