package com.karhoo.uisdk.screen.trip.map

import androidx.annotation.StringRes
import com.google.android.gms.common.api.ResolvableApiException
import com.karhoo.sdk.api.model.Position
import com.karhoo.uisdk.base.listener.ErrorView

interface TripMapMVP {

    interface View {

        fun animateDriverPositionToLatLng(duration: Int, latitude: Double, longitude: Double)

        fun zoomMapToIncludeLatLngs(duration: Int, vararg position: Position?)

        fun addPinToMap(position: Position, isPickup: Boolean, @StringRes title: Int)

        fun resolveApiException(resolvableApiException: ResolvableApiException)

        var userLocationVisible: Boolean

    }

    interface Presenter {

        fun setOrigin(origin: Position)

        fun setDestination(destination: Position)

        fun trackDriverPosition(tripId: String)

        fun mapIsReady()

        fun mapDragged()

        fun locateMe()

        fun onStop()

        fun onDestroy()

        fun onResume()

        fun onPause()
    }

    interface Actions : ErrorView

}
