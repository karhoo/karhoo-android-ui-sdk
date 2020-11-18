package com.karhoo.karhootraveller.presentation.splash.register

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.location.Location
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.karhoo.karhootraveller.BuildConfig
import com.karhoo.karhootraveller.KarhooConfig
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.login.LoginActivity
import com.karhoo.karhootraveller.presentation.register.RegistrationActivity
import com.karhoo.karhootraveller.presentation.splash.domain.KarhooAppVersionValidator
import com.karhoo.karhootraveller.service.analytics.KarhooAnalytics
import com.karhoo.karhootraveller.service.preference.KarhooPreferenceStore
import com.karhoo.karhootraveller.util.playservices.KarhooPlayServicesUtil
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationProvider
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.view_splash.view.loginTypeSpinner
import kotlinx.android.synthetic.main.view_splash.view.registerButton
import kotlinx.android.synthetic.main.view_splash.view.signInButton

class SplashScreenView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), SplashMVP.View, PermissionListener,
      AdapterView.OnItemSelectedListener {

    private var presenter: SplashMVP.Presenter? = null

    var splashActions: SplashActions? = null

    init {
        inflate(context, R.layout.view_splash, this)
        initialiseListeners()
    }

    override fun onResume() {
        askForLocationPermission()
        loginTypeSpinner.setSelection(0)
    }

    private fun initialiseListeners() {
        signInButton.setOnClickListener { goToLogin() }
        registerButton.setOnClickListener { goToRegistration() }
        if (BuildConfig.BUILD_TYPE == "debug") {
            loginTypeSpinner.visibility = VISIBLE
            val loginTypeAdapter = ArrayAdapter<String>(context, android.R.layout
                    .simple_spinner_dropdown_item, LoginType.values().map { it.value })
            with(loginTypeSpinner) {
                adapter = loginTypeAdapter
                onItemSelectedListener = this@SplashScreenView
            }
        } else {
            loginTypeSpinner.visibility = GONE
        }
    }

    override fun goToLogin() {
        val intent = LoginActivity.Builder.builder.build(context)
        splashActions?.startActivity(intent)
    }

    override fun showError() {
        Toast.makeText(context, "Invalid Token User", Toast.LENGTH_LONG)
    }

    private fun goToRegistration() {
        val intent = RegistrationActivity.Builder.builder.build(context)
        splashActions?.startActivityForResult(intent, RegistrationActivity.REQ_CODE)
    }

    override fun setConfig(authenticationMethod: AuthenticationMethod) {
        KarhooUISDK.setConfiguration(KarhooConfig(context.applicationContext, authenticationMethod))
    }

    override fun setLoginRegVisibility(visibility: Boolean) {
        signInButton.visibility = if (visibility) VISIBLE else INVISIBLE
        registerButton.visibility = if (visibility) VISIBLE else INVISIBLE
    }

    override fun saveUsersLocation(latLng: LatLng) {
        val preferences = context.getSharedPreferences(USER_LOCATION_PREF, Context.MODE_PRIVATE)
        val json = Gson().toJson(latLng)
        preferences.edit().putString(USER_LOCATION, json).apply()
    }

    override fun goToBooking(location: Location?) {
        splashActions?.goToBooking(location)
    }

    override fun appInvalid() {
        splashActions?.showErrorWithAction(R.string.app_invalid,
                                           { context.startActivity(Intent(ACTION_VIEW, Uri.parse("market://details?id=" + context.packageName))) },
                                           R.string.update)
    }

    override fun promptUpdatePlayServices(errorCode: Int) {
        val dialog = GoogleApiAvailability.getInstance()
                .getErrorDialog(context as Activity, errorCode, 0)
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.d("Adyen", "On no item selected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val type = parent?.getItemAtPosition(position)

        presenter?.handleLoginTypeSelection(type as String)
    }

    //region Permissions

    override fun onPermissionGranted(response: PermissionGrantedResponse) {
        splashActions?.dismissErrors()

        val fallbackLocation = Location("").apply {
            latitude = context.getString(R.string.central_lat).toDouble()
            longitude = context.getString(R.string.central_long).toDouble()
        }

        presenter = SplashPresenter(this,
                                    KarhooApi.paymentsService,
                                    LocationProvider(context, KarhooApi.addressService),
                                    KarhooApi.userStore, KarhooAppVersionValidator(BuildConfig.VERSION_CODE, KarhooPreferenceStore.getInstance(context)),
                                    KarhooAnalytics.INSTANCE, fallbackLocation, KarhooApi.authService, KarhooPlayServicesUtil(context))
        presenter?.getUsersLocation()
        presenter?.checkIfUserIsLoggedIn()
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse) {
        splashActions?.dismissErrors()
        setRationaleShown(true)
        presenter?.locationUpdatesDenied()
        splashActions?.goFullScreen()
        presenter = SplashPresenter(this,
                                    KarhooApi.paymentsService,
                                    LocationProvider(context, KarhooApi.addressService),
                                    KarhooApi.userStore, KarhooAppVersionValidator(BuildConfig.VERSION_CODE, KarhooPreferenceStore.getInstance(context)),
                                    KarhooAnalytics.INSTANCE, null, KarhooApi.authService, KarhooPlayServicesUtil(context))
        presenter?.checkIfUserIsLoggedIn()
    }

    private fun askForLocationPermission() {
        Dexter.withActivity(context as Activity)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(this)
                .check()
    }

    private fun setRationaleShown(isShown: Boolean) {
        context.getSharedPreferences(context.getString(R.string.permissions), Context.MODE_PRIVATE)
                .edit().putBoolean(context.getString(R.string.location_rationale_shown), isShown)
                .apply()
    }

    private fun rationaleShown(): Boolean {
        return context.getSharedPreferences(context.getString(R.string.permissions), Context.MODE_PRIVATE)
                .getBoolean(context.getString(R.string.location_rationale_shown), false)
    }

    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
        token.continuePermissionRequest()
    }

    companion object {
        const val USER_LOCATION_PREF = "USER_LOCATION"
        const val USER_LOCATION = "users::latlng"
    }
}
