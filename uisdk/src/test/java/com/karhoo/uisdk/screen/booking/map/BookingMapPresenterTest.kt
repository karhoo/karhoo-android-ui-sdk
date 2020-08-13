package com.karhoo.uisdk.screen.booking.map

import androidx.lifecycle.LifecycleOwner
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BookingMapPresenterTest {

    internal lateinit var presenter: BookingMapPresenter

    internal var view: BookingMapMVP.View = mock()
    internal var pickupPresenter: BookingMapStategy.Presenter = mock()
    internal var pickupDropOffPresenter: BookingMapStategy.Presenter = mock()
    internal var bookingStatus: BookingStatus = mock()
    internal var analytics: Analytics? = mock()
    internal var lifeCycleOwner: LifecycleOwner = mock()
    internal var bookingStatusStateViewModel: BookingStatusStateViewModel = mock()

    @Before
    fun setUp() {
        presenter = BookingMapPresenter(
                view = view,
                analytics = analytics,
                pickupOnlyPresenter = pickupPresenter,
                pickupDropoffPresenter = pickupDropOffPresenter
                                       )
    }

    /**
     * Given:   The user has given their permission for location
     * When:    A call is made to locationPermissionGranted
     * Then:    The map should reset and the user should be located
     */
    @Test
    @Throws(Exception::class)
    fun `Location permission granted`() {
        presenter.locationPermissionGranted()

        verify(view, atLeastOnce()).resetMap()
        verify(view, atLeastOnce()).locateUser()
    }

    // TODO: These unfinished tests require a refactor of the BookingMapPresenter in relation to
    //  the tests commented out below.
//    /**
//     * Given: The user is loading the app
//     * When: The booking screen appears
//     * Then: Watch booking status
//     */
//    @Test
//    fun `Example Test One`() {
//        val observer = presenter.watchBookingStatus(lifeCycleOwner, bookingStatusStateViewModel)
//    }
//
//    /**
//     * Given: The user is loading the app
//     * When: The booking screen appears
//     * Then: Watch booking status
//     */
//    @Test
//    fun `Example Test One Part Two`() {
//        val observer = presenter.watchBookingStatus(lifeCycleOwner, bookingStatusStateViewModel)
//
//    }

    /**
     * Given: The user is on the booking screen
     * When: Location permission are denied
     * Then: Show snackbar informing of location permissions
     */
    @Test
    fun `Show Snackbar On Location Permissions Denied`() {

    }

    /**
     * Given: The user is on the booking screen
     * When: The locate me button is pressed
     * Then: Zoom to user location
     */
    @Test
    fun `Zoom to User Location`() {

    }

    /**
     * Given: The user is on the booking screen
     * When: A pickup destination is set
     * Then: Move the map to the marker
     */
    @Test
    fun `Focus map on pickup point`() {

    }

    /**
     * Given: The user is on the booking screen
     * When: The map is moved with no following movements
     * Then: The pickup pin should move and pickup destination should be set
     */
    @Test
    fun `Pickup Pin Moved And Pickup Location Set After Movement Stops`() {

    }

    /**
     * Given: The user is on the booking screen
     * When: An error occurs
     * Then: A snackbar is shown
     */
    @Test
    fun `Show snackbar on error`() {
        val errorMessage = 401
        presenter.onError(errorMessage)
        verify(view).showSnackbar(SnackbarConfig(text = "", stringId = errorMessage))
    }

}