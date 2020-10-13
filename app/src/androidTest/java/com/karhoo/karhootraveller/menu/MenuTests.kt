package com.karhoo.karhootraveller.menu

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.karhootraveller.common.preferences
import com.karhoo.karhootraveller.common.testrunner.TravellerTestConfig
import com.karhoo.karhootraveller.profile.user.userProfile
import com.karhoo.uisdk.booking.booking
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.LONG
import com.karhoo.uisdk.util.TestData.Companion.SHORT
import com.karhoo.uisdk.util.TestData.Companion.USER
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuTests : Launch {

    @get:Rule
    val activityRule: ActivityTestRule<BookingActivity> =
            ActivityTestRule(BookingActivity::class.java, false, false)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    var wireMockRule = WireMockRule(options().port(TravellerTestConfig.PORT_NUMBER), false)

    private val intent = Intent()

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(this.intent)
    }

    /**
     * Given:   I am on the profile screen
     * When:    I press the back button in the top bar
     * Then:    I am returned to the booking/map screen
     **/
    @Test
    @AllowFlaky(attempts = 3)
    fun userClosesProfileScreen() {
        preferences {
            setUserPreference(USER)
        }
        booking(this) {
            pressMenuButton()
        }
        menu {
            clickOnProfileButton()
        }
        userProfile {
            clickBackToolbarButton()
            waitFor(SHORT)
        }
        booking {
        } result {
            checkBookingScreenIsShown()
        }
    }

    /**
     * Given:   I am on the side menu
     * When:    I click help
     * Then:    I am taken to the Help page
     **/
    @Test
    fun userSeesHelpPageFromSideMenu() {
        preferences {
            setUserPreference(USER)
        }
        booking(this) {
            pressMenuButton()
        }
        menu {
            clickOnHelpButton()
            sleep(LONG)
        } result {
            helpPageIsShown()
        }
    }

    /**
     * Given:   I am on the side menu
     * When:    I click on About
     * Then:    I can see the following elements: Version, T&Cs button, PP button and Licence
     * button, About title
     **/
    @Test
    fun aboutPageElementsCheckSideMenu() {
        preferences {
            setUserPreference(USER)
        }
        booking(this) {
            pressMenuButton()
        }
        menu {
            clickOnAboutButton()
        } result {
            fullCheckAbout()
        }
    }

    /**
     * Given:   I am on the About Screen
     * When:    I click on T&C's
     * Then:    Karhoo's T&C's is shown
     **/
    @Test
    fun termsAndConditionsShownAboutScreen() {
        preferences {
            setUserPreference(USER)
        }
        booking(this) {
            pressMenuButton()
        }
        menu {
            clickOnAboutButton()
            clickOnTermsAndConditionsAbout()
        } result {
            termsAndConditionsPageIsShownAbout()
        }
    }

    /**
     * Given:   I am on the About Screen
     * When:    I click on T&C's
     * Then:    Karhoo's Privacy Policy is shown
     **/
    @Test
    fun privacyPolicyShownAboutScreen() {
        preferences {
            setUserPreference(USER)
        }
        booking(this) {
            pressMenuButton()
        }
        menu {
            clickOnAboutButton()
            clickOnPrivacyPolicyAbout()
        } result {
            privacyPolicyPageIsShownAbout()
        }
    }

    /**
     * Given:   I am on the About Screen
     * When:    I click on T&C's
     * Then:    Karhoo's Privacy Policy is shown
     **/
    @Test
    fun licenceAttributionShownAboutScreen() {
        preferences {
            setUserPreference(USER)
        }
        booking(this) {
            pressMenuButton()
        }
        menu {
            clickOnAboutButton()
            clickOnLicenceAttributionAbout()
        } result {
            licenceAttributionPageIsShownAbout()
        }
    }

    /**
     * Given:   I am on the about screen
     * When:    I click the back button
     * Then:    I am returned to the booking screen
     **/
    @Test
    fun userReturnsToBookingScreenFromAbout() {
        preferences {
            setUserPreference(USER)
        }
        booking(this) {
            pressMenuButton()
        }
        menu {
            clickOnAboutButton()
            sleep()
            clickBackToolbarButton()
        }
        booking {
        } result {
            checkBookingScreenIsShown()
        }
    }
}