package com.karhoo.uisdk.screen.booking.map

import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.model.LatLng
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.uisdk.base.listener.ErrorView
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel

interface BookingMapMVP {

    interface View : ErrorView {

        fun zoom(position: LatLng?)

        fun clearMarkers()

        fun addMarkers(pickup: Position,
                       dropoff: Position?)

        fun addPickUpMarker(pickup: Position?,
                            dropoff: Position?)

        fun zoomMapToOriginAndDestination()

        fun zoomMapToOriginAndDestination(origin: Position, destination: Position?)

        fun locateUser()

        fun doReverseGeolocate()

        fun moveTo(position: LatLng?)

        fun resetMap()

        fun locationPermissionGranted()
        
        fun showLocationButton(show: Boolean)

        fun updateMapViewForQuotesListVisibilityCollapsed()

        fun updateMapViewForQuotesListVisibilityExpanded()
    }

    interface Presenter {

        fun watchBookingStatus(lifecycleOwner: LifecycleOwner, journeyDetailsStateViewModel: JourneyDetailsStateViewModel)

        fun mapMoved(position: LatLng?)

        fun setPickupLocation(pickupLocation: LocationInfo?)

        fun mapDragged()

        fun zoom(position: LatLng)

        fun locateUserPressed()

        fun locationPermissionGranted()

        fun checkLocateUser()
    }

    interface Actions : ErrorView

}
