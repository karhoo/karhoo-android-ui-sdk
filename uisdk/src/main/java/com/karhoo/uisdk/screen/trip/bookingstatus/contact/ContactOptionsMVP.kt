package com.karhoo.uisdk.screen.trip.bookingstatus.contact

import androidx.annotation.StringRes
import com.karhoo.uisdk.screen.trip.bookingstatus.BookingStatusMVP

interface ContactOptionsMVP {

    interface View {

        fun showCancelConfirmationDialog()

        fun showTripCancelledDialog()

        fun showCallToCancelDialog(number: String, quote: String)

        fun enableCallDriver()

        fun enableCallFleet()

        fun disableCallFleet()

        fun disableCallDriver()

        fun enableCancelButton()

        fun disableCancelButton()

        fun makeCall(number: String)

        fun showLoadingDialog(show: Boolean)

        fun observeTripStatus(bookingStatusPresenter: BookingStatusMVP.Presenter)

        fun showError(@StringRes errorMessageId: Int)

    }

    interface Presenter {

        fun cancelTrip()

        fun cancelPressed()

        fun contactFleet()

        fun contactDriver()

    }

}
