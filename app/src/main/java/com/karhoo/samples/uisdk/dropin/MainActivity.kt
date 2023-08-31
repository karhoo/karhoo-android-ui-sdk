package com.karhoo.samples.uisdk.dropin

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import kotlinx.coroutines.GlobalScope
import android.util.Log
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatDelegate
import com.karhoo.farechoice.service.analytics.KarhooAnalytics
import com.karhoo.samples.uisdk.dropin.BuildConfig.ADYEN_AUTH_TOKEN
import com.karhoo.samples.uisdk.dropin.BuildConfig.BRAINTREE_AUTH_TOKEN
import com.karhoo.samples.uisdk.dropin.config.LoyaltyTokenBraintreeConfig
import com.karhoo.sdk.analytics.AnalyticsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var loadingProgressBar: View
    private lateinit var sharedPrefs: SharedPreferences
    private val notificationsId = "notifications_enabled"
    val darkModeId = "darkMode_enabled"
    private var requestedAuthentication = false
    private var username: String? = null
    private var password: String? = null
    private var deferredRequests: MutableList<(()-> Unit)> = arrayListOf()

    init {
        Thread.setDefaultUncaughtExceptionHandler { _, eh ->
            Log.e(TAG, "Uncaught exception")
            eh.message?.let { Log.e(TAG, it) }
            eh.stackTrace.let { Log.e(TAG, Arrays.toString(eh.stackTrace)) }
            eh.printStackTrace()

            this.startActivity(Intent(this, MainActivity::class.java))
            exitProcess(0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPrefs = this.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        KarhooApi.userService.logout()

        KarhooApi.paymentsService.getAdyenClientKey()

        loadingProgressBar = findViewById<View>(R.id.loadingSpinner)

        findViewById<Button>(R.id.bookTripButtonBraintreeGuest).setOnClickListener {
            showLoading()

            applyBraintreeGuestConfig()
            KarhooApi.userService.logout()

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

        findViewById<Button>(R.id.bookTripButtonLoyaltyTokenExchangeBraintree).setOnClickListener {
            showLoading()

            applyLoyaltyTokenExchangeBraintreeConfig()

            loginTokenExchange(BuildConfig.LOYALTY_AUTH_TOKEN)
        }

        findViewById<Button>(R.id.bookTripButtonLogin).setOnClickListener {
            val config = KarhooConfig(applicationContext)
            config.paymentManager = createBraintreeManager()

            config.sdkAuthenticationRequired = { callback ->
                Log.e(TAG, "Need an external authentication")

                if (username != null && password != null) {
                    KarhooApi.userService.logout()
                    loginUser(username!!, password!!, goToBooking = false, callback)
                }
            }

            KarhooUISDK.apply {
                setConfiguration(config, applicationContext)
            }
            showLoginInputDialog()
        }

        val checkbox = findViewById<CheckBox>(R.id.notifications_checkbox)
        checkbox.setChecked(getCurrentNotificationStatus())

        val checkbox2 = findViewById<CheckBox>(R.id.darkmode_checkbox)
        checkbox2.isChecked = getCurrentDarkModeStatus()
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
            this.username = userNameEditText.text.toString()
            this.password = passwordEditText.text.toString()

            if (username != null && password != null) {
                loginUser(username!!, password!!, goToBooking = true)
            }

            dialog.dismiss()
        }
        dialog.show()
    }

    private fun loginUser(
        email: String,
        password: String,
        goToBooking: Boolean,
        callback: (() -> Unit)? = null
    ) {
        KarhooApi.userService.loginUser(UserLogin(email = email, password = password))
            .execute { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "User authenticated with success")

                        if (goToBooking) {
                            goToBooking()
                        }

                        callback?.invoke()
                    }
                    is Resource.Failure -> toastErrorMessage(result.error)
                }
            }
    }

    private fun createBraintreeManager(): BraintreePaymentManager{
        return BraintreePaymentManager().apply {
            this.paymentProviderView = BraintreePaymentView()
        }
    }

    private fun createAdyenManager(): AdyenPaymentManager{
        return AdyenPaymentManager().apply {
            this.paymentProviderView = AdyenPaymentView()
        }
    }
    private fun applyBraintreeTokenExchangeConfig() {
        val config = BraintreeTokenExchangeConfig(applicationContext)
        config.paymentManager = createBraintreeManager()
        config.sdkAuthenticationRequired = {
            loginInBackground(it, BRAINTREE_AUTH_TOKEN)
        }
        config.forceDarkMode = getCurrentDarkModeStatus()
        KarhooUISDK.apply {
            setConfiguration(config, applicationContext)
        }
    }

    private fun applyBraintreeGuestConfig() {
        val config = BraintreeGuestConfig(applicationContext)
        config.paymentManager = createBraintreeManager()
        KarhooUISDK.apply {
            setConfiguration(config, applicationContext)
        }
    }

    private fun applyAdyenTokenExchangeConfig() {
        val config = AdyenTokenExchangeConfig(applicationContext)
        config.paymentManager = createAdyenManager()
        KarhooApi.userService.logout()
        config.sdkAuthenticationRequired = {
            loginInBackground(it, ADYEN_AUTH_TOKEN)
        }
        config.forceDarkMode = getCurrentDarkModeStatus()

        KarhooUISDK.apply {
            setConfiguration(config, applicationContext)
        }
    }

    private fun applyLoyaltyTokenExchangeConfig() {
        val config = LoyaltyTokenConfig(applicationContext)
        config.paymentManager = createAdyenManager()
        KarhooApi.userService.logout()
        config.sdkAuthenticationRequired = {
            loginInBackground(it, BuildConfig.LOYALTY_AUTH_TOKEN)
        }
        config.forceDarkMode = getCurrentDarkModeStatus()

        KarhooUISDK.apply {
            setConfiguration(config, applicationContext)
        }
    }

    private fun applyLoyaltyTokenExchangeBraintreeConfig() {
        val config = LoyaltyTokenBraintreeConfig(applicationContext)
        config.paymentManager = createBraintreeManager()
        KarhooApi.userService.logout()
        config.sdkAuthenticationRequired = {
            loginInBackground(it, BuildConfig.LOYALTY_AUTH_TOKEN)
        }
        config.forceDarkMode = getCurrentDarkModeStatus()

        KarhooUISDK.apply {
            setConfiguration(config, applicationContext)
        }
    }

    private fun loginInBackground(callback: () -> Unit, token: String) {
        if (!requestedAuthentication) {
            Log.e(TAG, "Need an external authentication")
            requestedAuthentication = true
            deferredRequests.add(callback)

            GlobalScope.launch {
                delay(TESTING_DELAY)
                KarhooApi.authService.login(token).execute { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.e(TAG, "We got a new token from the back-end")

                            deferredRequests.map {
                                it.invoke()
                            }
                            deferredRequests.clear()

                            requestedAuthentication = false
                        }
                        is Resource.Failure -> toastErrorMessage(result.error)
                    }
                }
            }
        } else {
            deferredRequests.add(callback)
        }
    }

    private fun applyAdyenGuestConfig() {
        val config = AdyenGuestConfig(
            applicationContext
        )

        config.paymentManager = createAdyenManager()
        KarhooApi.userService.logout()
        KarhooUISDK.apply {
            setConfiguration(config, applicationContext)
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
        AnalyticsManager.initialise()
        KarhooUISDK.analytics = KarhooAnalytics.INSTANCE
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

    fun onCheckboxClicked(view: View) {
        view
        toggleNotificationStatus()
    }

    fun onDarkModeCheckboxClicked(view: View) {
        view
        setDarkModeStatus(!getCurrentDarkModeStatus())
    }

    private fun getCurrentNotificationStatus(): Boolean {
        return sharedPrefs.getBoolean(notificationsId, false)
    }

    private fun getCurrentDarkModeStatus(): Boolean {
        return sharedPrefs.getBoolean(darkModeId, false)
    }

    private fun setNotificationStatus(value: Boolean) {
        with(sharedPrefs.edit()) {
            putBoolean(notificationsId, value)
            apply()
        }
    }

    private fun setDarkModeStatus(value: Boolean) {
        with(sharedPrefs.edit()) {
            putBoolean(darkModeId, value)
            apply()
        }
        if(value){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun toggleNotificationStatus() {
        setNotificationStatus(!getCurrentNotificationStatus())
    }

    companion object {
        private val TAG = MainActivity::class.java.name
        private const val TESTING_DELAY = 5000L
    }
}
