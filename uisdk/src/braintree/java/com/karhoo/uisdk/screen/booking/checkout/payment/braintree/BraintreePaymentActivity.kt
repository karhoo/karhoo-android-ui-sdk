package com.karhoo.uisdk.screen.booking.checkout.payment.braintree

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.braintreepayments.api.DropInClient
import com.braintreepayments.api.DropInListener
import com.braintreepayments.api.DropInRequest
import com.braintreepayments.api.DropInResult
import com.braintreepayments.api.UserCanceledException
import com.karhoo.sdk.api.KarhooError
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.util.extension.isGuest
import java.lang.Exception

class BraintreePaymentActivity : BaseActivity(), DropInListener {
    override val layout: Int
        get() = R.layout.activity_braintree_payment

    private lateinit var sdkToken: String
    private lateinit var dropInRequest: DropInRequest
    private lateinit var dropInClient: DropInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        extras?.let {
            sdkToken = it.getString(BRAINTREE_ACTIVITY_SDK_TOKEN, "")
            dropInRequest = it.getParcelable<DropInRequest>(BRAINTREE_ACTIVITY_DROP_IN_REQUEST)!!

            dropInClient = DropInClient(this@BraintreePaymentActivity, sdkToken)
        }
    }

    override fun onStart() {
        super.onStart()
        dropInClient.setListener(this)
        dropInClient.launchDropIn(dropInRequest)
    }

    override fun handleExtras() {
        // do nothing
    }

    override fun onDropInSuccess(dropInResult: DropInResult) {
        finishActivity(if (isGuest()) BraintreePaymentView.REQ_CODE_BRAINTREE_GUEST else BraintreePaymentView.REQ_CODE_BRAINTREE)
        setResult(RESULT_OK, Intent().apply {
            putExtra(BRAINTREE_ACTIVITY_DROP_IN_RESULT, dropInResult)
        })
        finish()
    }

    override fun onDropInFailure(error: Exception) {
        finishActivity(if (isGuest()) BraintreePaymentView.REQ_CODE_BRAINTREE_GUEST else BraintreePaymentView.REQ_CODE_BRAINTREE)
        setResult(RESULT_OK, Intent().apply {
            if(error is UserCanceledException)
                putExtra(BRAINTREE_ACTIVITY_DROP_IN_RESULT_USER_CANCELLED_ERROR, KarhooError.fromCustomError("", error.message!!, error.localizedMessage!!))
            else
                putExtra(BRAINTREE_ACTIVITY_DROP_IN_RESULT_ERROR, KarhooError.fromCustomError("", error.message!!, error.localizedMessage!!))
        })
        finish()
    }

    /**
     * Intent Builder
     */
    class Builder {

        private val extrasBundle: Bundle = Bundle()

        fun sdkToken(sdkToken: String): Builder {
            extrasBundle.putString(BRAINTREE_ACTIVITY_SDK_TOKEN, sdkToken)
            return this
        }

        fun dropInRequest(dropInRequest: DropInRequest): Builder {
            extrasBundle.putParcelable(BRAINTREE_ACTIVITY_DROP_IN_REQUEST, dropInRequest)
            return this
        }

        fun build(context: Context): Intent = Intent(context, BraintreePaymentActivity::class.java).apply {
            putExtras(extrasBundle)
        }
    }

    companion object {
        const val BRAINTREE_ACTIVITY_SDK_TOKEN = "BRAINTREE_ACTIVITY_SDK_TOKEN"
        const val BRAINTREE_ACTIVITY_DROP_IN_REQUEST = "BRAINTREE_ACTIVITY_DROP_IN_REQUEST"
        const val BRAINTREE_ACTIVITY_DROP_IN_RESULT = "BRAINTREE_ACTIVITY_DROP_IN_RESULT"
        const val BRAINTREE_ACTIVITY_DROP_IN_RESULT_ERROR = "BRAINTREE_ACTIVITY_DROP_IN_RESULT_ERROR"
        const val BRAINTREE_ACTIVITY_DROP_IN_RESULT_USER_CANCELLED_ERROR = "BRAINTREE_ACTIVITY_DROP_IN_RESULT_USER_CANCELLED_ERROR"
    }
}
