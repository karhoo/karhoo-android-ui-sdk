package com.karhoo.uisdk.screen.address.recents

import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.uisdk.base.BasePresenter

private const val MAX_LOCATIONS = 5

class RecentsPresenter(view: RecentsMVP.View,
                       private val locationStore: LocationStore) : BasePresenter<RecentsMVP.View>(), RecentsMVP.Presenter {

    private var locations = mutableListOf<LocationInfo>()

    init {
        attachView(view)
        this.locations = locationStore.retrieve().toMutableList()
    }

    override fun loadLocations() {
        locations = locationStore.retrieve().toMutableList()
        showLocations(locations)
    }

    override fun save(location: LocationInfo) {
        locations = locations.filterNot { it == location }
                .take(MAX_LOCATIONS - 1)
                .toMutableList()

        locations.add(0, location)

        locationStore.save(locations)
    }

    private fun showLocations(locations: List<LocationInfo>) {
        if (locations.isEmpty()) {
            view?.showEmptyState()
        } else {
            view?.showLocations(locations)
        }
    }

}
