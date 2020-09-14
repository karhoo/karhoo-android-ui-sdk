package com.karhoo.uisdk.screen.address.map

import androidx.lifecycle.Lifecycle
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.uisdk.base.address.AddressType
import com.karhoo.uisdk.base.snackbar.SnackbarConfig

interface AddressMapMVP {

    interface View {

        fun updateDisplayAddress(displayAddress: String)

        fun setFinalAddress(locationInfo: LocationInfo?)

        fun hideMap()

        fun setAddressType(addressType: AddressType?)

    }

    interface Presenter {

        fun locationUpdated(locationInfo: LocationInfo?)

        fun getAddress(position: Position)

        fun selectAddressPressed()

        fun onBackArrowPressed()

    }

    interface Actions {

        fun getLifecycle(): Lifecycle

        fun addressSelected(locationInfo: LocationInfo, addressPositionInList: Int = 0)

        fun showSnackbar(snackbarConfig: SnackbarConfig)

        fun dismissSnackbar()

    }
}
