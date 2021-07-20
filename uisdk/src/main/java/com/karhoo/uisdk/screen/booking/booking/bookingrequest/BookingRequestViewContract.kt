package com.karhoo.uisdk.screen.booking.booking.bookingrequest

import android.content.Intent
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.TripInfo

interface BookingRequestViewContract {

    interface BookingRequestWidget {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
        fun showBookingRequest(quote: Quote, outboundTripId: String? = null, bookingMetadata:
        HashMap<String, String>?)
    }

    sealed class BookingRequestEvent {
        data class TermsAndConditionsRequested(val url: String) : BookingRequestEvent()
        data class BookingSuccess(val tripInfo: TripInfo) : BookingRequestEvent()
        data class BookingError(@StringRes val stringId: Int, val karhooError: KarhooError?) : BookingRequestEvent()
    }

    sealed class BookingRequestAction {
        data class ShowTermsAndConditions(val url: String) : BookingRequestAction()
        object WaitForTripAllocation : BookingRequestAction()
        data class HandleBookingError(@StringRes val stringId: Int, val karhooError: KarhooError?) : BookingRequestAction()
    }
}
