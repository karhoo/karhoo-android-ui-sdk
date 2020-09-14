package com.karhoo.uisdk.screen.address.options

import android.location.Location
import com.google.android.gms.common.api.ResolvableApiException
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationInfoListener
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationProvider

class AddressOptionsPresenter(view: AddressOptionsMVP.View,
                              private val locationProvider: LocationProvider)
    : BasePresenter<AddressOptionsMVP.View>(), AddressOptionsMVP.Presenter {

    init {
        attachView(view)
    }

    override fun getCurrentLocation() {
        locationProvider.getAddress(object : LocationInfoListener {

            override fun onLocationInfoReady(locationInfo: LocationInfo) {
                locationInfo.position?.let {
                    KarhooUISDK.analytics?.userLocated(Location("").apply {
                        latitude = it.latitude
                        longitude = it.longitude
                    })

                    view?.didGetCurrentLocation(locationInfo)
                }
            }

            override fun onLocationServicesDisabled() {
                // Do nothing
            }

            override fun onLocationInfoUnavailable(errorMessage: String) {
                view?.showSnackbar(SnackbarConfig(text = errorMessage))
            }

            override fun onResolutionRequired(resolvableApiException: ResolvableApiException) {
                view?.resolveLocationApiException(resolvableApiException)
            }
        })
    }
}
