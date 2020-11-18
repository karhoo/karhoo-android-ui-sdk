package com.karhoo.karhootraveller.profile.user

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.common.Launch
import com.karhoo.karhootraveller.common.networkServiceRobot
import com.karhoo.karhootraveller.common.preferences
import com.karhoo.karhootraveller.common.testrunner.TravellerTestConfig
import com.karhoo.karhootraveller.presentation.profile.ProfileActivity
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileLOCTest : Launch {

    @get:Rule
    var wireMockRule = WireMockRule(TravellerTestConfig.PORT_NUMBER)

    @get:Rule
    val activityRule: ActivityTestRule<ProfileActivity> =
            ActivityTestRule(ProfileActivity::class.java, false, false)

    @After
    fun tearDown() {
        preferences {
            clearUserPreference()
        }
    }

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(Intent())
    }

    /**
     * Given:   The user is on the splash screen
     * When:    The wifi is disabled
     * Then:    The snackbar should show to enable wifi
     **/
    @Test
    fun snackbarShowsToTheUserWhenWifiIsDisabledProfileScreen() {
        userProfile(this) {
            networkServiceRobot {
                disableNetwork(activityRule.activity.applicationContext)
            }
            mediumSleep()
        } result {
            checkSnackbarWithText(R.string.network_error)
        }
    }
}
