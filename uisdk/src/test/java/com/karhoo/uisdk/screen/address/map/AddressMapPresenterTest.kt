package com.karhoo.uisdk.screen.address.map

import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.address.AddressService
import com.karhoo.sdk.call.Call
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AddressMapPresenterTest {

    private val view: AddressMapMVP.View = mock()
    private val addressService: AddressService = mock()
    private val locationInfo: LocationInfo = mock()
    private val locationCall: Call<LocationInfo> = mock()

    lateinit var presenter: AddressMapPresenter

    private val lambdaCaptor = argumentCaptor<(Resource<LocationInfo>) -> Unit>()

    @Before
    fun setUp() {
        presenter = AddressMapPresenter(view = view, addressService = addressService)
        doNothing().whenever(locationCall).execute(lambdaCaptor.capture())
    }

    /**
     * Given:   The location info has been updated
     * When:    Passed into the locationUpdated method
     * Then:    The presenter should update the view with the display address
     */
    @Test
    fun `when location is updated the view displays the display address`() {
        whenever(locationInfo.displayAddress).thenReturn("karhoo street")

        presenter.locationUpdated(locationInfo)

        verify(view).updateDisplayAddress("karhoo street")
    }

    /**
     * Given:   The location info has been updated
     * When:    The passed location has no display address
     * Then:    An empty string is passed to the view
     */
    @Test
    fun `empty string shown when no display address is available`() {
        presenter.locationUpdated(null)

        verify(view).updateDisplayAddress("")
    }

    /**
     * Given:   No address has been set
     * When:    The user presses select
     * Then:    The button shouldnt react
     */
    @Test
    fun `no address set disables the select button`() {
        presenter.selectAddressPressed()

        verify(view, never()).setFinalAddress(any())
    }

    /**
     * Given:   An address has been set
     * When:    The user presses select
     * Then:    The view should be informed to set final address
     */
    @Test
    fun `set address updates the final selected address when select is pressed`() {
        whenever(addressService.reverseGeocode(any())).thenReturn(locationCall)
        presenter.getAddress(position = Position())
        lambdaCaptor.firstValue.invoke(Resource.Success(locationInfo))

        presenter.selectAddressPressed()

        verify(view).setFinalAddress(locationInfo)
    }

    /**
     * Given:   A request to get address is made
     * When:    The result is successful
     * Then:    The view should update the display address
     */
    @Test
    fun `get address is returns successful`() {
        whenever(addressService.reverseGeocode(any())).thenReturn(locationCall)
        whenever(locationInfo.displayAddress).thenReturn("Paddington")

        presenter.getAddress(position = Position())
        lambdaCaptor.firstValue.invoke(Resource.Success(locationInfo))

        verify(view).updateDisplayAddress("Paddington")
    }

    /**
     * Given:   The back arrow is pressed
     * When:    Handling the event
     * Then:    A call should be made to hide the view
     */
    @Test
    fun `hide the view when the back arrow is pressed`() {
        presenter.onBackArrowPressed()

        verify(view).hideMap()
    }
}