package com.karhoo.uisdk.screen.address.domain

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.Places
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.network.request.PlaceSearch
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.address.AddressService
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.listener.ErrorView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired
import java.lang.ref.WeakReference
import java.util.UUID

class KarhooAddressProvider(private val analytics: Analytics?,
                            private val addressService: AddressService)
    : AddressSearchProvider {

    private var addresses: Addresses = Addresses(
            PlaceSearch(position = Position(0.0, 0.0), query = "", sessionToken = ""),
            Places())

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private var sessionToken: String = ""

    private var errorView: WeakReference<ErrorView>? = null

    private var addressesObservers = mutableSetOf<AddressSearchProvider.OnAddressesChangedListener?>()

    override fun addAddressesObserver(addressChangedListener: AddressSearchProvider.OnAddressesChangedListener) {
        addressesObservers.add(addressChangedListener)
        addressChangedListener.onAddressesChanged(addresses)
    }

    override fun getSessionToken(): String {
        if (sessionToken.isEmpty()) {
            sessionToken = UUID.randomUUID().toString()
        }
        return sessionToken
    }

    override fun setSearchQuery(searchQuery: String) {
        val placeSearch = PlaceSearch(
                position = Position(
                        latitude = latitude,
                        longitude = longitude
                                   ),
                query = searchQuery,
                sessionToken = getSessionToken())
        requestAddresses(placeSearch)
    }

    override fun setCurrentLatLong(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }

    override fun setErrorView(errorView: ErrorView) {
        this.errorView = WeakReference(errorView)
    }

    private fun requestAddresses(placeSearch: PlaceSearch) {
        if (placeSearch.query.length < 3) {
            updatePlaces(placeSearch, Places())
        } else {
            addressService.placeSearch(placeSearch).execute { result ->
                when (result) {
                    is Resource.Success -> updatePlaces(placeSearch, result.data)
                    is Resource.Failure -> onErrorGettingPlaces(placeSearch, result.error)
                }
            }
        }
    }

    private fun updatePlaces(placeSearch: PlaceSearch, places: Places) {
        addresses = Addresses(placeSearch, places)
        notifyObservers()
    }

    private fun onErrorGettingPlaces(placeSearch: PlaceSearch, karhooError: KarhooError) {
        when (karhooError) {
            KarhooError.CouldNotAutocompleteAddress -> {
                addresses = Addresses(placeSearch, Places())
                notifyObservers()
            }
            KarhooError.CouldNotGetAddress -> errorView?.get()?.showErrorDialog(returnErrorStringOrLogoutIfRequired(karhooError), karhooError)
            else -> errorView?.get()?.showSnackbar(SnackbarConfig(text = null, messageResId = returnErrorStringOrLogoutIfRequired(karhooError), karhooError = karhooError))
        }
    }

    private fun notifyObservers() {
        addressesObservers.map {
            it?.onAddressesChanged(addresses) ?: run {
                addressesObservers.remove(it)
            }
        }
    }

}
