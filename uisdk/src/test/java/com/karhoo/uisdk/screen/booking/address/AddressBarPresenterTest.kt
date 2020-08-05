package com.karhoo.uisdk.screen.booking.address

import androidx.lifecycle.MutableLiveData
import com.karhoo.sdk.api.model.Address
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.address.AddressService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.address.AddressType
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarMVP
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarPresenter
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.address.JourneyInfo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AddressBarPresenterTest {

    var view: AddressBarMVP.View = mock()
    var analytics: Analytics = mock()
    var address: Address = mock()
    private var bookingStatusStateViewModel: BookingStatusStateViewModel = mock()
    private var bookingStatusMutable: MutableLiveData<BookingStatus> = mock()
    private var bookingStatus: BookingStatus = mock()
    private var mockedLocationOne: LocationInfo = mock()
    private var mockedLocationTwo: LocationInfo = mock()
    private var mockedTripInfo: TripInfo = mock()
    private var addressService: AddressService = mock()
    private var reverseGeoCall: Call<LocationInfo> = mock()
    private val reverseGeoCaptor = argumentCaptor<(Resource<LocationInfo>) -> Unit>()

    private val position = Position(1.0, 2.0)

    private lateinit var presenter: AddressBarPresenter

    @Before
    fun setUp() {
        presenter = AddressBarPresenter(
                view = view,
                analytics = analytics,
                addressService = addressService)
        presenter.subscribeToBookingStatus(bookingStatusStateViewModel)
    }

    /**
     * Given:    Pick up address has not been set
     * When:     Pick up address clicked
     * Then:     Address clicked with pickup type
     */
    @Test
    @Throws(Exception::class)
    fun `pick up address clicked shows address search with type pickup when no location set`() {
        presenter.pickUpAddressClicked()
        verify(bookingStatusStateViewModel, atLeastOnce()).process(AddressBarViewContract
                                                                           .AddressBarEvent
                                                                           .AddressClickedEvent(AddressType.PICKUP, null))
    }

    /**
     * Given:    Pick up address has not been set
     * When:     Pick up address clicked
     * Then:     Address clicked with pickup type
     */
    @Test
    @Throws(Exception::class)
    fun `pick up address clicked shows address search with type pickup from booking status location`() {
        val locationInfo = LocationInfo(position = position)
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatus.pickup).thenReturn(locationInfo)
        presenter.subscribeToBookingStatus(bookingStatusStateViewModel = bookingStatusStateViewModel)

        presenter.pickUpAddressClicked()

        verify(bookingStatusStateViewModel, atLeastOnce()).process(AddressBarViewContract
                                                                           .AddressBarEvent
                                                                           .AddressClickedEvent(AddressType.PICKUP, position))
    }

    /**
     * Given:   Destination address has not been set
     * When:    Destination address clicked
     * Then:    Address clicked with destination type
     */
    @Test
    @Throws(Exception::class)
    fun `drop off address clicked shows address search with type destination`() {
        presenter.dropOffAddressClicked()

        verify(analytics, atLeastOnce()).destinationPressed()
        verify(bookingStatusStateViewModel, atLeastOnce()).process(AddressBarViewContract
                                                                           .AddressBarEvent
                                                                           .AddressClickedEvent(AddressType.DESTINATION, null))
    }

    /**
     * Given:   Pickup address already set
     * When:    Destination address is set
     * Then:    Flip button is shown
     */
    @Test
    @Throws(Exception::class)
    fun `show flip button when both pickup and destination addresses are set`() {

        presenter.pickupSet(mockedLocationOne, 0)
        presenter.destinationSet(mockedLocationTwo, 0)

        verify(view).showFlipButton()
    }

    /**
     * Given:   Pickup address already set
     * When:    Destination address not set
     * Then:    Flip button is hidden
     */
    @Test
    @Throws(Exception::class)
    fun `hide flip button when there is no destination set`() {
        presenter.pickupSet(mockedLocationOne, 0)
        presenter.clearDestinationClicked()
        verify(view).hideFlipButton()
    }

    /**
     * Given:   Pickup address and destination address already set
     * When:    Flip addresses clicked
     * Then:    Pickup address and destination address should be flipped
     */
    @Test
    @Throws(Exception::class)
    fun `pickup and drop off addresses are flipped when flip button clicked`() {

        whenever(mockedLocationOne.displayAddress).thenReturn("address_one")
        whenever(mockedLocationTwo.displayAddress).thenReturn("address_two")

        presenter.pickupSet(mockedLocationOne, 0)
        presenter.destinationSet(mockedLocationTwo, 1)
        presenter.flipAddressesClicked()

        verify(view).setPickupAddress(mockedLocationOne.displayAddress)
        verify(view).setDropoffAddress(mockedLocationTwo.displayAddress)
    }

    /**
     * Given:   Pickup address and destination address not set
     * When:    Trip info with pickup and destination address is set
     * Then:    Both pickup and destination address should be set and flip button is shown
     */
    @Test
    @Throws(Exception::class)
    fun `setting both pickup and drop off using trip info updates pickup and destination and show the flip button`() {

        val origin = TripLocationInfo(displayAddress = "address_one")
        val destination = TripLocationInfo(displayAddress = "address_two")

        whenever(mockedTripInfo.origin).thenReturn(origin)
        whenever(mockedTripInfo.destination).thenReturn(destination)

        presenter.setBothPickupDropoff(mockedTripInfo)

        verify(view).setPickupAddress(origin.displayAddress)
        verify(view).setDropoffAddress(destination.displayAddress)
        verify(view).showFlipButton()
    }

    /**
     * Given:   Destination is already set
     * When:    The user clears the destination
     * Then:    Destination address should appear empty (Not set)
     */
    @Test
    fun `clear destination clicked clears the drop off address`() {
        presenter.clearDestinationClicked()
        verify(view, atLeastOnce()).setDropoffAddress("")
    }

    /**
     * Given:   Booking status not already set
     * When:    Booking status is set
     * Then:    Booking status should be updated, should not show loading pickup
     */
    @Test
    @Throws(Exception::class)
    fun `setting booking status with pickup and destination address updates booking status`() {

        whenever(mockedLocationOne.displayAddress).thenReturn("address_one")
        whenever(mockedLocationTwo.displayAddress).thenReturn("address_two")

        val origin = TripLocationInfo(mockedLocationOne.displayAddress)
        val destination = TripLocationInfo(mockedLocationTwo.displayAddress)

        whenever(mockedTripInfo.origin).thenReturn(origin)
        whenever(mockedTripInfo.destination).thenReturn(destination)

        val observer = presenter.subscribeToBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(mockedLocationOne, mockedLocationTwo, DateTime.now()))
        presenter.setBothPickupDropoff(mockedTripInfo)
        verify(view, atLeastOnce()).setPickupAddress(mockedLocationOne.displayAddress)
        verify(view, atLeastOnce()).setDropoffAddress(mockedLocationTwo.displayAddress)
        verify(view, never()).setDefaultPickupText()
    }

    /**
     * Given:   Booking status not already set
     * When:    Booking status is set with a destination but without a pickup address
     * Then:    Booking status is updated and should show loading pickup
     */
    @Test
    @Throws(Exception::class)
    fun `setting booking status without pickup address updates booking status and shows add pick`() {

        whenever(mockedLocationTwo.displayAddress).thenReturn("address_two")

        val destination = TripLocationInfo(mockedLocationTwo.displayAddress)

        whenever(mockedTripInfo.origin).thenReturn(null)
        whenever(mockedTripInfo.destination).thenReturn(destination)

        val observer = presenter.subscribeToBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(null, mockedLocationTwo, DateTime.now()))
        presenter.setBothPickupDropoff(mockedTripInfo)
        verify(view, atLeastOnce()).setDefaultPickupText()
        verify(view, atLeastOnce()).setDropoffAddress(mockedLocationTwo.displayAddress)
    }

    /**
     * Given    Booking status not already set
     * When:    Booking status is set with a origin but without a destination address
     * Then:    Booking status is updated and should clear destination view
     */
    @Test
    @Throws
    fun `setting booking status without destination address updates booking status and clears destination view`() {

        whenever(mockedLocationOne.displayAddress).thenReturn("address_one")

        val origin = TripLocationInfo(mockedLocationOne.displayAddress)

        whenever(mockedTripInfo.origin).thenReturn(origin)
        whenever(mockedTripInfo.destination).thenReturn(null)

        val observer = presenter.subscribeToBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(mockedLocationOne, null, DateTime.now()))
        presenter.setBothPickupDropoff(mockedTripInfo)
        verify(view, atLeastOnce()).setPickupAddress(mockedLocationOne.displayAddress)
        verify(view, atLeastOnce()).setDropoffAddress("")
        verify(view, atLeastOnce()).hideFlipButton()
    }

    /**
     * Given:   The user has selected a destination
     * When:    The destination is being set
     * Then:    The view should be asked to display the address
     */
    @Test
    fun `destination set updates the destination address shown`() {
        whenever(mockedLocationOne.displayAddress).thenReturn("address")
        presenter.destinationSet(mockedLocationOne, 0)
        verify(view, atLeastOnce()).setDropoffAddress(anyString())
    }

    /**
     * Given:   The user has selected a destination
     * When:    The destination is being set
     * Then:    The view should be asked to display the address
     */
    @Test
    fun `pickup set updates the pickup address shown`() {
        whenever(mockedLocationOne.displayAddress).thenReturn("address")
        presenter.pickupSet(mockedLocationOne, 0)
        verify(view, atLeastOnce()).setPickupAddress(anyString())
    }

    /**
     * Given:   There is a pickup and destination address set
     * When:    Flip addresses is invoked
     * Then:    Booking status should be invoked
     */
    @Test
    fun `flipping the addresses should update the booking status`() {

        whenever(mockedLocationOne.displayAddress).thenReturn("address_one")
        whenever(mockedLocationTwo.displayAddress).thenReturn("address_two")

        val observer = presenter.subscribeToBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(mockedLocationOne, mockedLocationTwo, DateTime.now()))

        presenter.flipAddressesClicked()

        verify(view, atLeastOnce()).setPickupAddress(anyString())
        verify(view, atLeastOnce()).setDropoffAddress(anyString())
    }

    /**
     * Given:   The user has selected a pickup
     * When:    The pickup is being set
     * Then:    An analytical event should be sent
     */
    @Test
    fun `pickup set sends an event to analytics`() {
        whenever(mockedLocationOne.displayAddress).thenReturn("address")
        presenter.pickupSet(mockedLocationOne, 0)
        verify(analytics).pickupAddressSelected(mockedLocationOne, 0)
    }

    /**
     * Given:   The user has selected a destination
     * When:    The destination is being set
     * Then:    An analytical event should be sent
     */
    @Test
    fun `destination set sends an event to analytics`() {
        whenever(mockedLocationOne.displayAddress).thenReturn("address")
        presenter.destinationSet(mockedLocationOne, 0)
        verify(analytics).destinationAddressSelected(mockedLocationOne, 0)
    }

    /**
     * Given:   The code wants to watch the booking status observable
     * When:    The call is made to get the observable
     * Then:    Then the bookingStatusObservable should be asked to get the observable
     */
    @Test
    fun `watching booking status returns the correct observable`() {
        Assert.assertNotNull(presenter.subscribeToBookingStatus(bookingStatusStateViewModel))
    }

    /**
     * Given:   The user injects a Journey Info Object
     * When:    The address picker presenter is injected with this
     * Then:    The correct calls should be made
     */
    @Test
    fun `full journey info object injected makes calls to the address service`() {
        whenever(addressService.reverseGeocode(any())).thenReturn(reverseGeoCall)
        doNothing().whenever(reverseGeoCall).execute(reverseGeoCaptor.capture())

        presenter.prefillForJourney(journeyInfo)

        reverseGeoCaptor.firstValue.invoke(Resource.Success(mockedLocationOne))

        verify(addressService).reverseGeocode(originPosition)
        verify(addressService).reverseGeocode(destinationPosition)
    }

    /**
     * Given:   The user injects a Journey Info Object
     * And:     There is no pickup location
     * When:    The address picker presenter is injected with this
     * Then:    The correct calls should be made
     */
    @Test
    fun `journey info object with no pickup injected makes one call to the address service`() {
        whenever(addressService.reverseGeocode(any())).thenReturn(reverseGeoCall)

        presenter.prefillForJourney(journeyInfoNoOrigin)

        verify(addressService, never()).reverseGeocode(originPosition)
        verify(addressService).reverseGeocode(destinationPosition)
    }

    /**
     * Given:   The user injects a Journey Info Object
     * When:    The journey info object has no destination
     * Then:    reverse geo should be called once
     */
    @Test
    fun `no destination journey info object injected makes calls to the address service once`() {
        whenever(addressService.reverseGeocode(any())).thenReturn(reverseGeoCall)

        presenter.prefillForJourney(journeyInfoNoDestination)

        verify(addressService, times(1)).reverseGeocode(any())
    }

    /**
     * Given:   The user injects a Journey Info Object
     * When:    The journey info object has no origin
     * Then:    reverse geo should be called once
     */
    @Test
    fun `no origin journey info object injected makes calls to the address service once`() {
        whenever(addressService.reverseGeocode(any())).thenReturn(reverseGeoCall)

        presenter.prefillForJourney(journeyInfoNoOrigin)

        verify(addressService, times(1)).reverseGeocode(any())
    }

    /**
     * Given:   The user injects a Journey Info Object
     * When:    The journey info object has no origin or destination
     * Then:    reverse geo should not be called
     */
    @Test
    fun `no origin or destination journey info object injected doesnt call reversegeo`() {
        presenter.prefillForJourney(journeyInfoNoOriginNoDestination)

        verify(addressService, never()).reverseGeocode(any())
    }

    /**
     * Given:   The user injects a Journey Info Object
     * When:    The address is returned
     * Then:    booking status should be updated
     */
    @Test
    fun `journey info object injected successfully updates booking info`() {
        val captor = argumentCaptor<(Resource<LocationInfo>) -> Unit>()

        whenever(mockedLocationOne.displayAddress).thenReturn(displayLocationOne)
        whenever(mockedLocationTwo.displayAddress).thenReturn(displayLocationTwo)

        whenever(addressService.reverseGeocode(any())).thenReturn(reverseGeoCall)
        doNothing().whenever(reverseGeoCall).execute(captor.capture())

        presenter.prefillForJourney(journeyInfo)

        captor.firstValue.invoke(Resource.Success(mockedLocationOne))
        captor.secondValue.invoke(Resource.Success(mockedLocationTwo))

        verify(view).setPickupAddress(displayLocationOne)
        verify(view).setDropoffAddress(displayLocationTwo)
        verify(view).showFlipButton()
    }

    /**
     * Given:   The user injects a Journey Info Object with no destination
     * When:    The address is returned
     * Then:    booking status should be updated
     */
    @Test
    fun `journey info object injected with no destination successfully updates booking info`() {
        val captor = argumentCaptor<(Resource<LocationInfo>) -> Unit>()

        whenever(mockedLocationOne.displayAddress).thenReturn(displayLocationOne)

        whenever(addressService.reverseGeocode(any())).thenReturn(reverseGeoCall)
        doNothing().whenever(reverseGeoCall).execute(captor.capture())

        presenter.prefillForJourney(journeyInfoNoDestination)

        captor.firstValue.invoke(Resource.Success(mockedLocationOne))

        verify(view).setPickupAddress(displayLocationOne)
        verify(view, never()).setDropoffAddress(any())
        verify(view, never()).showFlipButton()
    }

    /**
     * Given:   The user injects a Journey Info Object with no destination
     * When:    The address is returned
     * Then:    booking status should be updated
     */
    @Test
    fun `journey info object injected with no origin successfully updates booking info`() {
        val captor = argumentCaptor<(Resource<LocationInfo>) -> Unit>()

        whenever(mockedLocationTwo.displayAddress).thenReturn(displayLocationTwo)

        whenever(addressService.reverseGeocode(any())).thenReturn(reverseGeoCall)
        doNothing().whenever(reverseGeoCall).execute(captor.capture())

        presenter.prefillForJourney(journeyInfoNoOrigin)

        captor.firstValue.invoke(Resource.Success(mockedLocationTwo))

        verify(view, never()).setPickupAddress(any())
        verify(view).setDropoffAddress(displayLocationTwo)
        verify(view).showFlipButton()
    }

    /**
     * Given:   The user injects a Journey Info Object with no destination or origin
     * When:    The address is returned
     * Then:    booking status should not be updated
     */
    @Test
    fun `journey info object injected with no origin or destination doesn't update booking info`() {
        presenter.prefillForJourney(journeyInfoNoOriginNoDestination)

        verify(view, never()).setPickupAddress(any())
        verify(view, never()).setDropoffAddress(any())
        verify(view, never()).showFlipButton()
    }

    /**
     * Given:   The user injects a Journey Info Object with no destination or origin
     * When:    The date is checked but is an hour behind
     * Then:    booking status should not be updated
     */
    @Test
    fun `journey info object injected with invalid date doesn't update booking info`() {
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatus.date).thenReturn(journeyInfo.date)

        presenter.subscribeToBookingStatus(bookingStatusStateViewModel)
        presenter.prefillForJourney(journeyInfoNoOriginNoDestination)

        verify(view, never()).setPickupAddress(any())
        verify(view, never()).setDropoffAddress(any())
        verify(view, never()).showFlipButton()
        verify(view, never()).displayPrebookTime(any())
    }

    /**
     * Given:   The user injects a Journey Info Object with no destination or origin
     * When:    The date is checked but is 3 hours ahead
     * Then:    the date should be set on the view
     */
    @Test
    fun `journey info object injected with valid date updates date on view`() {
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatus.date).thenReturn(journeyInfoValidDate.date)

        presenter.subscribeToBookingStatus(bookingStatusStateViewModel)
        presenter.prefillForJourney(journeyInfoNoOriginNoDestination)

        verify(view, never()).setPickupAddress(any())
        verify(view, never()).setDropoffAddress(any())
        verify(view, never()).showFlipButton()
        verify(view).displayPrebookTime(any())
    }

    companion object {
        const val displayLocationOne = "LocationOne"
        const val displayLocationTwo = "LocationTwo"

        val originPosition = Position(latitude = 50.00, longitude = 2.00)
        val destinationPosition = Position(latitude = 30.00, longitude = 4.00)

        val journeyInfo = JourneyInfo(origin = originPosition, destination = destinationPosition, date = DateTime.now())
        val journeyInfoValidDate = journeyInfo.copy(date = DateTime.now().plusHours(3))
        val journeyInfoNoDestination = journeyInfo.copy(destination = null)
        val journeyInfoNoOrigin = journeyInfo.copy(origin = null)
        val journeyInfoNoOriginNoDestination = journeyInfo.copy(origin = null, destination = null)
    }
}