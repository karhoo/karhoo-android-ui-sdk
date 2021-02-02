package com.karhoo.uisdk.screen.booking.tripallocation

import android.content.Context
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.FleetInfo
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.observable.Observable.Companion.BASE_POLL_TIME
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.request.TripCancellation
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.sdk.call.Call
import com.karhoo.sdk.call.PollCall
import com.karhoo.uisdk.UnitTestUISDKConfig
import com.karhoo.uisdk.screen.booking.booking.tripallocation.TripAllocationMVP
import com.karhoo.uisdk.screen.booking.booking.tripallocation.TripAllocationPresenter
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TripAllocationPresenterTest {

    private val tripsService: TripsService = mock()
    private val view: TripAllocationMVP.View = mock()
    private val tripDetailsCall: PollCall<TripInfo> = mock()
    private var cancelTripCall: Call<Void> = mock()
    private var context: Context = mock()
    private var observable: com.karhoo.sdk.api.network.observable.Observable<TripInfo> = mock()

    @InjectMocks
    private lateinit var presenter: TripAllocationPresenter
    private val tripRequested = TripInfo(
            tripId = TRIP_ID,
            fleetInfo = FleetInfo(name = FLEET_NAME, phoneNumber = PHONE_NUMBER),
            followCode = FOLLOW_CODE,
            tripState = TripStatus.REQUESTED)
    private val tripDriverEnRoute = TripInfo(
            tripId = TRIP_ID,
            tripState = TripStatus.DRIVER_EN_ROUTE)
    private val tripUserCancelled = TripInfo(
            tripId = TRIP_ID,
            tripState = TripStatus.CANCELLED_BY_USER)
    private val tripDispatchCancelled = TripInfo(
            tripId = TRIP_ID,
            fleetInfo = FleetInfo(name = FLEET_NAME),
            tripState = TripStatus.CANCELLED_BY_DISPATCH)

    private val lambdaCaptor = argumentCaptor<(Resource<Void>) -> Unit>()
    private val tripLambdaCaptor = argumentCaptor<Observer<Resource<TripInfo>>>()
    private val pollingTimeCaptor = argumentCaptor<Long>()

    @Before
    fun setUp() {
        UnitTestUISDKConfig.setKarhooAuthentication(context)
        whenever(tripsService.trackTrip(TRIP_ID)).thenReturn(tripDetailsCall)
        whenever(tripDetailsCall.observable()).thenReturn(observable)
        doNothing().whenever(observable).subscribe(tripLambdaCaptor.capture(), anyLong())
        doNothing().whenever(cancelTripCall).execute(lambdaCaptor.capture())
    }

    /**
     * Given:   A valid trip with id
     * When:    Trying to
     * Then:    the observers should be added to the observables
     */
    @Test
    fun `observable added to composite disposable container`() {
        presenter.waitForAllocation(tripRequested)
        verify(observable).subscribe(any(), pollingTimeCaptor.capture())

        assertEquals(BASE_POLL_TIME, pollingTimeCaptor.firstValue)
    }

    /**
     *  Given:  a requested trip
     *  When:   the trip state goes to allocated
     *  And:    it is a guest booking
     *  Then:   the trip info is updated with the follow code
     */
    @Test
    fun `trip info updated with follow code if guest booking`() {
        UnitTestUISDKConfig.setGuestAuthentication(context)
        whenever(tripsService.trackTrip(FOLLOW_CODE)).thenReturn(tripDetailsCall)
        presenter.waitForAllocation(tripRequested)
        tripLambdaCaptor.firstValue.onValueChanged(Resource.Success(tripDriverEnRoute))

        verify(observable).unsubscribe(any())
        verify(view).displayWebTracking(FOLLOW_CODE)
        assertEquals(FOLLOW_CODE, tripDriverEnRoute.followCode)
    }

    /**
     *  Given:  a requested trip
     *  When:   the trip state goes to allocated
     *  Then:   remove observer, dispose composite disposable, go to trip screen
     */
    @Test
    fun `go to trip screen when trip is allocated`() {
        whenever(tripsService.trackTrip(TRIP_ID)).thenReturn(tripDetailsCall)
        presenter.waitForAllocation(tripRequested)
        tripLambdaCaptor.firstValue.onValueChanged(Resource.Success(tripDriverEnRoute))

        verify(observable).unsubscribe(any())
        verify(view).goToTrip(tripDriverEnRoute)
        assertNull(tripDriverEnRoute.followCode)
    }

    /**
     *  Given:  a requested trip
     *  When:   the trip state goes to a state beyond allocated
     *  Then:   remove observer from observable, go to trip screen
     */
    @Test
    fun `go to trip screen when trip is beyond allocated`() {
        whenever(tripsService.trackTrip(TRIP_ID)).thenReturn(tripDetailsCall)

        presenter.waitForAllocation(tripRequested)
        tripLambdaCaptor.firstValue.onValueChanged(Resource.Success(tripDriverEnRoute))

        verify(observable).unsubscribe(any())
        verify(view).goToTrip(tripDriverEnRoute)
    }

    /**
     *  Given:  a requested trip for a guest booking
     *  When:   the trip state goes to a state beyond allocated
     *  And:    there is a follow code
     *  Then:   remove observer from observable, go to web tracking screen
     */
    @Test
    fun `go to web tracking screen when guest booking trip with follow code is beyond allocated`() {
        UnitTestUISDKConfig.setGuestAuthentication(context)

        whenever(tripsService.trackTrip(FOLLOW_CODE)).thenReturn(tripDetailsCall)

        presenter.waitForAllocation(tripRequested)
        tripLambdaCaptor.firstValue.onValueChanged(Resource.Success(tripDriverEnRoute))

        verify(observable).unsubscribe(any())
        verify(view).displayWebTracking(FOLLOW_CODE)
    }

    /**
     *  Given:  a requested trip
     *  When:   the trip state goes to dispatch cancelled
     *  Then:   remove observer, dispose composite disposable, display trip cancelled to view
     */
    @Test
    fun `display booking failed when trip goes to dispatch cancelled`() {
        whenever(tripsService.trackTrip(TRIP_ID)).thenReturn(tripDetailsCall)

        presenter.waitForAllocation(tripRequested)
        tripLambdaCaptor.firstValue.onValueChanged(Resource.Success(tripDispatchCancelled))

        verify(observable).unsubscribe(any())
        verify(view).displayBookingFailed(FLEET_NAME)
    }

    /**
     *  Given:  a requested trip
     *  When:   the trip state goes to dispatch cancelled
     *  And:    it is a guest booking
     *  Then:   remove observer, dispose composite disposable, display trip cancelled to view
     */
    @Test
    fun `display guest booking failed when trip goes to dispatch cancelled`() {
        UnitTestUISDKConfig.setGuestAuthentication(context)

        whenever(tripsService.trackTrip(FOLLOW_CODE)).thenReturn(tripDetailsCall)

        presenter.waitForAllocation(tripRequested)
        tripLambdaCaptor.firstValue.onValueChanged(Resource.Success(tripDispatchCancelled))

        verify(observable).unsubscribe(any())
        verify(view).displayBookingFailed(FLEET_NAME)
    }

    /**
     *  Given:  a requested trip
     *  When:   the trip state goes to user cancelled
     *  Then:   remove observer, dispose composite disposable, display trip cancelled by user to view
     */
    @Test
    fun `display trip cancelled when trip goes to user cancelled`() {
        whenever(tripsService.trackTrip(TRIP_ID)).thenReturn(tripDetailsCall)

        presenter.waitForAllocation(tripRequested)
        tripLambdaCaptor.firstValue.onValueChanged(Resource.Success(tripUserCancelled))

        verify(observable).unsubscribe(any())
        verify(view).displayTripCancelledSuccess()
    }

    /**
     *  Given:  a requested trip
     *  When:   the trip state goes to user cancelled
     *  And:    it is a guest booking
     *  Then:   remove observer, dispose composite disposable, display trip cancelled by user to view
     */
    @Test
    fun `display trip cancelled when guest booking trip goes to user cancelled`() {
        UnitTestUISDKConfig.setGuestAuthentication(context)

        whenever(tripsService.trackTrip(FOLLOW_CODE)).thenReturn(tripDetailsCall)

        presenter.waitForAllocation(tripRequested)
        tripLambdaCaptor.firstValue.onValueChanged(Resource.Success(tripUserCancelled))

        verify(observable).unsubscribe(any())
        verify(view).displayTripCancelledSuccess()
    }

    /**
     * Given:   waiting for allocation on a requested trip
     * When:    cancelling trip fails
     * Then:    show call to cancel dialog
     */
    @Test
    fun `cancelling trip fails then show call to cancel dialog`() {
        presenter.waitForAllocation(tripRequested)
        whenever(tripsService.cancel(TripCancellation(TRIP_ID))).thenReturn(cancelTripCall)

        presenter.cancelTrip()
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.Unexpected))

        verify(view).showCallToCancelDialog(PHONE_NUMBER, FLEET_NAME, KarhooError.Unexpected)
    }

    /**
     * Given:   there is an allocation delay
     * When:    it is a karhoo user booking
     * And:     there has been no booking cancellation or completion
     * Then:    then no alert is displayed
     */
    @Test
    fun `delayed allocation alert shown for delayed allocation for karhoo user`() {
        presenter.waitForAllocation(tripRequested)

        presenter.handleAllocationDelay(tripRequested)

        verify(view).showAllocationDelayAlert(tripRequested)
    }

    /**
     * Given:   there is an allocation delay
     * When:    it is a karhoo user booking
     * And:     the booking has been cancelled by the user
     * Then:    then no alert is displayed
     */
    @Test
    fun `no alert shown for delayed allocation for karhoo user after user cancellation`() {
        whenever(tripsService.trackTrip(TRIP_ID)).thenReturn(tripDetailsCall)

        presenter.waitForAllocation(tripRequested)
        tripLambdaCaptor.firstValue.onValueChanged(Resource.Success(tripUserCancelled))

        presenter.handleAllocationDelay(tripRequested)

        verify(view, never()).showAllocationDelayAlert(tripRequested)
    }

    /**
     * Given:   there is an allocation delay
     * When:    it is a karhoo user booking
     * And:     the booking has been cancelled by karhoo
     * Then:    then no alert is displayed
     */
    @Test
    fun `no alert shown for delayed allocation for karhoo user after karhoo cancellation`() {
        whenever(tripsService.trackTrip(TRIP_ID)).thenReturn(tripDetailsCall)

        presenter.waitForAllocation(tripRequested)
        tripLambdaCaptor.firstValue.onValueChanged(Resource.Success(tripDispatchCancelled))

        presenter.handleAllocationDelay(tripRequested)

        verify(view, never()).showAllocationDelayAlert(tripRequested)
    }

    /**
     * Given:   there is an allocation delay
     * When:    it is a karhoo user booking
     * And:     a driver has been allocated
     * Then:    then no alert is displayed
     */
    @Test
    fun `no alert shown for delayed allocation for karhoo user after driver allocation`() {
        whenever(tripsService.trackTrip(TRIP_ID)).thenReturn(tripDetailsCall)

        presenter.waitForAllocation(tripRequested)
        tripLambdaCaptor.firstValue.onValueChanged(Resource.Success(tripDriverEnRoute))

        presenter.handleAllocationDelay(tripRequested)

        verify(view, never()).showAllocationDelayAlert(tripRequested)
    }

    /**
     * Given:   there is an allocation delay
     * When:    it is a token auth booking
     * And:     there has been no booking cancellation or completion
     * Then:    then no alert is displayed
     */
    @Test
    fun `delayed allocation alert shown for delayed allocation for token auth user`() {
        UnitTestUISDKConfig.setTokenAuthentication(context)
        presenter.waitForAllocation(tripRequested)

        presenter.handleAllocationDelay(tripRequested)

        verify(view).showAllocationDelayAlert(tripRequested)
    }

    /**
     * Given:   there is an allocation delay
     * When:    it is a guest booking
     * Then:    then no alert is displayed
     */
    @Test
    fun `no alert shown for delayed allocation for guest booking`() {
        UnitTestUISDKConfig.setGuestAuthentication(context)
        whenever(tripsService.trackTrip(FOLLOW_CODE)).thenReturn(tripDetailsCall)
        presenter.waitForAllocation(tripRequested)

        presenter.handleAllocationDelay(tripRequested)

        verify(view, never()).showAllocationDelayAlert(tripRequested)
    }

    companion object {
        private const val TRIP_ID = "12trip34id"
        private const val FOLLOW_CODE = "followid"
        private const val FLEET_NAME = "Keane's Magic Motahs"
        private const val PHONE_NUMBER = "+44123 567891"
    }
}