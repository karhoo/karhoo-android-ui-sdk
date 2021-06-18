package com.karhoo.uisdk.screen.address.map

import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.model.LatLng
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.address.AddressService
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationInfoListener
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationProvider
import com.karhoo.uisdk.util.extension.orZero

class AddressMapPresenter(view: AddressMapMVP.View,
                          private val addressService: AddressService = KarhooApi.addressService,
                            private val locationProvider: LocationProvider)
    : BasePresenter<AddressMapMVP.View>(), AddressMapMVP.Presenter {

    private var lastLocationInfo: LocationInfo? = null

    init {
        attachView(view)
    }

    override fun locationUpdated(locationInfo: LocationInfo?) {
        view?.updateDisplayAddress(locationInfo?.displayAddress.orEmpty())
    }

    override fun selectAddressPressed() {
        view?.setFinalAddress(lastLocationInfo)
    }

    override fun getAddress(position: Position) {
        addressService.reverseGeocode(position).execute { result ->
            when (result) {
                is Resource.Success -> {
                    updateLocation(result.data)
                }
            }
        }
    }

    private fun updateLocation(locationInfo: LocationInfo) {
        lastLocationInfo = locationInfo
        view?.updateDisplayAddress(locationInfo.displayAddress)
    }

    override fun getLastLocation() {
        locationProvider.getAddress(object : LocationInfoListener {
            override fun onLocationInfoReady(locationInfo: LocationInfo) {
                updateLocation(locationInfo)
                view?.zoom(LatLng(locationInfo.position?.latitude.orZero(), locationInfo.position?.longitude.orZero()))
            }

            override fun onLocationServicesDisabled() {
                view?.showLocationDisabledSnackbar()
            }

            override fun onLocationInfoUnavailable(errorMessage: String, karhooError: KarhooError?) {
                view?.showSnackbar(SnackbarConfig(text = errorMessage, karhooError = karhooError))
            }

            override fun onResolutionRequired(resolvableApiException: ResolvableApiException) {
                // not used
            }
        })
    }

    override fun onBackArrowPressed() {
        view?.hideMap()
    }
}
