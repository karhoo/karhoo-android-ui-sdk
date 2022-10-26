package com.karhoo.uisdk.screen.booking.checkout.payment.braintree

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.braintreepayments.api.BraintreeFragment
import com.braintreepayments.api.ThreeDSecure
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.interfaces.BraintreeErrorListener
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener
import com.braintreepayments.api.models.PaymentMethodNonce
import com.braintreepayments.api.models.ThreeDSecureRequest
import com.karhoo.sdk.api.KarhooError
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
        val braintreeFragment = BraintreeFragment.newInstance(context as AppCompatActivity, sdkToken)

        braintreeFragment.addListener(object : PaymentMethodNonceCreatedListener {
            override fun onPaymentMethodNonceCreated(paymentMethodNonce: PaymentMethodNonce?) {
                actions?.threeDSecureNonce(paymentMethodNonce?.nonce.orEmpty())
            }
        })

        braintreeFragment.addListener(
                object : BraintreeErrorListener {
                    override fun onError(error: Exception?) {
                        actions?.showPaymentFailureDialog(KarhooError.fromThrowable(error))
                    }
                })

        val threeDSecureRequest = ThreeDSecureRequest()
                .nonce(nonce)
                .amount(amount)
                .versionRequested(ThreeDSecureRequest.VERSION_2)
        ThreeDSecure.performVerification(braintreeFragment, threeDSecureRequest)
        { request, lookup ->
            ThreeDSecure.continuePerformVerification(braintreeFragment, request, lookup)
        }
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
        return presenter?.handleActivityResult(requestCode, resultCode, data) == true
    }

    override fun showPaymentDropInUI(context: Context, sdkToken: String, paymentData: String?, quote: Quote?) {
        val dropInRequest: DropInRequest = presenter?.getDropInConfig(context, sdkToken) as
                DropInRequest
        val requestCode = if (isGuest()) REQ_CODE_BRAINTREE_GUEST else REQ_CODE_BRAINTREE
        (context as Activity).startActivityForResult(dropInRequest.getIntent(context), requestCode)
    }

    override fun setPassenger(passengerDetails: PassengerDetails?) {
        presenter?.setPassenger(passengerDetails)
    }

    companion object {
        const val REQ_CODE_BRAINTREE = 301
        const val REQ_CODE_BRAINTREE_GUEST = 302
    }
}
