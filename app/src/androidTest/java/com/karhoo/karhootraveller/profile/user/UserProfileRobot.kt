package com.karhoo.karhootraveller.profile.user

import androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.common.BaseTestRobot
import com.karhoo.karhootraveller.common.Launch
import com.karhoo.uisdk.util.TestData.Companion.CARD_ENDING
import com.karhoo.uisdk.util.TestData.Companion.USER
import com.karhoo.uisdk.util.TestData.Companion.USER_PHONE_CODE
import com.karhoo.uisdk.util.TestData.Companion.USER_PHONE_NUMBER
import com.karhoo.uisdk.util.TestData.Companion.USER_UPDATED
import com.karhoo.uisdk.util.TestData.Companion.USER_UPDATED_PHONE_CODE
import com.karhoo.uisdk.util.TestData.Companion.USER_UPDATED_PHONE_NUMBER

fun userProfile(func: UserProfileRobot.() -> Unit) = UserProfileRobot().apply {
    func()
}

fun userProfile(launch: Launch, func: UserProfileRobot.() -> Unit) = UserProfileRobot().apply {
    launch.launch()
    func()
}

class UserProfileRobot : BaseTestRobot() {

    fun updateUserProfileWithDefaultInfo() {
        clickMenuToolbarButton()
        clickEditButton()
        fillFirstNameField()
        fillLastNameField()
        fillMobileNumberField()
        clickMenuToolbarButton()
        clickSaveButton()
    }

    fun clickMenuToolbarButton(): UserProfileRobot {
        openContextualActionModeOverflowMenu()
        return this
    }

    fun clickEditButton(): UserProfileRobot {
        clickButtonByString(R.string.edit_profile)
        return this
    }

    fun clickSaveButton(): UserProfileRobot {
        clickButtonByString(R.string.save)
        return this
    }

    fun clickDiscardButton(): UserProfileRobot {
        clickButtonByString(R.string.discard)
        return this
    }

    fun clickFirstNameField(): UserProfileRobot {
        clickButton(R.id.firstNameInput)
        return this
    }

    fun clickEmailNameField(): UserProfileRobot {
        clickButton(R.id.emailInput)
        return this
    }

    fun clickLastNameField(): UserProfileRobot {
        clickButton(R.id.lastNameInput)
        return this
    }

    fun clickPhoneNumberField(): UserProfileRobot {
        clickButton(R.id.mobileNumberInput)
        return this
    }

    fun fillFirstNameField(): UserProfileRobot {
        fillEditText(
                resId = R.id.firstNameInput,
                text = USER_UPDATED.firstName)
        return this
    }

    fun fillLastNameField(): UserProfileRobot {
        fillEditText(
                resId = R.id.lastNameInput,
                text = USER_UPDATED.lastName
                    )
        return this
    }

    fun fillMobileNumberField(): UserProfileRobot {
        fillEditText(
                resId = R.id.mobileNumberInput,
                text = USER_UPDATED_PHONE_NUMBER
                    )
        return this
    }

    fun clearFirstNameField(): UserProfileRobot {
        fillEditText(
                resId = R.id.firstNameInput,
                text = ""
                    )
        return this
    }

    fun clearLastNameField(): UserProfileRobot {
        fillEditText(
                resId = R.id.lastNameInput,
                text = ""
                    )
        return this
    }

    fun clearMobileNumberField(): UserProfileRobot {
        fillEditText(
                resId = R.id.mobileNumberInput,
                text = ""
                    )
        return this
    }

    fun clickSignOutButton() {
        clickButtonByString(R.string.sign_out)
    }

    infix fun result(func: ResultRobot.() -> Unit): ResultRobot {
        return ResultRobot().apply { func() }
    }
}

class ResultRobot : BaseTestRobot() {

    fun noProfileChangesFullCheck() {
        stringIsVisible(USER.firstName)
        stringIsVisible(USER.lastName)
        stringIsVisible(USER_PHONE_NUMBER)
        stringIsVisible(USER_PHONE_CODE)
        stringIsVisible(USER.email)
    }

    fun updatedProfileChangesFullCheck() {
        stringIsVisible(USER_UPDATED.firstName)
        stringIsVisible(USER_UPDATED.lastName)
        stringIsVisible(USER_UPDATED_PHONE_NUMBER)
        stringIsVisible(USER_UPDATED_PHONE_CODE)
        stringIsVisible(USER_UPDATED.email)
    }

    fun checkFirstNameFieldIsNotFocused() {
        viewIsNotFocused(R.id.firstNameInput)
    }

    fun checkFirstNameFieldIsFocused() {
        viewIsFocused(R.id.firstNameInput)
    }

    fun checkEmailFieldIsNotFocused() {
        viewIsNotFocused(R.id.emailInput)
    }

    fun checkEditButtonIsVisible() {
        textIsVisible(R.string.edit_profile)
    }

    fun checkSignOutButtonIsVisible() {
        textIsVisible(R.string.sign_out)
    }

    fun checkSaveButtonIsVisible() {
        textIsVisible(R.string.save)
    }

    fun checkDiscardButtonIsVisible() {
        textIsVisible(R.string.discard)
    }

    fun checkSaveButtonIsNotClickable() {
        buttonWithTextIsNotClickable(R.string.save)
    }

    fun checkSuccessSnackBarIsShown() {
        checkSnackbarWithText(R.string.profile_update_successful)
    }

    fun fullScreenCheckNoCardRegistered() {
        firstNameFieldIsVisible()
        userFirstNameIsVisible()
        lastNameFieldIsVisible()
        userLastNameIsVisible()
        emailFieldIsVisible()
        userEmailIsVisible()
        countryCodeFieldIsVisible()
        userCountryCodeIsVisible()
        phoneNumberFieldIsVisible()
        userPhoneNumberIsVisible()
        paymentCardFieldIsVisible()
        addCardIsVisible()
    }

    fun fullScreenCheckCardRegistered() {
        firstNameFieldIsVisible()
        userFirstNameIsVisible()
        lastNameFieldIsVisible()
        userLastNameIsVisible()
        emailFieldIsVisible()
        userEmailIsVisible()
        countryCodeFieldIsVisible()
        userCountryCodeIsVisible()
        phoneNumberFieldIsVisible()
        userPhoneNumberIsVisible()
        paymentCardFieldIsVisible()
        lastFourDigitsCardVisible()
        changeCardIsVisible()
    }

    fun firstNameFieldIsVisible() {
        viewIsVisible(R.id.firstNameLayout)
    }

    fun userFirstNameIsVisible() {
        matchString(R.id.firstNameInput, USER.firstName)
    }

    fun lastNameFieldIsVisible() {
        viewIsVisible(R.id.lastNameLayout)
    }

    fun userLastNameIsVisible() {
        matchString(R.id.lastNameInput, USER.lastName)
    }

    fun emailFieldIsVisible() {
        viewIsVisible(R.id.emailLayout)
    }

    fun userEmailIsVisible() {
        matchString(R.id.emailInput, USER.email)
    }

    fun countryCodeFieldIsVisible() {
        viewIsVisible(R.id.countryCodeSpinner)
    }

    fun userCountryCodeIsVisible() {
        matchString(R.id.countryDiallingCodeText, USER_PHONE_CODE)
    }

    fun phoneNumberFieldIsVisible() {
        viewIsVisible(R.id.mobileNumberLayout)
    }

    fun userPhoneNumberIsVisible() {
        matchString(R.id.mobileNumberInput, USER_PHONE_NUMBER)
    }

    fun paymentCardFieldIsVisible() {
        viewIsVisible(R.id.paymentCardLabel)
    }

    fun addCardIsVisible() {
        matchText(R.id.changeCardLabel, R.string.edit_profile)
    }

    fun changeCardIsVisible() {
        matchText(R.id.changeCardLabel, R.string.edit_profile)
    }

    fun cardLogoIsVisible() {
        viewIsVisible(R.id.cardLogoImage)
    }

    fun lastFourDigitsCardVisible() {
        matchString(R.id.cardNumberText, CARD_ENDING)
    }

}