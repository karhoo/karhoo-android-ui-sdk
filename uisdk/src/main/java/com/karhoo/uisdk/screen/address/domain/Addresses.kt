package com.karhoo.uisdk.screen.address.domain

import com.karhoo.sdk.api.model.Places
import com.karhoo.sdk.api.network.request.PlaceSearch

data class Addresses(val query: PlaceSearch, val locations: Places)