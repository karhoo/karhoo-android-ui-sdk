package com.karhoo.karhootraveller.presentation.splash

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.transition.Explode
import android.view.View
import android.view.Window
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.register.RegistrationActivity
import com.karhoo.karhootraveller.presentation.splash.register.SplashActions
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.uisdk.base.SnackbarState
import com.karhoo.uisdk.base.listener.NetworkReceiver
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.booking.domain.address.JourneyInfo
import com.karhoo.uisdk.screen.trip.TripActivity
import com.karhoo.uisdk.util.extension.isGuest
import com.karhoo.uisdk.util.extension.showWithCheck
import kotlinx.android.synthetic.main.activity_splash.snackbarContainer
import kotlinx.android.synthetic.main.activity_splash.splashScreenWidget
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class SplashActivity : AppCompatActivity(), SplashActions, NetworkReceiver.Actions {

    private var backgroundFade: TransitionDrawable? = null
    private var networkReceiver: NetworkReceiver? = null
    private var errorShown: SnackbarState = SnackbarState.NOT_SHOWN
    private var snackbar: Snackbar? = null
    private var journeyInfo: JourneyInfo? = null
    private var tripInfo: TripInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            exitTransition = Explode()
        }
        setContentView(R.layout.activity_splash)
        splashScreenWidget.splashActions = this
        backgroundFade = snackbarContainer.background as TransitionDrawable?

        val isAutomaticLogout = intent.extras?.getBoolean(EXTRA_AUTOMATIC_LOGOUT) ?: false
        if (isAutomaticLogout) {
            AlertDialog.Builder(this, R.style.DialogTheme)
                    .setMessage(R.string.automatic_logout_message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .create()
                    .show()
        }
    }

    override fun onStart() {
        super.onStart()
        backgroundFade?.resetTransition()
        if (networkReceiver == null) {
            networkReceiver = NetworkReceiver(this)
            registerReceiver(networkReceiver, networkReceiver?.intentFilter)
        }

        when (errorShown) {
            SnackbarState.NOT_SHOWN -> if (networkReceiver?.hasConnection(this) == true) {
                splashScreenWidget.onResume()
            }
            SnackbarState.INVITE -> {
                splashScreenWidget.visibility = View.INVISIBLE
                goFullScreen()
                showErrorWithAction(R.string.requested_invite, { inviteUnderstood() }, R.string.got_it)
            }
        }
        getBookingMapDeepLinkData()
        getTripTrackingDeepLinkData()
    }

    override fun onStop() {
        super.onStop()
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver)
            networkReceiver = null
        }
    }

    // Fix for system navigation hiding the invite snackbar (MOB-2016)
    override fun goFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val decorView = window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            decorView.systemUiVisibility = uiOptions
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == RegistrationActivity.REQ_CODE) {
            errorShown = SnackbarState.INVITE
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun goToBooking(location: Location?) {
        tripInfo?.let {
            startActivity(TripActivity.Builder.builder
                                  .tripInfo(it)
                                  .build(this))
        } ?: run {

            val builder = BookingActivity.Builder.builder
                    .initialLocation(location)
            journeyInfo?.let {
                builder.journeyInfo(it)
            }
            startActivity(builder.build(this))
            if (!isGuest()) {
                Handler().postDelayed({ finish() }, SPLASH_SCREEN_DELAY)
            }
        }
    }

    override fun showErrorWithAction(@StringRes error: Int, action: () -> Unit, @StringRes actionText: Int) {
        snackbar?.dismiss()
        if (errorShown == SnackbarState.NOT_SHOWN) {
            errorShown = SnackbarState.GENERIC
        }
        snackbar = Snackbar.make(snackbarContainer, getText(error), Snackbar.LENGTH_INDEFINITE)
                .setAction(actionText) { action() }
                .setActionTextColor(ContextCompat.getColor(this, R.color.text_white))
                .showWithCheck(errorShown)
    }

    override fun dismissErrors() {
        snackbar?.dismiss()
    }

    private fun inviteUnderstood() {
        errorShown = SnackbarState.NOT_SHOWN
        splashScreenWidget.visibility = View.VISIBLE
    }

    override fun connectionChanged(isConnected: Boolean) {
        if (errorShown != SnackbarState.INVITE) {
            if (isConnected) {
                snackbar?.dismiss()
                resetErrorLock()
            } else {
                showErrorWithAction(R.string.network_error, { startActivity(networkReceiver?.settingsIntent) }, R.string.settings)
                enableErrorLock(SnackbarState.NETWORK)
            }
        }
    }

    private fun resetErrorLock() {
        if (errorShown == SnackbarState.NETWORK) {
            backgroundFade?.reverseTransition(resources.getInteger(R.integer.snackbar_background_fade))
            snackbarContainer.isClickable = false
            errorShown = SnackbarState.NOT_SHOWN
            onStart()
        }
    }

    private fun enableErrorLock(state: SnackbarState) {
        errorShown = state
        backgroundFade?.startTransition(resources.getInteger(R.integer.snackbar_background_fade))
        snackbarContainer.isClickable = true
    }

    private fun getBookingMapDeepLinkData() {
        intent?.data?.apply {
            val origin = getQueryParameter(ORIGIN_LATITUDE)?.let { latitude ->
                getQueryParameter(ORIGIN_LONGITUDE)?.let { longitude ->
                    Position(latitude = latitude.toDouble(), longitude = longitude.toDouble())
                }
            }
            val destination = getQueryParameter(DESTINATION_LATITUDE)?.let { latitude ->
                getQueryParameter(DESTINATION_LONGITUDE)?.let { longitude ->
                    Position(latitude = latitude.toDouble(), longitude = longitude.toDouble())
                }
            }
            val dateTime = getQueryParameter(DATE_TIME)?.let {
                DateTime.parse(it, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"))
            }
            journeyInfo = JourneyInfo(origin, destination, dateTime)
        } ?: run { journeyInfo = null }
    }

    @Suppress("NestedBlockDepth")
    private fun getTripTrackingDeepLinkData() {
        intent?.data?.apply {
            val tripId = getQueryParameter(TRIP_ID)?.let {
                it
            }
            tripId?.let {
                val origin = getQueryParameter(ORIGIN_LATITUDE)?.let { latitude ->
                    getQueryParameter(ORIGIN_LONGITUDE)?.let { longitude ->
                        getQueryParameter(ORIGIN_DISPLAY_ADDRESS)?.let { display_address ->
                            val pos = Position(
                                    latitude = latitude.toDouble(), longitude = longitude
                                    .toDouble()
                                              )
                            TripLocationInfo(position = pos, displayAddress = display_address)
                        }
                    }
                }
                val destination = getQueryParameter(DESTINATION_LATITUDE)?.let { latitude ->
                    getQueryParameter(DESTINATION_LONGITUDE)?.let { longitude ->
                        getQueryParameter(DESTINATION_DISPLAY_ADDRESS)?.let { display_address ->
                            val pos = Position(
                                    latitude = latitude.toDouble(), longitude = longitude
                                    .toDouble()
                                              )
                            TripLocationInfo(position = pos, displayAddress = display_address)
                        }
                    }
                }
                tripInfo = TripInfo(tripId = tripId, origin = origin, destination = destination)
            }
        } ?: run { tripInfo = null }
    }

    companion object {
        const val EXTRA_AUTOMATIC_LOGOUT = "SplashActivity.AutomaticLogout"
        private const val SPLASH_SCREEN_DELAY = 2500L
        private const val ORIGIN_LATITUDE = "origin_latitude"
        private const val ORIGIN_LONGITUDE = "origin_longitude"
        private const val DESTINATION_LATITUDE = "destination_latitude"
        private const val DESTINATION_LONGITUDE = "destination_longitude"
        private const val DATE_TIME = "date_time"
        private const val TRIP_ID = "trip_id"
        private const val ORIGIN_DISPLAY_ADDRESS = "origin_display_address"
        private const val DESTINATION_DISPLAY_ADDRESS = "destination_display_address"
    }
}

