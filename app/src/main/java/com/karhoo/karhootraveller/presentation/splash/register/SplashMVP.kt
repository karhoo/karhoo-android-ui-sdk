package com.karhoo.karhootraveller.presentation.splash.register

import android.location.Location
import com.google.android.gms.maps.model.LatLng

interface SplashMVP {

    interface View {

        fun setLoginRegVisibility(visibility: Boolean)

        fun saveUsersLocation(latLng: LatLng)

        fun goToBooking(location: Location?)

        fun onResume()

        fun appInvalid()

        fun promptUpdatePlayServices(errorCode: Int)
    }

    interface Presenter {

        fun checkIfUserIsLoggedIn()

        fun getUsersLocation()

        fun locationUpdatesDenied()
    }

}
