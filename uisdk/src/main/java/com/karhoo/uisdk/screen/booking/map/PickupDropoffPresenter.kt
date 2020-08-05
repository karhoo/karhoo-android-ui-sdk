package com.karhoo.uisdk.screen.booking.map

import com.google.android.gms.maps.model.LatLng

class PickupDropoffPresenter : BookingMapStategy.Presenter {

    private var owner: BookingMapStategy.Owner? = null

    override fun mapMoved(position: LatLng) {
        //do nothing
    }

    override fun setOwner(owner: BookingMapStategy.Owner) {
        this.owner = owner
    }

    override fun mapDragged() {
        //do nothing
    }

    override fun locateUserPressed() {
        owner?.zoomMapToMarkers()
    }
}
