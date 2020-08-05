package com.karhoo.uisdk.service.preference

import com.karhoo.sdk.api.model.TripInfo

interface PreferenceStore {

    var lastTrip: TripInfo?

}
