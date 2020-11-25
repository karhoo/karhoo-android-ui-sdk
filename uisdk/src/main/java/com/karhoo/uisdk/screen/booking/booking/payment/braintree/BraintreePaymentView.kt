package com.karhoo.uisdk.screen.booking.booking.payment.braintree

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
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.uisdk.screen.booking.booking.payment.BookingPaymentMVP
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentDropInMVP
import com.karhoo.uisdk.util.extension.isGuest

class BraintreePaymentView constructor(actions: PaymentDropInMVP.Actions) : PaymentDropInMVP.View {

    var presenter: PaymentDropInMVP.Presenter? = BraintreePaymentPresenter(actions)
    var actions: PaymentDropInMVP.Actions? = actions

    override fun handleThreeDSecure(context: Context, sdkToken: String, nonce: String, amount: String) {
        val braintreeFragment = BraintreeFragment
                .newInstance(context as AppCompatActivity, sdkToken)

        braintreeFragment.addListener(object : PaymentMethodNonceCreatedListener {
            override fun onPaymentMethodNonceCreated(paymentMethodNonce: PaymentMethodNonce?) {
                actions?.threeDSecureNonce(paymentMethodNonce?.nonce.orEmpty())
            }
        })

        braintreeFragment.addListener(
                object : BraintreeErrorListener {
                    override fun onError(error: Exception?) {
                        actions?.showPaymentFailureDialog()
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

    override fun initialiseChangeCard(price: QuotePrice?) {
        presenter?.sdkInit(price)
    }

    override fun initialiseGuestPayment(price: QuotePrice?) {
        presenter?.initialiseGuestPayment(price)
    }

    override fun initialisePaymentFlow(price: QuotePrice?) {
        presenter?.getPaymentNonce(price)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter?.handleActivityResult(requestCode, resultCode, data)
    }

    override fun showPaymentDropInUI(context: Context, sdkToken: String, paymentData:
    String?, price: QuotePrice?) {
        val dropInRequest: DropInRequest = presenter?.getDropInConfig(context, sdkToken) as
                DropInRequest
        val requestCode = if (isGuest()) REQ_CODE_BRAINTREE_GUEST else REQ_CODE_BRAINTREE
        (context as Activity).startActivityForResult(dropInRequest.getIntent(context), requestCode)
    }

    companion object {
        const val REQ_CODE_BRAINTREE = 301
        const val REQ_CODE_BRAINTREE_GUEST = 302
    }
}
