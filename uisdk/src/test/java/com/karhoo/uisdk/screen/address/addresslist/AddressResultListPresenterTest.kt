package com.karhoo.uisdk.screen.address.addresslist

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Place
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.address.AddressService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.screen.address.domain.AddressSearchProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AddressResultListPresenterTest {

    private var view: AddressResultListMVP.View = mock()
    private var addressService: AddressService = mock()
    private var locationInfo: LocationInfo = mock()
    private var locationDetailsCall: Call<LocationInfo> = mock()
    private var addressProvider: AddressSearchProvider = mock()
    private var place: Place = Place(placeId = "12345678")

    @InjectMocks
    private lateinit var presenter: AddressResultListPresenter

    private val lambdaCaptor = argumentCaptor<(Resource<LocationInfo>) -> Unit>()

    @Before
    fun setUp() {
        doNothing().whenever(locationDetailsCall).execute(lambdaCaptor.capture())
    }

    /**
     * Given    A user has found there desired address
     * When     an address has been selected in the list
     * Then     A request for more details should be made and returned to the view
     */
    @Test
    fun `when an address is selected more details about the address are returned`() {
        whenever(addressService.locationInfo(any())).thenReturn(locationDetailsCall)
        whenever(addressProvider.getSessionToken()).thenReturn("a_session_token")

        presenter.onAddressSelected(place, 0)

        lambdaCaptor.firstValue.invoke(Resource.Success(locationInfo))
        verify(view, atLeastOnce()).setAddress(any(), eq(0))
    }

    /**
     * Given    A user has found there desired address
     * When     an address has been selected in the list
     * Then     The session token should not have changed.
     */
    @Test
    fun `when an address is selected the session token remains unchanged`() {

        whenever(addressService.locationInfo(any())).thenReturn(locationDetailsCall)
        whenever(addressProvider.getSessionToken()).thenReturn("a_session_token")

        presenter.onAddressSelected(place, 0)

        lambdaCaptor.firstValue.invoke(Resource.Success(locationInfo))

        verify(addressService).locationInfo(argThat {
            sessionToken == presenter.getSessionToken()
        })
    }

    /**
     * Given    A user has found their desired address
     * When     an address has been selected in the list
     * Then     A request for more details should be made and returned to the view
     */
    @Test
    fun `when an address is selected and an error occurs the callback returns error`() {
        whenever(addressService.locationInfo(any())).thenReturn(locationDetailsCall)
        whenever(addressProvider.getSessionToken()).thenReturn("a_session_token")

        presenter.onAddressSelected(place, 0)
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view, atLeastOnce()).showError(anyInt())
    }

    /**
     * Given:   The user requests desired address
     * When:    onServiceError from the sdk callback
     * Then:    The view should show an error
     */
    @Test
    fun `displays error when error occurs`() {
        whenever(addressService.locationInfo(any())).thenReturn(locationDetailsCall)
        whenever(addressProvider.getSessionToken()).thenReturn("a_session_token")

        presenter.onAddressSelected(place, 0)
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).showError(anyInt())
    }

}