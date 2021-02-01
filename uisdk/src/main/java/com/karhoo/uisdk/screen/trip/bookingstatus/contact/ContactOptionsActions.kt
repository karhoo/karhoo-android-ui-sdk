package com.karhoo.uisdk.screen.trip.bookingstatus.contact

import com.karhoo.sdk.api.KarhooError

interface ContactOptionsActions {

    fun goToCleanBooking()

    fun showTemporaryError(error: String, karhooError: KarhooError?)

}
