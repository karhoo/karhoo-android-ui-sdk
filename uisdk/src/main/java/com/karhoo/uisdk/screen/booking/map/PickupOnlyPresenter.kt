package com.karhoo.uisdk.screen.booking.map

import com.google.android.gms.maps.model.LatLng
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.address.AddressService
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired

internal class PickupOnlyPresenter(private val addressService: AddressService) : BookingMapStategy.Presenter {
    private var owner: BookingMapStategy.Owner? = null

    override fun mapMoved(position: LatLng) {
        if (position.latitude == 0.0 || position.longitude == 0.0) {
            return
        }

        addressService
                .reverseGeocode(Position(position.latitude, position.longitude))
                .execute { result ->
                    when (result) {
                        is Resource.Success -> owner?.setPickupLocation(result.data)
                        is Resource.Failure -> owner?.onError(returnErrorStringOrLogoutIfRequired(result.error))
                    }
                }
    }

    override fun setOwner(owner: BookingMapStategy.Owner) {
        this.owner = owner
    }

    override fun mapDragged() {
        owner?.setPickupLocation(null)
    }

    override fun locateUserPressed() {
        owner?.locateAndUpdate()
    }

}
