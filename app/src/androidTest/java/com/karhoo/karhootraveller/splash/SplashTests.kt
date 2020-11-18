package com.karhoo.karhootraveller.splash

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.karhootraveller.common.Launch
import com.karhoo.karhootraveller.common.preferences
import com.karhoo.karhootraveller.common.testrunner.TravellerTestConfig
import com.karhoo.karhootraveller.login.login
import com.karhoo.karhootraveller.menu.menu
import com.karhoo.karhootraveller.presentation.splash.SplashActivity
import com.karhoo.karhootraveller.profile.user.userProfile
import com.karhoo.karhootraveller.registration.registration
import com.karhoo.uisdk.booking.booking
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_PROVIDER
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.REVERSE_GEO_SUCCESS
import com.karhoo.uisdk.util.TestData.Companion.USER_INFO
import com.karhoo.uisdk.util.TestData.Companion.USER_UPDATED_INFO
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
class SplashTests : Launch {

    @get:Rule
    var wireMockRule = WireMockRule(WireMockConfiguration.options().port(TravellerTestConfig.PORT_NUMBER), false)

    @get:Rule
    val activityRule: ActivityTestRule<SplashActivity> =
            ActivityTestRule(SplashActivity::class.java, false, false)

    private var flakyRule = FlakyTestRule()

    @get:Rule
    var chain: RuleChain = RuleChain.outerRule(flakyRule)
            .around(activityRule)

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(Intent())
    }

    @Before
    fun clearUser() {
        preferences {
            clearUserPreference()
        }
    }

    @After
    fun tearDown() {
        preferences {
            clearUserPreference()
        }
    }

    /**
     * Given:   I am on the splash screen
     * When:    I click on the Request Invite button
     * Then:    I am taken to the registration screen
     **/
    @Test
    fun userOpensRegistrationScreenFromSplash() {
        splash(this) {
            shortSleep()
            clickOnRequestInviteButton()
        } result {
            registrationPageIsShown()
        }
    }

    /**
     * Given:   I am on the splash screen
     * When:    I open the registration screen
     * And:     I press back
     * Then:    I am returned to the splash screen
     **/
    @Test
    @AllowFlaky(attempts = 3)
    fun userReturnsBackToSplashFromRegistration() {
        splash(this) {
            clickOnRequestInviteButton()
        }
        registration {
            clickBackToolbarButton()
        }
        splash {
        } result {
            splashScreenIsVisible()
        }
    }

    /**
     * Given:   I am on the splash screen
     * When:    I click on the Sign In button
     * Then:    I am taken to the Sign in screen
     **/
    @Test
    fun userOpensSignInScreenFromSplash() {
        splash(this) {
            shortSleep()
            clickOnSignInButton()
        } result {
            loginPageIsShown()
        }
    }

    /**
     * Given:   I am On the Splash screen
     * When:    I log in and then log out
     * Then:    I am logged out and taken to the splash screen
     **/
    @Test
    fun userIsSuccessfullyLoggedOut() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            userProfileResponse(HTTP_OK, USER_INFO)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        splash(this) {
            mediumSleep()
            clickOnSignInButton()
        }
        login {
            validEmailAndPasswordLogin()
            userClicksOnSignInButton()
        }
        booking {
            pressMenuButton()
        }
        menu {
            clickOnProfileButton()
        }
        userProfile {
            clickMenuToolbarButton()
            clickSignOutButton()
        }
        splash {
            mediumSleep()
        } result {
            splashScreenIsVisible()
        }
    }

    /**
     * Given:   Given I am on the sign in screen
     * When:    I press the device's back button on the Sign in page
     * Then:    I should get back to the splash screen
     **/
    @Test
    fun userReturnsToSplashFromLoginPageBackButton() {
        splash(this) {
            mediumSleep()
            clickOnSignInButton()
        }
        login {
            pressDeviceBackButton()
        }
        splash {
        } result {
            splashScreenIsVisible()
        }
    }

    /**
     * Given:   Given I am on the sign in screen
     * When:    I press the top bar back button on the Sign in page
     * Then:    I should get back to the splash screen
     **/
    @Test
    fun userReturnsToSplashFromLoginPageCancelButton() {
        splash(this) {
            mediumSleep()
            clickOnSignInButton()
        }
        login {
            clickButtonByContentDescription("Navigate up")
        }
        splash {
        } result {
            splashScreenIsVisible()
        }
    }

    /**
     * Given:   I am on the registration page
     * When:    I enter valid credentials throughout
     * And:     I am not already registered
     * Then:    I receive confirmation that my registration has been successful
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun userRegistersSuccessfully() {
        serverRobot {
            successfulToken()
            registerUserResponse(HTTP_OK, USER_INFO)
        }
        splash(this) {
            clickOnRequestInviteButton()
        }
        registration {
            fillFullRegistration()
            clickContinueButtonRegistration()
        }
        splash {
            mediumSleep()
        } result {
            registrationConfirmationMessageIsShown()
        }
        splash {
            clickGotItButton()
            shortSleep()
        } result {
            splashScreenIsVisible()
        }
    }

    /**
     * Given:   I am on logged in with a user
     * When:    I log out
     * And:     I log in with a new user
     * Then:    The new user is logged in
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun logInWithADifferentUser() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            userProfileResponse(HTTP_OK, USER_INFO)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        splash(this) {
            mediumSleep()
            clickOnSignInButton()
        }
        login {
            validEmailAndPasswordLogin()
            userClicksOnSignInButton()
        }
        booking {
            mediumSleep()
            pressMenuButton()
        }
        menu {
            clickOnProfileButton()
        }
        userProfile {
            clickMenuToolbarButton()
            clickSignOutButton()
        }
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            userProfileResponse(HTTP_OK, USER_UPDATED_INFO)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        splash {
            mediumSleep()
            clickOnSignInButton()
        }
        login {
            validEmailAndPasswordLogin()
            userClicksOnSignInButton()
        }
        booking {
            pressMenuButton()
        }
        menu {
            clickOnProfileButton()
        }
        userProfile {
            shortSleep()
        } result {
            updatedProfileChangesFullCheck()
        }
    }

    /**
     * Given:   I am on the login screen
     * When:    I enter valid credentials
     * Then:    I am taken to the map scren
     **/
    @Test
    fun userSuccessfullyLogsIn() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            userProfileResponse(HTTP_OK, USER_INFO)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        splash(this) {
            shortSleep()
            clickOnSignInButton()
        }
        login {
            validEmailAndPasswordLogin()
            userClicksOnSignInButton()
        }
        booking {
            mediumSleep()
        } result {
            checkBookingScreenIsShown()
        }
    }
}

