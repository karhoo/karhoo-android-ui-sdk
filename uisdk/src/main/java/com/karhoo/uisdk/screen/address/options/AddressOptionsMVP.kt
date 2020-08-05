package com.karhoo.uisdk.screen.address.options

import com.google.android.gms.common.api.ResolvableApiException
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.uisdk.base.listener.ErrorView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig

interface AddressOptionsMVP {

    interface View {
        fun didGetCurrentLocation(location: LocationInfo)

        fun resolveLocationApiException(resolvableApiException: ResolvableApiException)

        fun showSnackbar(snackbarConfig: SnackbarConfig)
    }

    interface Actions : ErrorView {
        fun pickFromMap()

        fun didSelectCurrentLocation(location: LocationInfo)
    }

    interface Presenter {
        fun getCurrentLocation()
    }
}
