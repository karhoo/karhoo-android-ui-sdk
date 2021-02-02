package com.karhoo.uisdk.screen.booking.map

import com.google.android.gms.maps.model.LatLng
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.address.AddressService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner
import kotlin.jvm.Throws

@RunWith(MockitoJUnitRunner::class)
class PickupOnlyPresenterTest {

    internal var addressService: AddressService = mock()
    internal var owner: BookingMapStategy.Owner = mock()
    internal var bookingStatusStateViewModel: BookingStatusStateViewModel = mock()
    internal var locationDetails: LocationInfo = mock()
    internal var locationDetailsCall: Call<LocationInfo> = mock()

    private val karhooError = KarhooError.Unexpected

    private val lambdaCaptor = argumentCaptor<(Resource<LocationInfo>) -> Unit>()

    @InjectMocks
    internal lateinit var presenter: PickupOnlyPresenter

    @Before
    fun setUp() {
        doNothing().whenever(locationDetailsCall).execute(lambdaCaptor.capture())
    }

    /**
     * Given:   The map has stopped moving
     * When:    A call is made to reverse geo
     * Then:    The call should return the location details of the stopped point
     */
    @Test
    @Throws(Exception::class)
    fun `map finished moving calls reverse geo`() {
        whenever(addressService.reverseGeocode(any()))
                .thenReturn(locationDetailsCall)

        val latLng = LatLng(1.0, 1.0)
        presenter.setOwner(owner)
        presenter.mapMoved(latLng)
        lambdaCaptor.firstValue.invoke(Resource.Success(locationDetails))

        verify(owner, atLeastOnce()).setPickupLocation(locationDetails)
    }

    /**
     * Given:   The map has stopped moving with invalid lat long
     * When:    A call is made to reverse geo
     * Then:    The call should return the location details of the stopped point
     */
    @Test
    @Throws(Exception::class)
    fun `map finished moving calls reverse geo with error`() {
        whenever(addressService.reverseGeocode(any()))
                .thenReturn(locationDetailsCall)

        val latLng = LatLng(1.0, 1.0)
        presenter.setOwner(owner)
        presenter.mapMoved(latLng)
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(owner, never()).setPickupLocation(locationDetails)
    }

    /**
     * Given:   The user has dragged the map
     * When:    The owner interface isnt null
     * Then:    The pickup location should be set to null
     */
    @Test
    @Throws(Exception::class)
    fun `map dragged sets pickup to null`() {
        presenter.setOwner(owner)
        presenter.mapDragged()
        verify(owner, atLeastOnce()).setPickupLocation(null)
    }

    /**
     * Given:   The user presses locate me
     * When:    the event is passed to the presenter
     * Then:    The presenter should ask the owner to zoom to that latlng
     */
    @Test
    @Throws(Exception::class)
    fun `locate me zooms the map to users latlng`() {
        presenter.setOwner(owner)
        presenter.locateUserPressed()
        verify(owner, atLeastOnce()).locateAndUpdate()
    }

    /**
     * Given:   the user moves the map
     * When:    onServiceError from the sdk callback after mapMoved
     * Then:    pass error through to owner
     */
    @Test
    fun `calls owner on error on service error`() {
        val position = Position(1.0, 1.0)
        val googleLatLng = LatLng(1.0, 1.0)

        whenever(addressService.reverseGeocode(position)).thenReturn(locationDetailsCall)

        presenter.setOwner(owner)
        presenter.mapMoved(googleLatLng)
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(owner).onError(anyInt(), any())
    }

    /**
     * Given:   Map not yet started
     * When:    mapMoved and location is still latlong(0,0) (invalid)
     * Then:    do nothing
     */
    @Test
    fun `empty lat long doesnt do reverse geolocate`() {
        val googleLatLng = LatLng(0.0, 0.0)

        presenter.setOwner(owner)
        presenter.mapMoved(googleLatLng)

        verify(owner, never()).onError(anyInt(), any())
        verify(owner, never()).locateAndUpdate()
    }

}