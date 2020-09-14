package com.karhoo.uisdk.screen.address.map

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.address.AddressService
import com.karhoo.uisdk.base.BasePresenter

class AddressMapPresenter(view: AddressMapMVP.View,
                          private val addressService: AddressService = KarhooApi.addressService)
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
                    lastLocationInfo = result.data
                    view?.updateDisplayAddress(result.data.displayAddress)
                }
            }
        }
    }

    override fun onBackArrowPressed() {
        view?.hideMap()
    }
}
