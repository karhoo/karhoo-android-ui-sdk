package com.karhoo.uisdk.screen.trip.bookingstatus.tripinfo

import com.karhoo.sdk.api.model.Driver
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.uisdk.screen.trip.bookingstatus.BookingStatusMVP
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TripInfoPresenterTest {

    private var view: TripInfoMVP.View = mock()
    private var observerPresenter: BookingStatusMVP.Presenter = mock()

    private val PHOTO_URL = "http://photorepo.com/id123.png"

    private val DRIVER: Driver = Driver(
            firstName = "Jeff",
            lastName = "Smith",
            licenceNumber = "OK987",
            photoUrl = PHOTO_URL)

    private val DRIVER_MISSING_DETAILS: Driver = Driver(
            firstName = "",
            lastName = "",
            licenceNumber = "",
            photoUrl = "")

    private val VEHICLE: Vehicle = Vehicle(
            driver = DRIVER,
            vehicleClass = "CLASS",
            vehicleLicencePlate = "K012 232",
            description = "A very nice car")

    private val VEHICLE_MISSING_DRIVER_DETAILS: Vehicle = Vehicle(
            driver = DRIVER_MISSING_DETAILS,
            vehicleClass = "CLASS",
            vehicleLicencePlate = "K012 232",
            description = "A very nice car")

    private val VEHICLE_MISSING_DESCRIPTION_DETAILS: Vehicle = Vehicle(
            driver = DRIVER_MISSING_DETAILS,
            vehicleClass = "CLASS",
            vehicleLicencePlate = "K012 232")

    private val VEHICLE_MISSING_LICENCE_PLATE: Vehicle = Vehicle(
            driver = DRIVER_MISSING_DETAILS,
            vehicleClass = "CLASS",
            description = "A very nice car")

    private var TRIP_DETAILS: TripInfo = TripInfo(
            tripId = "tripId",
            vehicle = VEHICLE,
            tripState = TripStatus.DRIVER_EN_ROUTE)

    private var TRIP_DETAILS_POB: TripInfo = TripInfo(
            tripId = "tripId",
            vehicle = VEHICLE,
            tripState = TripStatus.PASSENGER_ON_BOARD)

    private var TRIP_DETAILS_COMPLETE: TripInfo = TripInfo(
            tripId = "tripId",
            vehicle = VEHICLE,
            tripState = TripStatus.COMPLETED)

    private var TRIP_DETAILS_MISSING_DRIVER_DETAILS: TripInfo = TripInfo(
            tripId = "tripId",
            vehicle = VEHICLE_MISSING_DRIVER_DETAILS,
            tripState = TripStatus.DRIVER_EN_ROUTE)

    private var TRIP_DETAILS_MISSING_VEHICLE_DETAILS: TripInfo = TripInfo(
            tripId = "tripId",
            vehicle = VEHICLE_MISSING_DESCRIPTION_DETAILS,
            tripState = TripStatus.DRIVER_EN_ROUTE)

    private var TRIP_DETAILS_MISSING_LICENCE_PLATE: TripInfo = TripInfo(
            tripId = "tripId",
            vehicle = VEHICLE_MISSING_LICENCE_PLATE,
            tripState = TripStatus.DRIVER_EN_ROUTE)

    @InjectMocks
    private lateinit var presenter: TripInfoPresenter

    /**
     * Given:   A observable is set
     * When:    Valid data becomes available
     * Then:    An update to the view to show the values should be made
     */
    @Test
    fun `setting observable with valid data updates view`() {
        addObserverWithPayload(TRIP_DETAILS)

        verify(view).showTripInfo()
        verify(view).bindViews(DRIVER.firstName, VEHICLE.description, VEHICLE.vehicleLicencePlate,
                               DRIVER.licenceNumber, PHOTO_URL)
    }

    /**
     * Given:   A user presses ride options
     * Then:    A call should me made to show ride options
     */
    @Test
    fun `valid trip makes call to show driver`() {
        addObserverWithPayload(TRIP_DETAILS)

        verify(view).showTripInfo()
    }

    /**
     * Given:   A user is waiting for a trip
     * When:    The driver is en route
     * Then:    The details should be enabled
     */
    @Test
    fun `details button visible when the ride is en route`() {
        addObserverWithPayload(TRIP_DETAILS)

        verify(view).showDetailsOptions()
    }

    /**
     * Given:   A user is en route
     * When:    The state is PASSENGER ON BOARD
     * Then:    The details should be enabled
     */
    @Test
    fun `details button visible when the ride is passenger on board`() {
        addObserverWithPayload(TRIP_DETAILS_POB)

        verify(view).showDetailsOptions()
    }

    /**
     * Given:   A user is en route
     * When:    The state is PASSENGER ON BOARD
     * Then:    The details should be disabled
     */
    @Test
    fun `details button invisible when the ride is complete`() {
        addObserverWithPayload(TRIP_DETAILS_COMPLETE)
        verify(view).hideDetailsOptions()
    }

    /**
     * Given:   A user is waiting for a trip
     * When:    The driver is en route AND driver details is missing
     * Then:    The details should be enabled
     */
    @Test
    fun `trip info visible when the driver description is missing`() {
        addObserverWithPayload(TRIP_DETAILS_MISSING_DRIVER_DETAILS)
        verify(view).showDriverDetails()
        verify(view).showTripInfo()
    }

    /**
     * Given:   A user is waiting for a trip
     * When:    The driver is en route AND vehicle description is missing
     * Then:    The details should be enabled
     */
    @Test
    fun `trip info NOT visible when the vehicle details is missing`() {
        addObserverWithPayload(TRIP_DETAILS_MISSING_VEHICLE_DETAILS)
        verify(view).showDriverDetails()
        verify(view).showTripInfo()
    }

    /**
     * Given:   A user is waiting for a trip
     * When:    The driver is en route AND vehicle licence plate is missing
     * Then:    The details should be enabled
     */
    @Test
    fun `trip info NOT visible when the vehicle licence plate is missing`() {
        addObserverWithPayload(TRIP_DETAILS_MISSING_LICENCE_PLATE)
        verify(view).showDriverDetails()
        verify(view).showTripInfo()
    }

    private fun addObserverWithPayload(tripInfo: TripInfo) {
        whenever(observerPresenter.addTripInfoObserver(any())).then {
            (it.arguments[0] as TripInfoPresenter).onTripInfoChanged(tripInfo)
        }
        presenter.observeTripStatus(observerPresenter)
    }

}
