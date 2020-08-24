package com.karhoo.uisdk.screen.booking.booking.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.uisdk.screen.booking.booking.BookingPaymentView
import com.karhoo.uisdk.util.extension.isGuest

class BraintreePaymentView constructor(context: Context) : BookingPaymentView(context), PaymentMVP.ViewActions {

    private var paymentPresenter: PaymentMVP.Presenter = BraintreePaymentPresenter(view = this)

    override fun getPaymentNonce(price: QuotePrice?) {
        paymentPresenter.initialiseGuestPayment(price)
    }

    override fun initialiseGuestPayment(price: QuotePrice?) {
        paymentPresenter.initialiseGuestPayment(price)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            when (requestCode) {
                REQ_CODE_BRAINTREE -> {
                    val braintreeResult = data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                    paymentPresenter.passBackNonce(braintreeResult?.paymentMethodNonce?.nonce.orEmpty())
                }
                REQ_CODE_BRAINTREE_GUEST -> {
                    val braintreeResult = data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                    braintreeResult?.paymentMethodNonce?.let {
                        paymentPresenter.updateCardDetails(it.nonce, it.description, it.typeLabel)
                    }
                    handlePaymentDetailsUpdate(braintreeResult?.paymentMethodNonce?.nonce)
                }
            }
        } else if (requestCode == REQ_CODE_BRAINTREE || requestCode == REQ_CODE_BRAINTREE_GUEST) {
//            refresh()
        }
    }

    override fun sdkInit() {
        paymentPresenter.sdkInit()
    }

    override fun showPaymentUI(braintreeSDKToken: String) {

        val dropInRequest = DropInRequest().clientToken(braintreeSDKToken)
        val requestCode = if (isGuest()) REQ_CODE_BRAINTREE_GUEST else REQ_CODE_BRAINTREE
        (context as Activity).startActivityForResult(dropInRequest.getIntent(context), requestCode)
    }

    companion object {
        private const val REQ_CODE_BRAINTREE = 301
        private const val REQ_CODE_BRAINTREE_GUEST = 302
    }
}