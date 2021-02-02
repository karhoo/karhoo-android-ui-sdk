package com.karhoo.uisdk.screen.rides.detail

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.uisdk.base.ScheduledDateView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig

interface RideDetailMVP {

    interface Presenter {
        fun bindState()

        fun bindPrice()

        fun bindCard()

        fun bindButtons()

        fun bindVehicle()

        fun bindFlightDetails()

        fun bindComments()

        fun bindDate()

        fun cancelTrip()

        fun contactFleet()

        fun baseFarePressed()

        fun onResume()

        fun onPause()

    }

    interface View : ScheduledDateView {
        fun displayState(@DrawableRes icon: Int, @StringRes state: Int, @ColorRes color: Int)

        fun displayPricePending()

        fun displayPrice(price: String)

        fun displayBasePrice(price: String)

        fun displayCard(@DrawableRes logo: Int, number: String)

        fun displayVehicle(licensePlate: String)

        fun displayFlightDetails(flightNumber: String, meetingPoint: String)

        fun hideFlightDetails()

        fun displayComments(comments: String)

        fun hideComments()

        fun displayRebookButton()

        fun hideRebookButton()

        fun displayReportIssueButton()

        fun hideReportIssueButton()

        fun displayCancelRideButton()

        fun hideCancelRideButton()

        fun displayContactFleetButton()

        fun hideContactFleetButton()

        fun makeCall(number: String)

        fun displayLoadingDialog()

        fun hideLoadingDialog()

        fun displayTripCancelledDialog()

        fun displayError(@StringRes errorMessage: Int, karhooError: KarhooError?)

        fun displayCallToCancelDialog(number: String, quote: String)

        fun displayBaseFareDialog()

        fun showFeedbackSubmitted()

        interface Actions {

            fun finishActivity()

            fun showSnackbar(snackbarConfig: SnackbarConfig)

            fun showCustomerSupport(tripId: String)

            var externalDateTime: String

        }
    }

}
