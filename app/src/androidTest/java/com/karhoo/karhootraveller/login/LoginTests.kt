package com.karhoo.karhootraveller.login

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.karhootraveller.common.Launch
import com.karhoo.karhootraveller.common.testrunner.TravellerTestConfig
import com.karhoo.karhootraveller.presentation.login.LoginActivity
import com.karhoo.uisdk.common.ServerRobot.Companion.PASSWORD_RESET_SUCCESS
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.util.TestData.Companion.LONG
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection.HTTP_CREATED

@RunWith(AndroidJUnit4::class)
class LoginTests : Launch {

    @get:Rule
    var wireMockRule = WireMockRule(TravellerTestConfig.PORT_NUMBER)

    @get:Rule
    val activityRule: ActivityTestRule<LoginActivity> =
            ActivityTestRule(LoginActivity::class.java, false, false)

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(Intent())
    }

    /**
     * Given:   I am on the login page
     * When:    I press "Forgotten password"
     * Then:    The forgotten password popup is displayed
     **/
    @Test
    fun userClicksOnForgottenPassword() {
        login(this) {
            userClicksOnForgottenPassword()
        } result {
            forgottenPasswordPopupIsShown()
        }
    }

    /**
     * Given:   I am on the login screen
     * When:    I enter an invalid format email and valid format password
     * Then:    Sign in button is disabled
     **/
    @Test
    fun userEntersInvalidEmailFormatLogin() {
        login(this) {
            invalidEmailFormatLogin()
        } result {
            signInButtonIsDisabled()
        }
    }

    /**
     * Given:   I am on the login screen
     * When:    I enter a valid format email and password field is empty
     * Then:    Sign in button is disabled
     **/
    @Test
    fun userLeavesPasswordFieldEmptyLogin() {
        login(this) {
            emptyPasswordFieldLogin()
        } result {
            signInButtonIsDisabled()
        }
    }

    /**
     * Given:   I am on the login screen
     * When:    I enter a valid password and email field is empty
     * Then:    Sign in button is disabled
     **/
    @Test
    fun userLeavesEmailFieldEmptyLogin() {
        login(this) {
            emptyEmailFieldLogin()
        } result {
            signInButtonIsDisabled()
        }
    }

    /**
     * Given:   I am on the login screen
     * When:    I enter a valid format email and at least one character in the password field
     * Then:    Sign in button is clickable
     **/
    @Test
    fun userEntersValidEmailPlusOneLetter() {
        login(this) {
            validEmailOneLetterPasswordLogin()
        } result {
            signInButtonIsClickable()
        }
    }

    /**
     * Given:   I am on the login screen
     * When:    I enter a valid email and password
     * Then:    the sign in button is clickable
     **/
    @Test
    fun userEntersValidEmailAndPassword() {
        login(this) {
            validEmailAndPasswordLogin()
        } result {
            signInButtonIsClickable()
        }
    }

    /**
     * Given:   I am on the login screens
     * When:    I attempt to log in with invalid credentials
     * Then:    I am shown the error "The email or password you've entered is incorrect"
     **/
    @Test
    fun userCannotLogInWithInvalidCredential() {
        serverRobot {
            unsuccessfulToken()
        }
        login(this) {
            validEmailOneLetterPasswordLogin()
            userClicksOnSignInButton()
        } result {
            loginFailedErrorIsVisible()
        }
    }

    /**
     * Given:   I am on the password reset screen
     * When:    I enter an invalid format email
     * Then:    The OK button is disabled
     **/
    @Test
    fun userEntersInvalidFormatEmailResetPassword() {
        login(this) {
            userClicksOnForgottenPassword()
            fillInvalidEmailLogin()
        } result {
            resetPasswordOkButtonIsDisabled()
        }
    }

    /**
     * Given:   I am on the password reset screen
     * When:    I enter a valid format email
     * Then:    The OK button is enabled
     **/
    @Test
    fun userEntersValidFormatEmailResetPassword() {
        login(this) {
            userClicksOnForgottenPassword()
            fillValidEmailLogin()
        } result {
            resetPasswordOkButtonIsEnabled()
        }
    }

    /**
     * Given:   I am on the reset password flow
     * When:    I enter a valid email and confirm
     * Then:    I see the confirmation message on the sign in screen
     **/
    @Test
    fun userCompletesValidPasswordReset() {
        serverRobot {
            passwordResetResponse(HTTP_CREATED, PASSWORD_RESET_SUCCESS)
        }
        login(this) {
            userClicksOnForgottenPassword()
            fillValidEmailLogin()
            pressOkButton()
        } result {
            resetPasswordConfirmationVisible()
        }
    }

    /**
     * Given:   I have completed the reset password flow
     * When:    I click GOT IT
     * Then:    The snackbar is dismissed
     **/
    @Test
    fun resetPasswordSuccessSnackbarIsDismissed() {
        serverRobot {
            passwordResetResponse(HTTP_CREATED, PASSWORD_RESET_SUCCESS)
        }
        login(this) {
            userClicksOnForgottenPassword()
            fillValidEmailLogin()
            pressOkButton()
            sleep(LONG)
            pressGotItSnackbarButton()
            sleep()
        } result {
            resetPasswordConfirmationIsNotVisible()
        }
    }

    /**
     * Given:   I am on the login page
     * When:    I have entered credentials in password field
     * Then:    The show password button is clickable
     **/
    @Test
    fun showPasswordButtonIsEnabledLogin() {
        login(this) {
            validEmailAndPasswordLogin()
        } result {
            showPasswordButtonIsClickable()
        }
    }
}