package com.karhoo.uisdk.trip

import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.booking.booking
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.trip.TripActivity
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.ARRIVED_NOTIFICATION
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_PROVIDER
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.DER_NOTIFICATION
import com.karhoo.uisdk.util.TestData.Companion.DRIVER_TRACKING
import com.karhoo.uisdk.util.TestData.Companion.GENERAL_ERROR
import com.karhoo.uisdk.util.TestData.Companion.QUOTE_LIST_ID_ASAP
import com.karhoo.uisdk.util.TestData.Companion.REVERSE_GEO_SUCCESS
import com.karhoo.uisdk.util.TestData.Companion.TRIP
import com.karhoo.uisdk.util.TestData.Companion.TRIP_ARRIVED
import com.karhoo.uisdk.util.TestData.Companion.TRIP_CANCELLED_BY_FLEET
import com.karhoo.uisdk.util.TestData.Companion.TRIP_DER
import com.karhoo.uisdk.util.TestData.Companion.TRIP_DER_NO_DRIVER_DETAILS
import com.karhoo.uisdk.util.TestData.Companion.TRIP_DER_NO_NUMBER_PLATE
import com.karhoo.uisdk.util.TestData.Companion.TRIP_DER_NO_VEHICLE_AND_DRIVER_DETAILS
import com.karhoo.uisdk.util.TestData.Companion.TRIP_DER_NO_VEHICLE_DETAILS
import com.karhoo.uisdk.util.TestData.Companion.TRIP_DER_NO_VEHICLE_NUMBER_PLATE_AND_DRIVER_DETAILS
import com.karhoo.uisdk.util.TestData.Companion.TRIP_POB
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_ARRIVED
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_CANCELLED_BY_FLEET
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_CANCELLED_BY_USER
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_DER
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_POB
import com.karhoo.uisdk.util.TestData.Companion.VEHICLES_ASAP
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.net.HttpURLConnection.HTTP_CREATED
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
@LargeTest
class TripTests : Launch {

    @get:Rule
    val activityRule: ActivityTestRule<TripActivity> =
            ActivityTestRule(TripActivity::class.java, false, false)

    private var flakyRule = FlakyTestRule()

    @get:Rule
    var chain: RuleChain = RuleChain.outerRule(flakyRule)
            .around(activityRule)

    @get:Rule
    var wireMockRule = WireMockRule(options()
                                            .port(UiSDKTestConfig.PORT_NUMBER)
                                            .notifier(ConsoleNotifier(true)))

    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setUp() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
        }
    }

    @After
    fun tearDown() {
        wireMockRule.resetAll()
    }

    /**
     * Given:   I am on the trip screen
     * When:    An error occurs
     * Then:    The snackbar displays the correct message
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun checkSnackbarErrorText() {
        mockTripFailedResponse(
                status = GENERAL_ERROR,
                tracking = GENERAL_ERROR,
                details = GENERAL_ERROR)
        trip(this) {
            shortSleep()
        } result {
            checkSnackbarWithText("General request error. [K0001]")
        }
    }

    /**
     * Given:   A driver has been allocated
     * When:    I look at the driver details
     * Then:    I can see: Driver photo (placeholder), Number plate, Driver name, Car type/model,
     * Licence number; Cancel ride, contact buttons are enabled.
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun checkDERDriverDetailsElements() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER)
        trip(this) {
            clickOnDriverDetails()
            shortSleep()
        } result {
            driverDERDetailsFullCheck()
        }
    }

    /**
     * Given:   Driver is en route
     * When:    I look at the map
     * Then:    I can see: Pick up address, destination address, ETA bubble, Driver details
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun fullScreenCheckDER() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER)
        trip(this) {
        } result {
            DERFullScreenCheck(
                    pickupText = TRIP_DER.origin?.displayAddress.orEmpty(),
                    destinationText = TRIP_DER.destination?.displayAddress.orEmpty()
                              )
        }
    }

    /**
     * Given:   A driver has arrived
     * When:    I look at the driver details
     * Then:    I can see: Driver photo (placeholder), Number plate, Driver name, Car type/model,
     * Licence number; Cancel ride, contact buttons are enabled
     **/
    @Test
    fun checkArrivedDriverDetailsElements() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_ARRIVED,
                tracking = DRIVER_TRACKING,
                details = TRIP_ARRIVED)
        trip(this) {
            shortSleep()
            clickOnDriverDetails()
        } result {
            driverArrivedDetailsFullCheck()
        }
    }

    /**
     * Given:   Driver has arrived
     * When:    I look at the map
     * Then:    I can see: Pick up address, destination address, Driver details, no ETA bubble
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun fullScreenCheckArrived() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_ARRIVED,
                tracking = DRIVER_TRACKING,
                details = TRIP_ARRIVED)
        trip(this) {
            shortSleep()
        } result {
            ArrivedFullScreenCheck(
                    pickupText = TRIP_ARRIVED.origin?.displayAddress.orEmpty(),
                    destinationText = TRIP_ARRIVED.destination?.displayAddress.orEmpty()
                                  )
        }
    }

    /**
     * Given:   The Passenger is on board
     * When:    I look at the driver details
     * Then:    I can see: Driver photo (placeholder), Number plate, Driver name, Car type/model,
     * Licence number
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun checkPOBDriverDetailsElements() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_POB,
                tracking = DRIVER_TRACKING,
                details = TRIP_POB)
        trip(this) {
            clickOnDriverDetails()
        } result {
            driverDetailsFullCheckPOB()
        }
    }

    /**
     * Given:   Passenger is on Board
     * When:    I look at the map
     * Then:    I can see: Pick up address, destination address, Arrival bubble, Driver details
     **/
    @Test
    fun fullScreenCheckPOB() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_POB,
                tracking = DRIVER_TRACKING,
                details = TRIP_POB)
        trip(this) {
            shortSleep()
        } result {
            fullScreenCheckPOB(
                    pickupText = TestData.TRIP_ARRIVED.origin?.displayAddress.orEmpty(),
                    destinationText = TestData.TRIP_ARRIVED.destination?.displayAddress.orEmpty()
                              )
        }
    }

    /**
     * Given:   I have a ride in progress and the driver details are expanded
     * When:    I click details
     * Then:    Driver details are no longer expanded
     **/
    @Test
    @AllowFlaky(attempts = 3)
    fun driverDetailsSuccessfullyClosed() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_ARRIVED,
                tracking = DRIVER_TRACKING,
                details = TRIP_ARRIVED)
        trip(this) {
            clickOnDriverDetails()
            mediumSleep()
            clickOnDriverDetails()
        } result {
            driverDetailsNoLongerExpanded()
        }
    }

    /**
     * Given:   Trip status is DER
     * When:    When I cancel the trip
     * Then:    The trip is confirmed as cancelled
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun cancelWhenDERSuccessfully() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER)
        serverRobot {
            cancelFeeResponse(
                    code = HTTP_CREATED,
                    response = TRIP_STATUS_CANCELLED_BY_USER,
                    trip = TRIP.tripId)
        }
        trip(this) {
            clickOnDriverDetails()
            clickOnCancelRide()
            clickConfirmCancellation()
        } result {
            cancellationConfirmation()
        }
    }

    /**
     * Given:   Trip status is Arrived
     * When:    When I cancel the trip
     * Then:    The trip is confirmed as cancelled
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun cancelWhenDriverArrivedSuccessfully() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_ARRIVED,
                tracking = DRIVER_TRACKING,
                details = TRIP_ARRIVED)
        serverRobot {
            cancelFeeResponse(
                    code = HTTP_CREATED,
                    response = TRIP_STATUS_CANCELLED_BY_USER,
                    trip = TRIP.tripId)
        }
        trip(this) {
            clickOnDriverDetails()
            clickOnCancelRide()
            clickConfirmCancellation()
        } result {
            cancellationConfirmation()
        }
    }

    /**
     * Given:   Trip status is DER
     * When:    When I cancel the trip cancellation
     * Then:    The trip continues as normal
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun userCancelsCancellationOfRideDER() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER)
        trip(this) {
            clickOnDriverDetails()
            clickOnCancelRide()
            clickOnCancelYourRideCancellation()
        } result {
            checkRideInProgress()
        }
    }

    /**
     * Given:   Trip status is Arrived
     * When:    When I cancel the trip cancellation
     * Then:    The trip continues as normal
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun userCancelsCancellationOfRideArrived() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_ARRIVED,
                tracking = DRIVER_TRACKING,
                details = TRIP_ARRIVED)
        trip(this) {
            clickOnDriverDetails()
            clickOnCancelRide()
            clickOnCancelYourRideCancellation()
            shortSleep()
        } result {
            checkRideInProgress()
        }
    }

    /**
     * Given:   The driver is en route
     * When:    the fleet cancels the ride
     * Then:    I see a message that the booking is not cancelled for cause of no drivers, alternative option is available
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun fleetCancelledWhenDER() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_CANCELLED_BY_FLEET,
                tracking = DRIVER_TRACKING,
                details = TRIP_CANCELLED_BY_FLEET)
        trip(this) {
            result {
                fleetCancelledAfterDERVisible()
            }
        }
    }

    /**
     * Given:   The driver is en route
     * When:    the fleet cancels the ride
     * Then:    I see a message that the booking is not cancelled for cause of no drivers, alternative option is available
     **/
    @Test
    @Ignore
    @AllowFlaky(attempts = 5)
    fun alternativeAfterFleetCancelledWhenDER() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_CANCELLED_BY_FLEET,
                tracking = DRIVER_TRACKING,
                details = TRIP_CANCELLED_BY_FLEET)
        serverRobot {
            successfulToken()
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        trip(this) {
            shortSleep()
            clickAlternativeButton()
        }
        booking {
            mediumSleep()
        } result {
            fullASAPQuotesListCheck()
        }
    }

    /**
     * Given:   The driver's name and details are unavailable
     * When:    I am on the trip screen and status is DER
     * Then:    I can still see the trip details box
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun tripDetailsBoxVisibleWithoutDriverDetails() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER_NO_DRIVER_DETAILS)
        trip(this) {
            clickOnDriverDetails()
        } result {
            DERFullScreenCheck(
                    pickupText = TRIP_DER.origin?.displayAddress.orEmpty(),
                    destinationText = TRIP_DER.destination?.displayAddress.orEmpty()
                              )
            noDriverDetailsDERCheck()
        }
    }

    /**
     * Given:   The vehicle category, description are not available but number plate is
     * When:    I am on the trip screen and status is DER
     * Then:    I can see the trip details box
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun tripDetailsBoxVisibleWithoutVehicleDetails() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER_NO_VEHICLE_DETAILS)
        trip(this) {
            clickOnDriverDetails()
        } result {
            DERFullScreenCheck(
                    pickupText = TRIP_DER.origin?.displayAddress.orEmpty(),
                    destinationText = TRIP_DER.destination?.displayAddress.orEmpty()
                              )
            noVehicleDetailsDERCheck()
        }
    }

    /**
     * Given:   The driver's name, and vehicle details are unavailable
     * When:    I am on the trip screen and status is DER
     * Then:    I can see the trip details box
     **/
    @Test
    @AllowFlaky(attempts = 3)
    fun tripDetailsBoxVisibleWithoutVehicleAndDriverDetails() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER_NO_VEHICLE_AND_DRIVER_DETAILS)
        trip(this) {
            clickOnDriverDetails()
        } result {
            DERFullScreenCheck(
                    pickupText = TRIP_DER.origin?.displayAddress.orEmpty(),
                    destinationText = TRIP_DER.destination?.displayAddress.orEmpty()
                              )
            noVehicleAndDriverDetailsDERCheck()
        }
    }

    /**
     * Given:   The driver's name, vehicle details and number plate are unavailable
     * When:    I am on the trip screen and status is DER
     * Then:    I can see the trip details box
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun tripDetailsBoxVisibleWithoutVehicleDetailsNumberPlateAndDriverDetails() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER_NO_VEHICLE_NUMBER_PLATE_AND_DRIVER_DETAILS)
        trip(this) {
            clickOnDriverDetails()
        } result {
            DERFullScreenCheck(
                    pickupText = TRIP_DER.origin?.displayAddress.orEmpty(),
                    destinationText = TRIP_DER.destination?.displayAddress.orEmpty()
                              )
            noVehicleDetailsNumberPlateAndDriverDetailsDERCheck()
        }
    }

    /**
     * Given:   The number plate  unavailable
     * When:    I am on the trip screen and status is DER
     * Then:    I can see the trip details box
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun tripDetailsBoxVisibleWithoutNumberPlate() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER_NO_NUMBER_PLATE)
        trip(this) {
            clickOnDriverDetails()
        } result {
            DERFullScreenCheck(
                    pickupText = TRIP_DER.origin?.displayAddress.orEmpty(),
                    destinationText = TRIP_DER.destination?.displayAddress.orEmpty()
                              )
            noNumberPlateDERCheck()
        }
    }

    /**
     * Given:   I am on the trip screen
     * When:    The status of the ride changes to driver en route
     * Then:    I can see the driver en route notification at the top of the screen
     **/
    @Test
    fun DERNotificationIsVisible() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER)
        trip(this) {
        } result {
            notificationStringCheck(DER_NOTIFICATION)
        }
    }

    /**
     * Given:   I am on the trip screen
     * When:    The status of the ride changes to driver arrived
     * Then:    I can see the driver arrived notification at the top of the screen
     **/
    @Test
    fun arrivedNotificationIsVisible() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_ARRIVED,
                tracking = DRIVER_TRACKING,
                details = TRIP_ARRIVED)
        trip(this) {
        } result {
            notificationStringCheck(ARRIVED_NOTIFICATION)
        }
    }

    /**
     * Given:   I am on the trip screen
     * When:    The status of the ride changes to Passenger on board
     * Then:    I can see the POB notification at the top of the screen
     **/
    @Test
    fun POBNotificationIsVisible() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_POB,
                tracking = DRIVER_TRACKING,
                details = TRIP_POB)
        trip(this) {
        } result {
            notificationIntCheck(R.string.pass_on_board)
        }
    }

    private fun mockTripSuccessResponse(status: Any, tracking: Any, details: TripInfo) {
        serverRobot {
            successfulToken()
            bookingStatusResponse(code = HTTP_OK, response = status, trip = TRIP.tripId)
            driverTrackingResponse(code = HTTP_OK, response = tracking, trip = TRIP.tripId)
            bookingDetailsResponse(code = HTTP_OK, response = details, trip = TRIP.tripId)
        }
    }

    private fun mockTripFailedResponse(status: Any, tracking: Any, details: Any) {
        serverRobot {
            successfulToken()
            bookingStatusResponse(code = HTTP_INTERNAL_ERROR, response = status, trip = TRIP.tripId)
            driverTrackingResponse(code = HTTP_INTERNAL_ERROR, response = tracking, trip = TRIP.tripId)
            bookingDetailsResponse(code = HTTP_INTERNAL_ERROR, response = details, trip = TRIP.tripId)
        }
    }

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(INTENT)
    }

    companion object {
        private val INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TripActivity.Builder.EXTRA_TRIP, TRIP)
            })
        }
    }
}
