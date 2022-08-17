package com.karhoo.uisdk.rides

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.ridedetail.rideDetail
import com.karhoo.uisdk.screen.rides.RidesActivity
import com.karhoo.uisdk.trip.trip
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.CANCEL_WITHOUT_BOOKING_FEE
import com.karhoo.uisdk.util.TestData.Companion.CANCEL_WITH_BOOKING_FEE
import com.karhoo.uisdk.util.TestData.Companion.DRIVER_TRACKING
import com.karhoo.uisdk.util.TestData.Companion.FARE_CANCELLED
import com.karhoo.uisdk.util.TestData.Companion.FARE_COMPLETE
import com.karhoo.uisdk.util.TestData.Companion.GENERAL_ERROR
import com.karhoo.uisdk.util.TestData.Companion.RIDE_SCREEN_CANCELLED_DRIVER
import com.karhoo.uisdk.util.TestData.Companion.RIDE_SCREEN_CANCELLED_KARHOO
import com.karhoo.uisdk.util.TestData.Companion.RIDE_SCREEN_CANCELLED_USER
import com.karhoo.uisdk.util.TestData.Companion.RIDE_SCREEN_COMPLETED
import com.karhoo.uisdk.util.TestData.Companion.RIDE_SCREEN_COMPLETED_AIRPORT_PICKUP
import com.karhoo.uisdk.util.TestData.Companion.RIDE_SCREEN_CONFIRMED
import com.karhoo.uisdk.util.TestData.Companion.RIDE_SCREEN_DER
import com.karhoo.uisdk.util.TestData.Companion.RIDE_SCREEN_DER_AIRPORT_PICKUP
import com.karhoo.uisdk.util.TestData.Companion.RIDE_SCREEN_INCOMPLETE
import com.karhoo.uisdk.util.TestData.Companion.RIDE_SCREEN_PREBOOKED
import com.karhoo.uisdk.util.TestData.Companion.RIDE_SCREEN_PREBOOKED_CANCELLED_BY_FLEET
import com.karhoo.uisdk.util.TestData.Companion.RIDE_SCREEN_PREBOOKED_CANCELLED_BY_USER
import com.karhoo.uisdk.util.TestData.Companion.TRIP
import com.karhoo.uisdk.util.TestData.Companion.TRIP_DER
import com.karhoo.uisdk.util.TestData.Companion.TRIP_HISTORY_EMPTY
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_DER
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_NO_CONTENT
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
class RidesTests : Launch {

    @get:Rule
    var wireMockRule = WireMockRule(UiSDKTestConfig.PORT_NUMBER)

    @get:Rule
    val activityRule: ActivityTestRule<RidesActivity> =
            ActivityTestRule(RidesActivity::class.java, false, false)

    @After
    fun tearDown() {
        wireMockRule.resetAll()
    }

    /**
     * Given:   I have a no prebooked ride
     * When:    I am on the upcoming rides
     * Then:    I can see the message that I have no upcoming rides
     **/
    @Test
    fun checkThatUserHasNoUpcomingRides() {
        serverRobot {
            successfulToken()
            upcomingRidesResponse(HTTP_OK, TRIP_HISTORY_EMPTY)
        }
        rides(this) {
            shortSleep()
        } result {
            checkNoUpcomingBookings()
        }
    }

    /**
     * Given:   I have no past rides
     * When:    I am on Past rides
     * Then:    I can see the message that I have no past rides
     **/
    @Test
    fun checkThatUserHasNoPastRides() {
        serverRobot {
            successfulToken()
            pastRidesResponse(HTTP_OK, TRIP_HISTORY_EMPTY)
        }
        rides(this) {
            clickPastBookingsTabButton()
            shortSleep()
        } result {
            checkNoPastBookings()
        }
    }

    /**
     * Given:   I have a completed ride
     * When:    I look at past rides
     * Then:    The completed ride has the right status
     **/
    @Test
    fun completedRideHasCorrectStatus() {
        serverRobot {
            successfulToken()
            pastRidesResponse(HTTP_OK, RIDE_SCREEN_COMPLETED)
        }
        rides(this) {
            clickPastBookingsTabButton()
            shortSleep()
        } result {
            pastBookingHasExpectedStatus(R.string.kh_uisdk_completed)
        }
    }

    /**
     * Given:   I have a cancelled by user ride
     * When:    I look at past rides
     * Then:    The ride status is "cancelled"
     **/
    @Test
    fun cancelledStatusOnCancelledByUserPastRide() {
        serverRobot {
            successfulToken()
            pastRidesResponse(HTTP_OK, RIDE_SCREEN_CANCELLED_USER)
        }
        rides(this) {
            clickPastBookingsTabButton()
            shortSleep()
        } result {
            pastBookingHasExpectedStatus(R.string.kh_uisdk_cancelled)
        }
    }

    /**
     * Given:   I have a cancelled by driver ride
     * When:    I look at past rides
     * Then:    The ride status is "cancelled"
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun cancelledStatusOnCancelledByDriverPastRide() {
        serverRobot {
            successfulToken()
            pastRidesResponse(HTTP_OK, RIDE_SCREEN_CANCELLED_DRIVER)
        }
        rides(this) {
            clickPastBookingsTabButton()
            shortSleep()
        } result {
            pastBookingHasExpectedStatus(R.string.kh_uisdk_cancelled)
        }
    }

    /**
     * Given:   I have a cancelled by Karhoo ride
     * When:    I look at past rides
     * Then:    The ride status is "cancelled"
     **/
    @Test
    fun cancelledStatusOnCancelledByKarhooPastRide() {
        serverRobot {
            successfulToken()
            pastRidesResponse(HTTP_OK, RIDE_SCREEN_CANCELLED_KARHOO)
            fareResponse(HTTP_OK, FARE_CANCELLED, tripId = TestData.TRIP.tripId)
        }
        rides(this) {
            clickPastBookingsTabButton()
            shortSleep()
        } result {
            pastBookingHasExpectedStatus(R.string.kh_uisdk_cancelled)
        }
    }

    /**
     * Given:   I have an incomplete ride
     * When:    I look at past rides
     * Then:    The ride status is Pending
     **/
    @Test
    fun incompleteStatusShowingAsPendingPastRides() {
        serverRobot {
            successfulToken()
            pastRidesResponse(HTTP_OK, RIDE_SCREEN_INCOMPLETE)
        }
        rides(this) {
            clickPastBookingsTabButton()
            shortSleep()
        } result {
            pastBookingHasExpectedPrice(R.string.kh_uisdk_cancelled)
        }
    }

    //    /**
    //     * Given:   I have more than one past ride
    //     * When:    I look at the list of past rides
    //     * Then:    They are sorted in chronological order (most recent first - top of the list)
    //     **/
    //    @Test
    //    fun pastRidesSortedInChronologicalOrder() {
    //
    //    }

    //    /**
    //     * Given:   I have more than one upcoming ride
    //     * When:    I look at the list of upcoming rides
    //     * Then:    They are sorted in chronological order (soonest first - most further away last)
    //     **/
    //    @Test
    //    fun upcomingRidesSortedInChronologicalOrder() {
    //
    //    }

    //    /**
    //     * Given:   I have a past trip cancelled
    //     * When:    I attempt to rebook it
    //     * Then:    The ride is rebooked successfully
    //     **/
    //    @Test
    //    fun rebookPastCancelledRideSuccessfully() {
    //
    //    }

    /**
     * Given:   I am on the Rides screen
     * When:    An error occurs
     * Then:    The screen shows the correct error message
     **/
    @Test
    fun checkScreenErrorTextRideScreen() {
        serverRobot {
            successfulToken()
            upcomingRidesResponse(HTTP_INTERNAL_ERROR, GENERAL_ERROR)
        }
        rides(this) {
            shortSleep()
        } result {
            checkErrorMessageIsShown(R.string.kh_uisdk_K0001)
        }
    }

    /**
     * Given:   I have an upcoming ride with a meeting point for pickup
     * When:    I look at the ride on the Rides screen
     * Then:    I can see the pickup type on the upcoming ride
     **/
    @Test
    fun airportPickUpTypeVisibleUpcomingTrip() {
        serverRobot {
            successfulToken()
            upcomingRidesResponse(HTTP_OK, RIDE_SCREEN_DER_AIRPORT_PICKUP)
        }
        rides(this) {
            shortSleep()
        } result {
            pickUpTypeVisibleRidesScreen("Meet and Greet")
        }
    }

    /**
     * Given:   I have an upcoming ride with an airport ride for dropoff
     * When:    I look at the ride on the Rides screen
     * Then:    I cannot see the pickup type on the upcoming ride
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun airportMeetingPointNotVisibleOnDropoffUpcoming() {
        serverRobot {
            successfulToken()
            upcomingRidesResponse(HTTP_OK, RIDE_SCREEN_CONFIRMED)
        }
        rides(this) {
            shortSleep()
        } result {
            pickUpTypeLabelNotVisibleOnDropoffUpcoming()
            callFleetButtonIsEnabled()
        }
    }

    /**
     * Given:   I have a past ride with a meeting point for pickup
     * When:    I look at the ride on the Rides screen
     * Then:    I can see the pickup type on the past ride
     **/
    @Test
    fun airportPickUpTypeVisiblePastTrip() {
        serverRobot {
            successfulToken()
            pastRidesResponse(HTTP_OK, RIDE_SCREEN_COMPLETED_AIRPORT_PICKUP)
        }
        rides(this) {
            clickPastBookingsTabButton()
        } result {
            pickUpTypeVisibleRidesScreen("Meet and Greet")
        }
    }

    /**
     * Given:   I have a past ride with an airport for dropoff
     * When:    I look at the ride on the Rides screen
     * Then:    I cannot see the pickup type on the past ride
     **/
    @Test
    fun airportPickUpTypeNotVisiblePastTripDropoff() {
        serverRobot {
            successfulToken()
            pastRidesResponse(HTTP_OK, RIDE_SCREEN_COMPLETED)
        }
        rides(this) {
            clickPastBookingsTabButton()
            shortSleep()
        } result {
            pickUpTypeLabelNotVisibleOnDropoffPast()
        }
    }

    /**
     * Given:   I am on Upcoming rides
     * When:    I select a prebooked ride and cancel it
     * Then:    I get a popup informing me of the cancellation success
     **/
    @Test
    fun prebookCancellationConfirmation() {
        serverRobot {
            successfulToken()
            upcomingRidesResponse(HTTP_OK, RIDE_SCREEN_PREBOOKED)
        }
        rides(this) {
            shortSleep()
            clickOnFirstRide()
        }
        rideDetail {
            shortSleep()
        }
        serverRobot {
            cancelFeeResponse(code = HTTP_OK, response = CANCEL_WITH_BOOKING_FEE, trip = TRIP.tripId)
            cancelResponse(code = HTTP_NO_CONTENT, response = RIDE_SCREEN_CANCELLED_USER, trip = TRIP.tripId)
        }
        rideDetail {
            clickCancelRideDetails()
            clickOnOkay()
        } result {
            cancelConfirmationIsVisible()
        }
    }

    /**
     * Given:  I am on the ride details screen
     * When:   I successfully cancel a prebooked ride
     * And:    There is no cancellation fee
     * And:    I click on Dismiss the confirmation dialog
     * Then:   I am not shown a cancellation fee
     * And:    I am taken back to the upcoming rides screen when I accept the cancellation
     **/
    @Test
    fun userIsTakenToUpcomingRidesScreenAfterCancellingPrebookWithoutFee() {
        serverRobot {
            successfulToken()
            upcomingRidesResponse(HTTP_OK, RIDE_SCREEN_PREBOOKED)
        }
        rides(this) {
            shortSleep()
            clickOnFirstRide()
        }
        rideDetail {
            shortSleep()
        }
        serverRobot {
            cancelFeeResponse(code = HTTP_OK, response = CANCEL_WITHOUT_BOOKING_FEE, trip = TRIP
                    .tripId)
            cancelResponse(code = HTTP_NO_CONTENT, response = RIDE_SCREEN_CANCELLED_USER, trip = TRIP.tripId)
            upcomingRidesResponse(HTTP_OK, TRIP_HISTORY_EMPTY)
        }
        rideDetail {
            clickCancelRideDetails()
            checkCancellationFeeIsNotShown()
            clickOnDismiss()
            clickCancelRideDetails()
            clickOnOkay()
            clickOnDismiss()
        }
        rides {
            shortSleep()
        } result {
            checkNoUpcomingBookings()
        }
    }

    /**
     * Given:  I am on the ride details screen
     * When:   I successfully cancel a prebooked ride
     * And:    There is a cancellation fee
     * And:    I click on Dismiss the confirmation dialog
     * Then:   I am shown the cancellation fee
     * And:    I am taken back to the upcoming rides screen when I accept the cancellation
     **/
    @Test
    fun userIsTakenToUpcomingRidesScreenAfterCancellingPrebookWithFee() {
        serverRobot {
            successfulToken()
            upcomingRidesResponse(HTTP_OK, RIDE_SCREEN_PREBOOKED)
        }
        rides(this) {
            shortSleep()
            clickOnFirstRide()
        }
        rideDetail {
            shortSleep()
        }
        serverRobot {
            cancelFeeResponse(code = HTTP_OK, response = CANCEL_WITH_BOOKING_FEE, trip = TRIP.tripId)
            cancelResponse(code = HTTP_NO_CONTENT, response = RIDE_SCREEN_CANCELLED_USER, trip = TRIP.tripId)
            upcomingRidesResponse(HTTP_OK, TRIP_HISTORY_EMPTY)
        }
        rideDetail {
            clickCancelRideDetails()
            checkCancellationFeeIsShown()
            clickOnDismiss()
            clickCancelRideDetails()
            clickOnOkay()
            clickOnDismiss()
        }
        rides {
            shortSleep()
        } result {
            checkNoUpcomingBookings()
        }
    }

    /**
     * Given:   I have a prebooked ride cancelled by the fleet
     * When:    I look at the ride in past Rides
     * Then:    I can see the following: Quote name and logo, Date and time of booking,
    Cancelled Price, cancelled status.
     **/
    @Test
    fun prebookCancelledByFleetShowsCorrectStatus() {
        serverRobot {
            successfulToken()
            pastRidesResponse(HTTP_OK, RIDE_SCREEN_PREBOOKED_CANCELLED_BY_FLEET)
            fareResponse(code = HTTP_OK, response = FARE_CANCELLED, tripId = TestData.TRIP.tripId)
        }
        rides(this) {
            clickPastBookingsTabButton()
            shortSleep()
        } result {
            cancelledByFleetPrebookedFullCheck()
        }
    }

    /**
     * Given:   I have a prebooked ride cancelled by the user
     * When:    I look at the ride in past Rides
     * Then:    I can see the following: Quote name and logo, Date and time of booking,
    Cancelled Price, cancelled status.
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun prebookCancelledByUserShowsCorrectStatus() {
        serverRobot {
            successfulToken()
            pastRidesResponse(HTTP_OK, RIDE_SCREEN_PREBOOKED_CANCELLED_BY_USER)
            fareResponse(code = HTTP_OK, response = FARE_CANCELLED, tripId = TestData.TRIP.tripId)
        }
        rides(this) {
            clickPastBookingsTabButton()
            shortSleep()
        } result {
            cancelledByUserPrebookedFullCheck()
        }
    }

    /**
     * Given:   I have a ride in Progress (DER)
     * When:    I look at the ride summary in Upcoming rides
     * Then:    I can see the following: Quote name and logo, Date, category of car and reg
    number, contact driver (or fleet), track driver button, addresses
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun fullCheckRideInProgressDetailsOnRideScreen() {
        serverRobot {
            successfulToken()
            upcomingRidesResponse(HTTP_OK, RIDE_SCREEN_DER)
        }
        rides(this) {
            shortSleep()
        } result {
            DERTripUpcomingRidesFullCheck()
        }
    }

    /**
     * Given:   I have a prebooked ride
     * When:    I look at the ride summary in Upcoming rides
     * Then:    I can see the following: Addresses, Quote name and logo, Date, contact
    fleet
     **/
    @Test
    fun fullCheckRideDetailsUpcomingRide() {
        serverRobot {
            successfulToken()
            upcomingRidesResponse(HTTP_OK, RIDE_SCREEN_PREBOOKED)
        }
        rides(this) {
            shortSleep()
        } result {
            prebookedTripUpcomingRidesFullCheck()
        }
    }

    /**
     * Given:   I have a completed past ride
     * When:    I look at the ride summary in past rides
     * Then:    I can see the following: Quote name and logo, Date and time of booking,
    category of car and reg, number, contact driver (or fleet), Price, trip id, report issue,
    rebook ride, trip status, addresses
     **/
    @Test
    fun fullCheckPastRideCompletedDetailsRideScreen() {
        serverRobot {
            successfulToken()
            pastRidesResponse(HTTP_OK, RIDE_SCREEN_COMPLETED)
            fareResponse(code = HTTP_OK, response = FARE_COMPLETE, tripId = TestData.TRIP.tripId)
        }
        rides(this) {
            clickPastBookingsTabButton()
            shortSleep()
        } result {
            completedTripPastRidesScreenFullCheck()
        }
    }

    /**
     * Given:   I am on the upcoming rides screen
     * When:    I select one of the rides and then press back
     * Then:    I am returned to the ride screen.
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun userNavigatesFromRideDetailsToUpcomingRidesScreen() {
        serverRobot {
            successfulToken()
            upcomingRidesResponse(HTTP_OK, RIDE_SCREEN_PREBOOKED)
        }
        rides(this) {
            shortSleep()
            clickOnFirstRide()
        }
        rideDetail {
            shortSleep()
            pressDeviceBackButton()
        }
        rides {
            shortSleep()
        } result {
            checkRidesScreenIsShown()
            prebookedTripUpcomingRidesFullCheck()
        }
    }

    /**
     * Given:   I am on the past rides screen
     * When:    I select one of the rides and then press back
     * Then:    I am returned to the ride screen.
     **/
    @Test
    fun userNavigatesFromRideDetailsToPastRidesScreen() {
        serverRobot {
            successfulToken()
            pastRidesResponse(HTTP_OK, RIDE_SCREEN_COMPLETED)
            fareResponse(code = HTTP_OK, response = FARE_COMPLETE, tripId = TestData.TRIP.tripId)
        }
        rides(this) {
            clickPastBookingsTabButton()
            shortSleep()
            clickOnFirstRide()
        }
        rideDetail {
            pressDeviceBackButton()
        }
        rides {
            shortSleep()
        } result {
            checkRidesScreenIsShown()
            completedTripPastRidesScreenFullCheck()
        }
    }

    /**
     * Given: I have a ride in Progress and I am on the ride details
     * When: When I press Track driver
     * Then: I am taken to the trip screen
     **/
    @Test
    fun userIsTakenFromRidesScreenToTripTracking() {
        serverRobot {
            successfulToken()
            upcomingRidesResponse(HTTP_OK, RIDE_SCREEN_DER)
            mockTripSuccessResponse(
                    status = TRIP_STATUS_DER,
                    tracking = DRIVER_TRACKING,
                    details = TRIP_DER
                                   )
        }
        rides(this) {
            shortSleep()
            clickOnTrackDriver()
        }
        trip {
            longSleep()
        } result {
            DERFullScreenCheck(pickupText = TRIP_DER.origin?.displayAddress.orEmpty(),
                               destinationText = TRIP_DER.destination?.displayAddress.orEmpty())
        }
    }

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(Intent())
    }

}
