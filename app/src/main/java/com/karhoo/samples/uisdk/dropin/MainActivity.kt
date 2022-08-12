package com.karhoo.samples.uisdk.dropin

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.karhoo.samples.uisdk.dropin.config.LoyaltyTokenConfig
import com.karhoo.samples.uisdk.dropin.config.KarhooConfig
import com.karhoo.samples.uisdk.dropin.config.AdyenGuestConfig
import com.karhoo.samples.uisdk.dropin.config.AdyenTokenExchangeConfig
import com.karhoo.samples.uisdk.dropin.config.BraintreeTokenExchangeConfig
import com.karhoo.samples.uisdk.dropin.config.BraintreeGuestConfig
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.network.request.UserLogin
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.booking.checkout.payment.AdyenPaymentManager
import com.karhoo.uisdk.screen.booking.checkout.payment.BraintreePaymentManager
import com.karhoo.uisdk.screen.booking.checkout.payment.adyen.AdyenPaymentView
import com.karhoo.uisdk.screen.booking.checkout.payment.braintree.BraintreePaymentView
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var loadingProgressBar: View
    private var braintreePaymentManager: BraintreePaymentManager = BraintreePaymentManager()
    private var adyenPaymentManager: AdyenPaymentManager = AdyenPaymentManager()

    init {
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->
            this.startActivity(Intent(this, MainActivity::class.java))
            exitProcess(0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        KarhooApi.userService.logout()

        KarhooApi.paymentsService.getAdyenClientKey()
        adyenPaymentManager.paymentProviderView = AdyenPaymentView()
        braintreePaymentManager.paymentProviderView = BraintreePaymentView()

        loadingProgressBar = findViewById<View>(R.id.loadingSpinner)

        findViewById<Button>(R.id.bookTripButtonBraintreeGuest).setOnClickListener {
            showLoading()

            applyBraintreeGuestConfig()

            goToBooking()
        }

        findViewById<Button>(R.id.bookTripButtonBraintreeTokenExchange).setOnClickListener {
            showLoading()

            applyBraintreeTokenExchangeConfig()

            loginTokenExchange(BuildConfig.BRAINTREE_AUTH_TOKEN)
        }

        findViewById<Button>(R.id.bookTripButtonAdyenGuest).setOnClickListener {
            showLoading()

            applyAdyenGuestConfig()

            goToBooking()
        }

        findViewById<Button>(R.id.bookTripButtonAdyenTokenExchange).setOnClickListener {
            showLoading()

            applyAdyenTokenExchangeConfig()

            loginTokenExchange(BuildConfig.ADYEN_AUTH_TOKEN)
        }

        findViewById<Button>(R.id.bookTripButtonLoyaltyTokenExchange).setOnClickListener {
            showLoading()

            applyLoyaltyTokenExchangeConfig()

            loginTokenExchange(BuildConfig.LOYALTY_AUTH_TOKEN)
        }

        findViewById<Button>(R.id.bookTripButtonLogin).setOnClickListener {
            val config = KarhooConfig(applicationContext)
            config.paymentManager = braintreePaymentManager

            KarhooUISDK.apply {
                setConfiguration(config)
            }
            showLoginInputDialog()
        }
    }

    private fun showLoginInputDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.view_login)
        val userNameEditText = dialog.findViewById(R.id.emailInput) as TextInputEditText
        val passwordEditText = dialog.findViewById(R.id.passwordInput) as TextInputEditText
        val signInButton = dialog.findViewById(R.id.signInButton) as Button
        signInButton.setOnClickListener {
            showLoading()
            loginUser(userNameEditText.text.toString(), passwordEditText.text.toString())
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun loginUser(email: String, password: String) {
        KarhooApi.userService.loginUser(UserLogin(email = email, password = password))
            .execute { result ->
                when (result) {
                    is Resource.Success -> goToBooking()
                    is Resource.Failure -> toastErrorMessage(result.error)
                }
            }
    }

    private fun applyBraintreeTokenExchangeConfig() {
        val config = BraintreeTokenExchangeConfig(applicationContext)
        config.paymentManager = braintreePaymentManager

        KarhooUISDK.apply {
            setConfiguration(config)
        }
    }

    private fun applyBraintreeGuestConfig() {
        val config = BraintreeGuestConfig(applicationContext)
        config.paymentManager = braintreePaymentManager

        KarhooUISDK.apply {
            setConfiguration(config)
        }
    }

    private fun applyAdyenTokenExchangeConfig() {
        val config = AdyenTokenExchangeConfig(applicationContext)
        config.paymentManager = adyenPaymentManager

        KarhooUISDK.apply {
            setConfiguration(config)
        }
    }

    private fun applyLoyaltyTokenExchangeConfig() {
        val config = LoyaltyTokenConfig(applicationContext)
        config.paymentManager = adyenPaymentManager

        KarhooUISDK.apply {
            setConfiguration(config)
        }
    }

    private fun applyAdyenGuestConfig() {
        val config = AdyenGuestConfig(
            applicationContext
        )

        config.paymentManager = adyenPaymentManager
        KarhooUISDK.apply {
            setConfiguration(config)
        }
    }

    private fun loginTokenExchange(token: String) {
        KarhooApi.userService.logout()
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
