package com.karhoo.uisdk.screen.booking.map

import androidx.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import com.karhoo.sdk.api.model.LocationInfo

interface BookingMapStategy {

    interface Presenter {

        fun mapMoved(position: LatLng)

        fun setOwner(owner: Owner)

        fun mapDragged()

        fun locateUserPressed()

    }

    interface Owner {

        fun setPickupLocation(pickupLocation: LocationInfo?)

        fun zoom(position: LatLng)

        fun zoomMapToMarkers()

        fun locateAndUpdate()

        fun onError(@StringRes errorMessage: Int)

    }

}
