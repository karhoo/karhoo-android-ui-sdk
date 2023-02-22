package com.karhoo.uisdk.screen.booking.checkout.payment.braintree

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.braintreepayments.api.DropInRequest
import com.braintreepayments.api.ThreeDSecureRequest
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.screen.booking.checkout.payment.PaymentDropInContract
import com.karhoo.uisdk.util.extension.isGuest
import java.util.Locale

class BraintreePaymentView : PaymentDropInContract.View {

    var presenter: PaymentDropInContract.Presenter? = BraintreePaymentPresenter()
    override var actions: PaymentDropInContract.Actions? = null
    set(value) {
        field = value
        presenter?.view = value
    }

    override fun handleThreeDSecure(context: Context, sdkToken: String, nonce: String, amount: String) {
        actions?.showLoadingButton(true)

        val dropInRequest: DropInRequest = presenter?.getDropInConfig(context, sdkToken) as
                DropInRequest
        val threeDSecureRequest = ThreeDSecureRequest().apply {
            this.nonce = nonce
            this.amount = presenter?.quotePriceToAmount(null)
            this.versionRequested = ThreeDSecureRequest.VERSION_2
        }
        dropInRequest.threeDSecureRequest = threeDSecureRequest
        val requestCode = if (isGuest()) REQ_CODE_BRAINTREE_GUEST else REQ_CODE_BRAINTREE

        val builder = BraintreePaymentActivity.Builder()
            .dropInRequest(dropInRequest)
            .sdkToken(sdkToken)
        (context as Activity).startActivityForResult(builder.build(context), requestCode)
    }

    override fun initialiseChangeCard(quote: Quote?, locale: Locale?) {
        presenter?.sdkInit(quote, null)
    }

    override fun initialiseGuestPayment(quote: Quote?) {
        presenter?.initialiseGuestPayment(quote)
    }

    override fun initialisePaymentFlow(quote: Quote?) {
        presenter?.getPaymentNonce(quote)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return presenter?.handleActivityResult(requestCode, resultCode, data) == false
    }

    override fun showPaymentDropInUI(context: Context, sdkToken: String, paymentData: String?, quote: Quote?) {
        actions?.showLoadingButton(true)

        val dropInRequest: DropInRequest = presenter?.getDropInConfig(context, sdkToken) as
                DropInRequest
        val threeDSecureRequest = ThreeDSecureRequest().apply {
            this.amount = presenter?.quotePriceToAmount(quote)
            this.versionRequested = ThreeDSecureRequest.VERSION_2
        }
        dropInRequest.threeDSecureRequest = threeDSecureRequest
        val requestCode = if (isGuest()) REQ_CODE_BRAINTREE_GUEST else REQ_CODE_BRAINTREE

        val builder = BraintreePaymentActivity.Builder()
            .dropInRequest(dropInRequest)
            .sdkToken(sdkToken)
        (context as Activity).startActivityForResult(builder.build(context), requestCode)
    }

    override fun setPassenger(passengerDetails: PassengerDetails?) {
        presenter?.setPassenger(passengerDetails)
    }

    companion object {
        const val REQ_CODE_BRAINTREE = 301
        const val REQ_CODE_BRAINTREE_GUEST = 302
    }
}
