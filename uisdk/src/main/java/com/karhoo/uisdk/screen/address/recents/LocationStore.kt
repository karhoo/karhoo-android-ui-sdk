package com.karhoo.uisdk.screen.address.recents

import com.karhoo.sdk.api.model.LocationInfo

interface LocationStore {

    fun save(locations: List<LocationInfo>): Boolean

    fun retrieve(): List<LocationInfo>

    fun clear()

}
