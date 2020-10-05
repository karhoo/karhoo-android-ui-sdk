package com.karhoo.karhootraveller.presentation.splash.register

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.karhoo.sdk.api.model.AuthenticationMethod

interface SplashMVP {

    interface View {

        fun setLoginRegVisibility(visibility: Boolean)

        fun saveUsersLocation(latLng: LatLng)

        fun goToBooking(location: Location?)

        fun goToLogin()

        fun onResume()

        fun appInvalid()

        fun promptUpdatePlayServices(errorCode: Int)

        fun setConfig(authenticationMethod: AuthenticationMethod)
    }

    interface Presenter {

        fun handleLoginTypeSelection(loginType: String)

        fun checkIfUserIsLoggedIn()

        fun getUsersLocation()

        fun locationUpdatesDenied()
    }

}
