package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.karhoo.uisdk.R

class ResultActivity : AppCompatActivity() {
    companion object {
        const val RESULT_KEY = "payment_result"
        const val TYPE_KEY = "integration_type"

        fun start(context: Context, paymentResult: String) {
            val intent = Intent(context, ResultActivity::class.java)
            intent.putExtra(RESULT_KEY, paymentResult)
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.uisdk_activity_base)

        val res = intent?.getStringExtra(RESULT_KEY) ?: "Processing"
        val type = intent?.getStringExtra(TYPE_KEY)

        if (type != ComponentType.DROPIN.id) {

        }
    }
}