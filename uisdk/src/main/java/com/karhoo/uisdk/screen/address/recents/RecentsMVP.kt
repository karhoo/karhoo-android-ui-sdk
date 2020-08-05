package com.karhoo.uisdk.screen.address.recents

import com.karhoo.sdk.api.model.LocationInfo

interface RecentsMVP {

    interface Presenter {
        fun loadLocations()

        fun save(location: LocationInfo)
    }

    interface View {
        fun showEmptyState()

        fun showLocations(locations: List<LocationInfo>)
    }

}
