package com.karhoo.karhootraveller.service.preference

import com.karhoo.sdk.api.model.TripInfo

interface PreferenceStore {

    var lastTrip: TripInfo?

    var loginTimeMillis: Long

}
