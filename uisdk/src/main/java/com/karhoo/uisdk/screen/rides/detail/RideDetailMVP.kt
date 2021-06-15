package com.karhoo.uisdk.screen.rides.detail

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.uisdk.base.ScheduledDateView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig

interface RideDetailMVP {

    interface Presenter {
        fun bindState()

        fun bindPrice()

        fun bindButtons()

        fun bindVehicle()

        fun bindFlightDetails()

        fun bindComments()

        fun bindDate()

        fun baseFarePressed()

        fun onResume()

        fun onPause()

        fun addTripInfoObserver(tripInfoListener: OnTripInfoChangedListener?)

        fun checkCancellationSLA(context: Context, trip: TripInfo, serviceCancellation: ServiceCancellation?)

        interface OnTripInfoChangedListener {

            fun onTripInfoChanged(tripInfo: TripInfo?)

        }
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

        fun displayContactOptions()

        fun hideContactOptions()

        fun makeCall(number: String)

        fun displayLoadingDialog()

        fun hideLoadingDialog()

        fun displayTripCancelledDialog()

        fun displayError(@StringRes errorMessage: Int, karhooError: KarhooError?)

        fun displayBaseFareDialog()

        fun showFeedbackSubmitted()

        fun showCancellationText(show: Boolean)

        fun setCancellationText(text: String)

        interface Actions {

            fun finishActivity()

            fun showSnackbar(snackbarConfig: SnackbarConfig)

            fun showCustomerSupport(tripId: String)

            var externalDateTime: String

        }
    }

}
