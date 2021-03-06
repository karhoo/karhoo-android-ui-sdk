package com.karhoo.uisdk.screen.trip.bookingstatus.contact

import android.content.Context
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.BookingFee
import com.karhoo.sdk.api.model.BookingFeePrice
import com.karhoo.sdk.api.model.Driver
import com.karhoo.sdk.api.model.FleetInfo
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.sdk.api.network.request.TripCancellation
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.UnitTestUISDKConfig
import com.karhoo.uisdk.analytics.Analytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ContactOptionsPresenterTest {

    private var context: Context = mock()
    private var cancelTripCall: Call<Void> = mock()
    private var view: ContactOptionsMVP.View = mock()
    private var tripsService: TripsService = mock()
    private var analytics: Analytics = mock()
    private val bookingFeeCall: Call<BookingFee> = mock()
    private val bookingFeeCaptor = argumentCaptor<(Resource<BookingFee>) -> Unit>()
    private val lambdaCaptor = argumentCaptor<(Resource<Void>) -> Unit>()

    @Captor
    private lateinit var cancellationCaptor: ArgumentCaptor<TripCancellation>

    companion object {
        private const val DRIVER_PHONE_NUMBER = "0123456789"
        private const val FLEET_PHONE_NUMBER = "0987654321"
        private const val TRIP_ID = "someTripId"
        private const val FOLLOW_CODE = "followCode"
    }

    private val bookingFee = BookingFeePrice(currency = "GBP", value = 5200)

    private var fleetInfo = FleetInfo(
            name = "Some mad oul fleet",
            fleetId = "123456",
            phoneNumber = FLEET_PHONE_NUMBER)

    private var vehicle = Vehicle(driver = Driver(phoneNumber = DRIVER_PHONE_NUMBER))

    private var tripDetails = TripInfo(
            tripId = TRIP_ID,
            followCode = FOLLOW_CODE,
            origin = TripLocationInfo(),
            destination = TripLocationInfo(),
            tripState = TripStatus.CONFIRMED,
            fleetInfo = fleetInfo,
            vehicle = vehicle)

    private var tripDetailsPassengerOnBoard = TripInfo(
            tripId = TRIP_ID,
            origin = TripLocationInfo(),
            destination = TripLocationInfo(),
            tripState = TripStatus.PASSENGER_ON_BOARD,
            fleetInfo = fleetInfo,
            vehicle = vehicle)

    private var emptyTripInfo = TripInfo(tripId = TRIP_ID)

    private lateinit var presenter: ContactOptionsPresenter

    @Before
    fun setUp() {
        UnitTestUISDKConfig.setKarhooAuthentication(context)

        whenever(tripsService.cancellationFee(any())).thenReturn(bookingFeeCall)
        doNothing().whenever(bookingFeeCall).execute(bookingFeeCaptor.capture())

        presenter = ContactOptionsPresenter(
                view = view,
                analytics = analytics,
                tripsService = tripsService)
        doNothing().whenever(cancelTripCall).execute(lambdaCaptor.capture())
    }

    /**
     * Given:   A user wishes to cancel a trip
     * When:    The cancel is successful
     * Then:    The view should be told to display the complete dialog
     */
    @Test
    fun `user requests cancels trip successfully`() {
        whenever(tripsService.cancel(any())).thenReturn(cancelTripCall)

        presenter.onTripInfoChanged(tripDetails)

        presenter.cancelTrip()
        lambdaCaptor.firstValue.invoke(Resource.Success(mock()))

        verify(view, atLeastOnce()).showTripCancelledDialog()
    }

    /**
     * Given:   A user wishes to cancel a trip
     * When:    The cancel has started
     * Then:    An event that the user pressed cancel should be sent
     */
    @Test
    fun `user presses cancel trip fires event`() {
        whenever(tripsService.cancel(any())).thenReturn(cancelTripCall)

        presenter.onTripInfoChanged(tripDetails)
        presenter.cancelTrip()
        lambdaCaptor.firstValue.invoke(Resource.Success(mock()))

        verify(analytics).userCancelTrip(tripDetails)
    }

    /**
     * Given:   A user wishes to cancel a trip
     * When:    The cancel has started
     * And:     It is a guest booking
     * Then:    The correct identifier is sent
     * And:     The cancel button is hidden
     */
    @Test
    fun `cancel trip uses correct identifier and hides cancel button`() {
        whenever(tripsService.cancel(any())).thenReturn(cancelTripCall)

        presenter.onTripInfoChanged(tripDetails)
        presenter.cancelTrip()
        lambdaCaptor.firstValue.invoke(Resource.Success(mock()))

        verify(tripsService).cancel(capture(cancellationCaptor))
        verify(view).enableCancelButton()
        assertEquals(TRIP_ID, cancellationCaptor.value.tripIdentifier)
    }

    /**
     * Given:   A user wishes to cancel a trip
     * When:    The cancel has started
     * And:     It is a guest booking
     * Then:    The correct identifier is sent
     */
    @Test
    fun `cancel guest booking trip uses correct identifier`() {
        UnitTestUISDKConfig.setGuestAuthentication(context)
        whenever(tripsService.cancel(any())).thenReturn(cancelTripCall)

        presenter.onTripInfoChanged(tripDetails)
        presenter.cancelTrip()
        lambdaCaptor.firstValue.invoke(Resource.Success(mock()))

        verify(tripsService).cancel(capture(cancellationCaptor))
        assertEquals(FOLLOW_CODE, cancellationCaptor.value.tripIdentifier)
    }

    /**
     * Given:   A user wishes to cancel a trip
     * When:    The cancel is unsuccessful
     * Then:    The view should be told to display the call dialog
     */
    @Test
    fun `user cancels trip unsuccessfully`() {
        whenever(tripsService.cancel(any())).thenReturn(cancelTripCall)

        presenter.onTripInfoChanged(tripDetails)

        presenter.cancelTrip()
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.Unexpected))

        verify(view, atLeastOnce()).showCallToCancelDialog(anyString(), anyString(), any())
    }

    /**
     * Given:   A user wishes to cancel a trip
     * When:    The cancel is successful by karhoo
     * Then:    The view should be told to display the complete dialog
     */
    @Test
    fun `user cancels trip successfully by karhoo`() {
        whenever(tripsService.cancel(any())).thenReturn(cancelTripCall)

        presenter.onTripInfoChanged(tripDetails)

        presenter.cancelTrip()
        lambdaCaptor.firstValue.invoke(Resource.Success(mock()))

        verify(view, atLeastOnce()).showTripCancelledDialog()
    }

    /**
     * Given:   A user wishes to cancel a trip
     * When:    The cancel is successful by dispatch
     * Then:    The view should be told to display the complete dialog
     */
    @Test
    fun `user cancels trip successfully by dispatch`() {
        whenever(tripsService.cancel(any())).thenReturn(cancelTripCall)

        presenter.onTripInfoChanged(tripDetails)

        presenter.cancelTrip()
        lambdaCaptor.firstValue.invoke(Resource.Success(mock()))

        verify(view, atLeastOnce()).showTripCancelledDialog()
    }

    /**
     * Given:   A trip with driver number available
     * When:    observing trip status
     * Then:    enable call driver & disable call fleet
     */
    @Test
    fun `driver number available enables call driver`() {
        presenter.onTripInfoChanged(tripDetails)
        verify(view).enableCallDriver()
        verify(view).disableCallFleet()
    }

    /**
     * Given:   A trip with driver number & fleet number available in passenger on board state
     * When:    observing trip status
     * Then:    enable call fleet & disable call driver
     * And:     disable cancel button
     */
    @Test
    fun `passenger on board enables call fleet and hides cancel button`() {
        presenter.onTripInfoChanged(tripDetailsPassengerOnBoard)

        verify(view).enableCallFleet()
        verify(view).disableCallDriver()
        verify(view).disableCancelButton()
    }

    /**
     * Given:   A user presses to call fleet
     * When:    The information is available
     * Then:    A call should be made to the fleet
     */
    @Test
    fun `call fleet makes a call request`() {
        presenter.onTripInfoChanged(tripDetails)
        presenter.contactFleet()
        verify(view).makeCall(FLEET_PHONE_NUMBER)
    }

    /**
     * Given:   A user presses to call driver
     * When:    The information is available
     * Then:    A call should be made to the driver
     */
    @Test
    fun `call driver makes a call request`() {
        presenter.onTripInfoChanged(tripDetails)
        presenter.contactDriver()
        verify(view).makeCall(DRIVER_PHONE_NUMBER)
    }

    /**
     * Given:   A user presses to call driver
     * When:    The information is available
     * Then:    An analytical event stating call driver is fired
     */
    @Test
    fun `call driver fires call driver event`() {
        presenter.onTripInfoChanged(tripDetails)
        presenter.contactDriver()
        verify(analytics).userCalledDriver(tripDetails)
    }

    /**
     * Given:   fleet info is null
     * When:    attempting to cancel trip, sdk callback onServiceError
     * Then:    show error
     */
    @Test
    fun `show error when attempting to cancel on service error trip and fleet info is null`() {
        whenever(tripsService.cancel(TripCancellation(TRIP_ID))).thenReturn(cancelTripCall)
        presenter.onTripInfoChanged(emptyTripInfo)

        presenter.cancelTrip()
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.Unexpected))

        verify(view).showError(anyInt(), any())
    }

    /**
     * Given:   A trip is in POB state
     * When:    The trip is validated
     * Then:    Then the contact options should be disabled
     */
    @Test
    fun `show call fleet option when trip is POB`() {
        presenter.onTripInfoChanged(tripDetailsPassengerOnBoard)

        verify(view).enableCallFleet()
        verify(view).disableCallDriver()
        verify(view).disableCancelButton()
    }

    /**
     * Given:   The cancellation fee is requested
     * When:    The call fails
     * Then:    Then view is updated with the error
     */
    @Test
    fun `a guest booking cancellation fee request failure uses the follow code`() {
        UnitTestUISDKConfig.setGuestAuthentication(context)
        presenter.onTripInfoChanged(tripDetails)

        presenter.cancelPressed()

        bookingFeeCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(tripsService).cancellationFee(FOLLOW_CODE)
        verify(view, atLeastOnce()).showCallToCancelDialog(anyString(), anyString(), any())
        verify(view).showLoadingDialog(false)
    }

    /**
     * Given:   The cancellation fee is requested
     * When:    The call fails
     * Then:    Then view is updated with the error
     */
    @Test
    fun `a cancellation fee request failure correctly updates the view`() {
        presenter.onTripInfoChanged(tripDetails)

        presenter.cancelPressed()

        bookingFeeCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(tripsService).cancellationFee(TRIP_ID)
        verify(view, atLeastOnce()).showCallToCancelDialog(anyString(), anyString(), any())
        verify(view).showLoadingDialog(false)
    }

    /**
     * Given:   The cancellation fee is requested
     * When:    There is a null cancellation fee
     * Then:    Then the information is returned to the user
     */
    @Test
    fun `a null cancellation fee response correctly updates the view`() {
        presenter.onTripInfoChanged(tripDetails)

        presenter.cancelPressed()

        bookingFeeCaptor.firstValue.invoke(Resource.Success(BookingFee(fee = null)))

        verify(view).showCancellationFee("", TRIP_ID)
    }

    /**
     * Given:   The cancellation fee is requested
     * When:    There there is no cancellation fee
     * Then:    Then the information is returned to the user
     */
    @Test
    fun `a no cancellation fee response correctly updates the view`() {
        presenter.onTripInfoChanged(tripDetails)

        presenter.cancelPressed()

        bookingFeeCaptor.firstValue.invoke(Resource.Success(BookingFee()))

        verify(view).showCancellationFee("", TRIP_ID)
    }

    /**
     * Given:   The cancellation fee is requested
     * When:    There is a valid cancellation fee
     * Then:    Then the information is returned to the user
     */
    @Test
    fun `a valid cancellation fee response correctly updates the view`() {
        presenter.onTripInfoChanged(tripDetails)

        presenter.cancelPressed()

        bookingFeeCaptor.firstValue.invoke(Resource.Success(BookingFee(fee = bookingFee)))

        verify(view).showCancellationFee("£52.00", TRIP_ID)
    }

    /**
     * Given:   A trip is in REQUESTED state
     * When:    The trip is validated
     * Then:    Then the contact driver option and cancel options should be enabled
     */
    @Test
    fun `show cancel option and call driver option when trip is requested`() {
        val requestedTrip = TripInfo(tripId = TRIP_ID,
                                     tripState = TripStatus.REQUESTED,
                                     fleetInfo = fleetInfo,
                                     vehicle = vehicle)

        presenter.onTripInfoChanged(requestedTrip)

        verify(view).disableCallFleet()
        verify(view).enableCallDriver()
        verify(view).enableCancelButton()
    }

    /**
     * Given:   A trip is in CONFIRMED state
     * When:    The trip is validated
     * Then:    Then the contact driver option and cancel options should be enabled
     */
    @Test
    fun `show cancel option and call driver option when trip is confirmed`() {
        val requestedTrip = TripInfo(tripId = TRIP_ID,
                                     tripState = TripStatus.CONFIRMED,
                                     fleetInfo = fleetInfo,
                                     vehicle = vehicle)

        presenter.onTripInfoChanged(requestedTrip)

        verify(view).disableCallFleet()
        verify(view).enableCallDriver()
        verify(view).enableCancelButton()
    }

    /**
     * Given:   A trip is in DER state
     * When:    The trip is validated
     * Then:    Then the contact driver option and cancel options should be enabled
     */
    @Test
    fun `show cancel and call driver options when driver is en route`() {
        val derTrip = TripInfo(tripId = TRIP_ID,
                               tripState = TripStatus.DRIVER_EN_ROUTE,
                               fleetInfo = fleetInfo)

        presenter.onTripInfoChanged(derTrip)

        verify(view).enableCallFleet()
        verify(view).disableCallDriver()
        verify(view).enableCancelButton()
    }

    /**
     * Given:   A trip is in ARRIVED state
     * When:    The trip is validated
     * Then:    Then the contact fleet option should be enabled
     * And:     The cancel option should be disabled
     */
    @Test
    fun `hide cancel option and show call fleet option when driver has arrived`() {
        val arrivedTrip = TripInfo(tripId = TRIP_ID,
                                   tripState = TripStatus.ARRIVED,
                                   fleetInfo = fleetInfo)

        presenter.onTripInfoChanged(arrivedTrip)

        verify(view).enableCallFleet()
        verify(view).disableCallDriver()
        verify(view).disableCancelButton()
    }

    /**
     * Given:   A trip is in COMPLETED state
     * When:    The trip is validated
     * Then:    Then the cancel and contact options should be disabled
     */
    @Test
    fun `hide cancel and contact option when trip is completed`() {
        val completedTrip = TripInfo(tripId = TRIP_ID,
                                   tripState = TripStatus.COMPLETED,
                                   fleetInfo = fleetInfo)

        presenter.onTripInfoChanged(completedTrip)

        verify(view).disableCallFleet()
        verify(view).disableCallDriver()
        verify(view).disableCancelButton()
    }
}