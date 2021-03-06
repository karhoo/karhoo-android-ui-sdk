package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.karhoo.uisdk.R

class AdyenResultActivity : AppCompatActivity() {
    companion object {
        const val RESULT_KEY = "payment_result"
        const val TYPE_KEY = "integration_type"
        const val RESULT_PROCESSING = "Processing"

        fun start(context: Context, paymentResult: String) {
            val intent = Intent(context, AdyenResultActivity::class.java)
            intent.putExtra(RESULT_KEY, paymentResult)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.uisdk_activity_base)

        val res = intent?.getStringExtra(RESULT_KEY) ?: RESULT_PROCESSING
        val data = Intent()
        data.putExtra(RESULT_KEY, res)
        setResult(RESULT_OK, data)

        finish()
    }
}
