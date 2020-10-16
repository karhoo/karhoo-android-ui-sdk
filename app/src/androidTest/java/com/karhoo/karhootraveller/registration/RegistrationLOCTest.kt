package com.karhoo.karhootraveller.registration

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.common.Launch
import com.karhoo.karhootraveller.common.networkServiceRobot
import com.karhoo.karhootraveller.common.testrunner.TravellerTestConfig
import com.karhoo.karhootraveller.presentation.register.RegistrationActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationLOCTest : Launch {

    @get:Rule
    var wireMockRule = WireMockRule(TravellerTestConfig.PORT_NUMBER)

    @get:Rule
    val activityRule: ActivityTestRule<RegistrationActivity> =
            ActivityTestRule(RegistrationActivity::class.java, false, false)

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(Intent())
    }

    /**
     * Given:   The user is on the splash screen
     * When:    The wifi is disabled
     * Then:    The snackbar should show to enable wifi
     **/
    @Test
    fun snackbarShowsToTheUserWhenWifiIsDisabledSplashScreen() {
        registration(this) {
            networkServiceRobot {
                disableNetwork(activityRule.activity.applicationContext)
            }
            mediumSleep()
        } result {
            checkSnackbarWithText(R.string.network_error)
        }
    }
}