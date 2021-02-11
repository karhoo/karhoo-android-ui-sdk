package com.karhoo.uisdk.ridedetail

import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.preferences
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.rides.detail.RideDetailActivity
import com.karhoo.uisdk.util.TestData.Companion.COULD_NOT_CANCEL_TRIP
import com.karhoo.uisdk.util.TestData.Companion.FARE_COMPLETE
import com.karhoo.uisdk.util.TestData.Companion.TRIP
import com.karhoo.uisdk.util.TestData.Companion.TRIP_CANCELLED_BY_DRIVER_MEETING_POINT_UNSET
import com.karhoo.uisdk.util.TestData.Companion.TRIP_CANCELLED_BY_KARHOO_MEETING_POINT_UNSET
import com.karhoo.uisdk.util.TestData.Companion.TRIP_CANCELLED_BY_USER_MEETING_POINT_UNSET
import com.karhoo.uisdk.util.TestData.Companion.TRIP_COMPLETED
import com.karhoo.uisdk.util.TestData.Companion.TRIP_CONFIRMED_MEETING_POINT_UNSET
import com.karhoo.uisdk.util.TestData.Companion.TRIP_DRIVER_EN_ROUTE_POINT_UNSET
import com.karhoo.uisdk.util.TestData.Companion.TRIP_INCOMPLETE
import com.karhoo.uisdk.util.TestData.Companion.TRIP_POB
import com.karhoo.uisdk.util.TestData.Companion.TRIP_PREBOOKED
import com.karhoo.uisdk.util.TestData.Companion.TRIP_REQUESTED_DETAILS
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_REQUESTED
import com.karhoo.uisdk.util.TestData.Companion.USER
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
class RideDetailTests : Launch {

    @get:Rule
    val activityRule: ActivityTestRule<RideDetailActivity> =
            ActivityTestRule(RideDetailActivity::class.java, false, false)

    @get:Rule
    var wireMockRule = WireMockRule(UiSDKTestConfig.PORT_NUMBER) // No-args constructor defaults to port 8080

    private var intent: Intent? = null

    @After
    fun tearDown() {
        wireMockRule.resetAll()
    }

    /**
     * Given:   I am on a completed past ride details
     * When:    I check all the elements
     * Then:    I can see: Fleet name and logo, date and time of the trip, pick up address, dropoff
     *                     address, vehicle details, status:completed, Price, KarhooID, Rate trip
     *                     elements, report issue button, rebook ride button
     **/
    @Test
    fun completedRideCheck() {
        serverRobot {
            successfulToken()
            fareResponse(code = HTTP_OK, response = FARE_COMPLETE, tripId = TRIP.tripId)
        }
        rideDetail(this, TRIP_COMPLETED_INTENT) {
            shortSleep()
        } result {
            completedRideFullCheck()
        }
    }

    override fun launch(intent: Intent?) {
        if (intent != null) {
            activityRule.launchActivity(intent)
        } else {
            activityRule.launchActivity(this.intent)
        }
    }

    companion object {
        private const val TRIP_EXTRA = RideDetailActivity.Builder.EXTRA_TRIP

        private val TRIP_INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TRIP_EXTRA, TRIP)
            })
        }

        private val TRIP_COMPLETED_INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TRIP_EXTRA, TRIP_COMPLETED)
            })
        }

        private val TRIP_INCOMPLETE_INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TRIP_EXTRA, TRIP_INCOMPLETE)
            })
        }

        private val TRIP_CANCELLED_BY_DRIVER_INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TRIP_EXTRA, TRIP_CANCELLED_BY_DRIVER_MEETING_POINT_UNSET)
            })
        }

        private val TRIP_CANCELLED_BY_USER_INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TRIP_EXTRA, TRIP_CANCELLED_BY_USER_MEETING_POINT_UNSET)
            })
        }

        private val TRIP_CANCELLED_BY_KARHOO_INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TRIP_EXTRA, TRIP_CANCELLED_BY_KARHOO_MEETING_POINT_UNSET)
            })
        }

        private val TRIP_PREBOOKED_INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TRIP_EXTRA, TRIP_PREBOOKED)
            })
        }

        private val TRIP_CONFIRMED_INTENT_MEETING_POINT_UNSET = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TRIP_EXTRA, TRIP_CONFIRMED_MEETING_POINT_UNSET)
            })
        }

        private val TRIP_DER_INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TRIP_EXTRA, TRIP_DRIVER_EN_ROUTE_POINT_UNSET)
            })
        }

        private val TRIP_POB_INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TRIP_EXTRA, TRIP_POB)
            })
        }
    }
}
