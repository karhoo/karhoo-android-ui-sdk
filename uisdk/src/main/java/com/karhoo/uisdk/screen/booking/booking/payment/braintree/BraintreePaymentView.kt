package com.karhoo.uisdk.screen.booking.booking.payment.braintree

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.braintreepayments.api.BraintreeFragment
import com.braintreepayments.api.ThreeDSecure
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.braintreepayments.api.interfaces.BraintreeErrorListener
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener
import com.braintreepayments.api.models.PaymentMethodNonce
import com.braintreepayments.api.models.ThreeDSecureRequest
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentDropInMVP
import com.karhoo.uisdk.util.extension.isGuest

class BraintreePaymentView : PaymentDropInMVP.View {

    var actions: PaymentDropInMVP.Actions? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            when (requestCode) {
                REQ_CODE_BRAINTREE -> {
                    val braintreeResult = data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                    actions?.passBackNonce(braintreeResult?.paymentMethodNonce?.nonce.orEmpty())
                }
                REQ_CODE_BRAINTREE_GUEST -> {
                    val braintreeResult = data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                    braintreeResult?.paymentMethodNonce?.let {
                        actions?.updateCardDetails(it.nonce, it.description, it.typeLabel)
                    }
                    actions?.handlePaymentDetailsUpdate(braintreeResult?.paymentMethodNonce?.nonce)
                }
            }
        } else if (requestCode == REQ_CODE_BRAINTREE || requestCode == REQ_CODE_BRAINTREE_GUEST) {
            actions?.refresh()
        }
    }

    override fun showPaymentUI(braintreeSDKToken: String, paymentData: String?, price: QuotePrice?, context: Context) {
        val dropInRequest = DropInRequest().clientToken(braintreeSDKToken)
        val requestCode = if (isGuest()) REQ_CODE_BRAINTREE_GUEST else REQ_CODE_BRAINTREE
        (context as Activity).startActivityForResult(dropInRequest.getIntent(context), requestCode)
    }

    override fun handleThreeDSecure(context: Context, braintreeSDKToken: String, nonce: String, amount: String) {
        val braintreeFragment = BraintreeFragment
                .newInstance(context as AppCompatActivity, braintreeSDKToken)

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

    companion object {
        private const val REQ_CODE_BRAINTREE = 301
        private const val REQ_CODE_BRAINTREE_GUEST = 302
    }
}