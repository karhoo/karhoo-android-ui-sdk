package com.karhoo.uisdk.screen.booking.booking.payment

import android.content.Intent
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo

interface BookingPaymentMVP {

    interface Widget {

        fun setPaymentViewVisibility()
    }

    interface View {

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun bindDropInView()

        fun bindPaymentDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun showError(@StringRes error: Int, karhooError: KarhooError?)

        fun setPaymentView(view: PaymentDropInMVP.View?)

        fun setViewVisibility(visibility: Int)

        fun updatePaymentViewVisbility(visibility: Int)
    }

    interface Presenter {

        fun createPaymentView(actions: PaymentDropInMVP.Actions)

        fun getPaymentProvider()

        fun getPaymentViewVisibility()
    }

    interface PaymentViewActions {

        fun showErrorDialog(@StringRes stringId: Int, karhooError: KarhooError?)

        fun handleChangeCard()

        fun handleViewVisibility(visibility: Int)
    }

    interface PaymentActions {

        fun handleChangeCard()

        fun showPaymentUI()

        fun showPaymentFailureDialog(error: KarhooError?)

        fun handlePaymentDetailsUpdate()

        fun showPaymentDialog(error: KarhooError? = null)

        fun threeDSecureNonce(threeDSNonce: String, tripId: String?)
    }
}
