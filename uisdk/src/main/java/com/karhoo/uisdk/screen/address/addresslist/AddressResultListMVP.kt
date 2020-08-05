package com.karhoo.uisdk.screen.address.addresslist

import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Place
import com.karhoo.uisdk.base.SimpleErrorMessageView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.address.domain.AddressSearchProvider

interface AddressResultListMVP {

    interface View : SimpleErrorMessageView {

        fun bindViewToAddresses(addressProvider: AddressSearchProvider)

        fun setAddress(location: LocationInfo, addressPositionInList: Int)

    }

    interface Presenter {

        fun onAddressSelected(place: Place, addressPositionInList: Int)

        fun getSessionToken(): String

    }

    interface Actions {

        fun addressSelected(location: LocationInfo, addressPositionInList: Int)

        fun showSnackbar(snackbarConfig: SnackbarConfig)

    }


}
