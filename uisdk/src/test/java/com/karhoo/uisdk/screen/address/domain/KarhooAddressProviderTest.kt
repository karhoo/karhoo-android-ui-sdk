package com.karhoo.uisdk.screen.address.domain

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.Place
import com.karhoo.sdk.api.model.Places
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.network.request.PlaceSearch
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.address.AddressService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.R
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.listener.ErrorView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class KarhooAddressProviderTest {

    private var addressService: AddressService = mock()
    private var analytics: Analytics = mock()
    private var errorView: ErrorView = mock()
    private var placesCall: Call<Places> = mock()

    @InjectMocks
    lateinit var searchProvider: KarhooAddressProvider

    private val lambdaCaptor = argumentCaptor<(Resource<Places>) -> Unit>()

    @Before
    fun setUp() {
        doNothing().whenever(placesCall).execute(lambdaCaptor.capture())
    }

    /**
     * Given    a search query of two characters is set
     * When     An address is to be looked up
     * Then     A call to the Karhoo address service should be made
     */
    @Test
    fun `requesting an address less than three characters calls the karhoo api for lookup`() {
        searchProvider.setSearchQuery("ya")

        verify(addressService, never()).placeSearch(any())
    }

    /**
     * Given    a search query of three characters is set
     * When     An address is to be looked up
     * Then     A call to the Karhoo address service should be made
     */
    @Test
    fun `requesting an address calls the karhoo api for lookup`() {
        whenever(addressService.placeSearch(any())).thenReturn(placesCall)

        searchProvider.setSearchQuery("yas")
        lambdaCaptor.firstValue.invoke(Resource.Success(places))

        verify(addressService, atLeastOnce()).placeSearch(any())
    }

    /**
     * Given    a search query of three characters is set
     * When     An address is to be looked up
     * Then     A call to fire an analytic event should be made
     */
    @Test
    fun `requesting an address fires analytical event`() {
        whenever(addressService.placeSearch(any())).thenReturn(placesCall)
        searchProvider.setSearchQuery("som")
        lambdaCaptor.firstValue.invoke(Resource.Success(places))

        verify(analytics, atLeastOnce()).amountAddressesShown(2)
    }

    /**
     * Given:   ErrorView is set
     * When:    setSearchQuery and sdk returns onServiceError
     * Then:    showTemporaryError from errorView
     */
    @Test
    fun `show temporary error when on service error`() {
        searchProvider.setErrorView(errorView)
        whenever(addressService.placeSearch(any())).thenReturn(placesCall)

        searchProvider.setSearchQuery("Some Search Query")
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(errorView).showSnackbar(eq(SnackbarConfig(text = null, messageResId = R.string.K0001)))
    }

    /**
     * Given:   ErrorView is set
     * When:    setSearchQuery and sdk returns CouldNotAutocompleteAddress
     * Then:    return empty list of results
     */
    @Test
    fun `return empty list of results when could not complete error`() {
        val nonsenseQuery = "A nonsense string that won't return any results"
        whenever(addressService.placeSearch(any())).thenReturn(placesCall)

        searchProvider.setSearchQuery(nonsenseQuery)
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.CouldNotAutocompleteAddress))

        var expectedAddresses: Addresses? = null
        searchProvider.addAddressesObserver(addressChangedListener = object : AddressSearchProvider.OnAddressesChangedListener {
            override fun onAddressesChanged(addresses: Addresses?) {
                expectedAddresses = addresses
            }
        })
        assertEquals(Addresses(PlaceSearch(
                position = Position(0.0, 0.0),
                query = nonsenseQuery, sessionToken = expectedAddresses!!.query.sessionToken), Places()),
                     expectedAddresses)
    }

    /**
     * Given:   Get session toke n from address provider is invoked
     * When:    Get session token is invoked
     * Then:    Get session token from provider must return a non empty string
     */
    @Test
    fun `get session token from address provider must always be non-empty`() {
        val sessionToken = searchProvider.getSessionToken()
        assertNotEquals(sessionToken, "")
    }

    /**
     * Given:   ErrorView is set
     * When:    setSearchQuery and sdk returns CouldNotGetAddress
     * Then:    show error dialog
     */
    @Test
    fun `show dialog error when CouldNotGetAddress error`() {
        searchProvider.setErrorView(errorView)
        whenever(addressService.placeSearch(any())).thenReturn(placesCall)

        searchProvider.setSearchQuery("Some Search Query")
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.CouldNotGetAddress))

        verify(errorView).showErrorDialog(R.string.K2001)
    }

    companion object {
        private val place = Place()
        private val placeList = mutableListOf(place, place)
        private val places = Places(placeList)
    }


}