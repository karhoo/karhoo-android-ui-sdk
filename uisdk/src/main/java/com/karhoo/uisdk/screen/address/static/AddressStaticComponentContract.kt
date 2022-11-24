package com.karhoo.uisdk.screen.address.static

import com.karhoo.sdk.api.model.LocationInfo
import org.joda.time.DateTime

interface AddressStaticComponentContract {
    interface View {
        fun setup(
            pickup: LocationInfo,
            destination: LocationInfo,
            time: DateTime? = null,
            type: AddressStaticComponent.AddressComponentType
        )

        fun setType(
            type: AddressStaticComponent.AddressComponentType,
            text: String? = null,
            time: DateTime? = null
        )

    }
}
