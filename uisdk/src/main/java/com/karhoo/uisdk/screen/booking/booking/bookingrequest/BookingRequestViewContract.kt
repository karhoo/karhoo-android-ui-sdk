package com.karhoo.uisdk.screen.booking.booking.bookingrequest

import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.karhoo.sdk.api.model.QuoteV2
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel

interface BookingRequestViewContract {

    interface BookingRequestWidget {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
        fun bindViewToBookingStatus(lifecycleOwner: LifecycleOwner, bookingStatusStateViewModel: BookingStatusStateViewModel)
        fun bindViewToBookingRequest(lifecycleOwner: LifecycleOwner, bookingRequestStateViewModel:
        BookingRequestStateViewModel)
        fun resetBookingButton()
        fun showBookingRequest(quote: QuoteV2, outboundTripId: String? = null)
    }

    sealed class BookingRequestEvent {
        data class BookingSuccess(val tripInfo: TripInfo) : BookingRequestEvent()
        data class BookingError(@StringRes val stringId: Int) : BookingRequestEvent()
    }

    sealed class BookingRequestAction {
        object WaitForTripAllocation : BookingRequestAction()
        data class HandleBookingError(@StringRes val stringId: Int) : BookingRequestAction()
    }
}