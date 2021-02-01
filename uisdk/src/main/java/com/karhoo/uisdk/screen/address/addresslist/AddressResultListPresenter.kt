package com.karhoo.uisdk.screen.address.addresslist

import com.karhoo.sdk.api.model.Place
import com.karhoo.sdk.api.network.request.LocationInfoRequest
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.address.AddressService
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.address.domain.AddressSearchProvider
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired

internal class AddressResultListPresenter(
        view: AddressResultListMVP.View,
        private val addressService: AddressService,
        private val addressProvider: AddressSearchProvider)
    : BasePresenter<AddressResultListMVP.View>(), AddressResultListMVP.Presenter {

    init {
        attachView(view)
    }

    override fun getSessionToken(): String {
        return addressProvider.getSessionToken()
    }

    override fun onAddressSelected(place: Place, addressPositionInList: Int) {
        addressService.locationInfo(LocationInfoRequest(place.placeId, addressProvider.getSessionToken())).execute { result ->
            when (result) {
                is Resource.Success -> view?.setAddress(result.data, addressPositionInList)
                is Resource.Failure -> view?.showError(returnErrorStringOrLogoutIfRequired(result.error), result.error)
            }
        }
    }
}
