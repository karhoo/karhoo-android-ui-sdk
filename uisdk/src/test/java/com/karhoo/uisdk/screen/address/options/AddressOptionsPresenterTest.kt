package com.karhoo.uisdk.screen.address.options

import android.location.Location
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.service.address.AddressService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationInfoListener
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AddressOptionsPresenterTest {

    private val view: AddressOptionsMVP.View = mock()
    private val addressService: AddressService = mock()
    private val locationInfo: LocationInfo = mock()
    private val position: Position = mock()
    private val locationCall: Call<LocationInfo> = mock()
    private val locationProvider: LocationProvider = mock()
    lateinit var presenter: AddressOptionsMVP.Presenter
    private val lambdaCaptor = argumentCaptor<LocationInfoListener>()
    private val location: Location = Location("").apply {
        latitude = 1.0
        longitude = 2.0
    }

    @Before
    fun setUp() {
        presenter = AddressOptionsPresenter(view = view, locationProvider = locationProvider)
    }

    /**
     * Given:   User selects current location option
     * When:    Location Provider succeeds
     * Then:    View should be called with updated location to forward to actions
     */
    @Test
    fun `get current location succeeds`() {
        doNothing().whenever(locationProvider).getAddress(lambdaCaptor.capture())
        whenever(locationInfo.position).thenReturn(position)

        presenter.getCurrentLocation()

        lambdaCaptor.firstValue.onLocationInfoReady(locationInfo)

        verify(view).didGetCurrentLocation(locationInfo)
    }

    /**
     * Given:   User selects current location option
     * When:    Location Provider fails
     * Then:    View should show a snack bar error
     */
    @Test
    fun `view updates when get current location fails`() {
        doNothing().whenever(locationProvider).getAddress(lambdaCaptor.capture())

        presenter.getCurrentLocation()

        lambdaCaptor.firstValue.onLocationInfoUnavailable("")

        verify(view).showSnackbar(any())

    }

    /**
     * Given:   User selects current location option
     * When:    Location Provider fails with resolveException
     * Then:    View should resolve exception
     */
    @Test
    fun `current location resolve exception error`() {
        doNothing().whenever(locationProvider).getAddress(lambdaCaptor.capture())

        presenter.getCurrentLocation()

        lambdaCaptor.firstValue.onResolutionRequired(mock())

        verify(view).resolveLocationApiException(any())

    }


}