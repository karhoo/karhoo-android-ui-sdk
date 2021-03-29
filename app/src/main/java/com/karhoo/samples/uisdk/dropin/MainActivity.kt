package com.karhoo.samples.uisdk.dropin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.karhoo.samples.uisdk.dropin.config.GuestConfig
import com.karhoo.samples.uisdk.dropin.config.TokenExchangeConfig
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.screen.booking.BookingActivity

class MainActivity : AppCompatActivity() {

    private lateinit var loadingProgressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        KarhooApi.userService.logout()

        loadingProgressBar = findViewById<View>(R.id.loadingSpinner)

        findViewById<Button>(R.id.bookTripButtonGuest).setOnClickListener {
            showLoading()

            applyGuestConfig()

            goToBooking()
        }

        findViewById<Button>(R.id.bookTripButtonTokenExchange).setOnClickListener {
            showLoading()

            applyTokenExchangeConfig()

            loginTokenExchange()
        }
    }

    private fun applyTokenExchangeConfig() {
        KarhooUISDK.apply {
            setConfiguration(
                TokenExchangeConfig(
                    applicationContext
                )
            )
        }
    }

    private fun applyGuestConfig() {
        KarhooUISDK.apply {
            setConfiguration(
                GuestConfig(
                    applicationContext
                )
            )
        }
    }

    private fun loginTokenExchange() {
        KarhooApi.userService.logout()
        val token: String = BuildConfig.BRAINTREE_AUTH_TOKEN
        KarhooApi.authService.login(token).execute { result ->
            when (result) {
                is Resource.Success -> goToBooking()
                is Resource.Failure -> toastErrorMessage(result.error)
            }
        }
    }

    private fun goToBooking() {
        val builder = BookingActivity.Builder.builder
            .initialLocation(null)
        startActivity(builder.build(this))
        hideLoading()
    }

    private fun toastErrorMessage(error: KarhooError) {
        Toast.makeText(
            this,
            error.userFriendlyMessage,
            Toast.LENGTH_LONG
        ).show()
        hideLoading()
    }

    private fun showLoading() {
        loadingProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        loadingProgressBar.visibility = View.INVISIBLE
    }
}