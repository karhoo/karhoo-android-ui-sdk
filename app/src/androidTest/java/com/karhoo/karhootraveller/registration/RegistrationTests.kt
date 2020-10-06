package com.karhoo.karhootraveller.registration

import android.content.Intent
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.karhootraveller.common.Launch
import com.karhoo.karhootraveller.common.testrunner.TravellerTestConfig
import com.karhoo.karhootraveller.presentation.register.RegistrationActivity
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.util.TestData.Companion.EMAIL_ALREADY_IN_USE
import com.karhoo.uisdk.util.TestData.Companion.INVALID_PHONE_NUMBER
import com.karhoo.uisdk.util.TestData.Companion.REGISTRATION_FAILED
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR

@RunWith(AndroidJUnit4ClassRunner::class)
class RegistrationTests : Launch {

    @get:Rule
    var wireMockRule = WireMockRule(TravellerTestConfig.PORT_NUMBER)

    @get:Rule
    val activityRule: ActivityTestRule<RegistrationActivity> =
            ActivityTestRule(RegistrationActivity::class.java, false, false)

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(Intent())
    }

    /**
     * Given:   I am on the registration screen
     * When:    I click on Terms and Conditions
     * Then:    Karhoo's terms and conditions webpage is opened.
     **/
    @Test
    fun userClicksOnTermsAndConditions() {
        registration(this) {
            clickOnTermsAndConditionsButton()
        } result {
            sleep()
            termsAndConditionsPageIsShown()
        }
    }

    /**
     * Given:   I am on the registration screen
     * When:    I click on the Privacy Policy link
     * Then:    Karhoo's Privacy Policy webpage is opened
     **/
    @Test
    fun userClicksOnPrivacyPolicy() {
        registration(this) {
            clickOnPrivacyPolicyButton()
        } result {
            privacyPolicyPageIsShown()
        }
    }

    /**
     * Given:   I am on the registration page
     * When:    I complete the registration with an email already in use
     * Then:    I see the snackbar error "Could not register user"
     **/
    @Test
    fun userRegistersWithAlreadyExistingEmail() {
        serverRobot {
            successfulToken()
            registerUserResponse(HTTP_INTERNAL_ERROR, EMAIL_ALREADY_IN_USE)
        }
        registration(this) {
            enterRegistrationDetailsAlreadyUsedEmail()
            waitFor(5000)
        } result {
            cannotRegisterUserErrorIsShown()
        }
    }

    /**
     * Given:   I am on the registration test
     * When:    I enter all fields correctly BUT an invalid email
     * Then:    The Continue button is disabled
     **/
    @Test
    fun continueButtonIsDisabledIfInvalidEmail() {
        registration(this) {
            fillRegistrationInvalidEmail()
        } result {
            continueButtonIsDisabled()
        }
    }

    /**
     * Given:   I am on the registration screen
     * When:    I leave the first name field blank but enter all other fields
     * Then:    The continue button is disabled
     **/
    @Test
    fun continueButtonDisabledIfFirstNameIsBlank() {
        registration(this) {
            fillRegistrationFirstNameBlank()
        } result {
            continueButtonIsDisabled()
        }
    }

    /**
     * Given:   I am on the registration screen
     * When:    I leave the last name field blank but enter all other fields
     * Then:    The continue button is disabled
     **/
    @Test
    fun continueButtonDisabledIfLastNameIsBlank() {
        registration(this) {
            fillRegistrationLastNameBlank()
        } result {
            continueButtonIsDisabled()
        }
    }

    /**
     * Given:   I am on the registration screen
     * When:    I leave the email field blank but enter all other fields
     * Then:    The continue button is disabled
     **/
    @Test
    fun continueButtonDisabledIfEmailIsBlank() {
        registration(this) {
            fillRegistrationEmailBlank()
        } result {
            continueButtonIsDisabled()
        }
    }

    /**
     * Given:   I am on the registration screen
     * When:    I leave the phone number field blank but enter all other fields
     * Then:    The continue button is disabled
     **/
    @Test
    fun continueButtonDisabledIfPhoneNumberIsBlank() {
        registration(this) {
            fillRegistrationPhoneNumberBlank()
        } result {
            continueButtonIsDisabled()
        }
    }

    /**
     * Given:   I am on the registration screen
     * When:    I leave the password field blank but enter all other fields
     * Then:    The continue button is disabled
     **/
    @Test
    fun continueButtonDisabledIfPasswordIsBlank() {
        registration(this) {
            fillRegistrationPasswordBlank()
        } result {
            continueButtonIsDisabled()
        }
    }

    /**
     * Given:   I am on the registration screen
     * When:    I enter an invalid format password but enter all other fields
     * Then:    The continue button is disabled
     **/
    @Test
    fun continueButtonDisabledIfPasswordIsInvalidFormat() {
        registration(this) {
            fillRegistrationPasswordInvalid()
        } result {
            continueButtonIsDisabled()
        }
    }

    /**
     * Given:   I am on the registration page
     * When:    I complete the registration with an invalid number
     * Then:    I see the snackbar error "Could not register user (invalid phone number)"
     **/
    @Test
    fun userRegistersWithInvalidPhoneNumber() {
        serverRobot {
            successfulToken()
            registerUserResponse(HTTP_BAD_REQUEST, INVALID_PHONE_NUMBER)
        }
        registration(this) {
            fillInvalidPhoneNumberRegistration()
            enterRegistrationDetailsInvalidNumber()
        } result {
            sleep()
            invalidNumberErrorIsShown()
            continueButtonIsDisabled()
        }
    }

    /**
     * Given:   I am on the registration page
     * When:    I have entered credentials in password field
     * Then:    The show password button is clickable
     **/
    @Test
    fun showPasswordButtonIsEnabledLogin() {
        registration(this) {
            fillFullRegistration()
        } result {
            showPasswordButtonIsClickable()
        }
    }

    /**
     * Given:   I am on the registration page
     * When:    I enter valid credentials throughout
     * And:     I am already registered
     * Then:    I receive a message stating that registration has failed
     **/
    @Test
    fun userAlreadyRegisteredCannotRegister() {
        serverRobot {
            successfulToken()
            registerUserResponse(HTTP_INTERNAL_ERROR, REGISTRATION_FAILED)
        }
        registration(this) {
            fillFullRegistration()
            clickContinueButtonRegistration()
        } result {
            cannotRegisterUserErrorIsShown()
        }
    }
}