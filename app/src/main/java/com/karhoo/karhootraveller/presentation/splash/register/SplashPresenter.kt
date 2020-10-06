package com.karhoo.karhootraveller.presentation.splash.register

import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.model.LatLng
import com.karhoo.karhootraveller.BuildConfig
import com.karhoo.karhootraveller.presentation.base.BasePresenter
import com.karhoo.karhootraveller.presentation.splash.domain.AppVersionValidator
import com.karhoo.karhootraveller.util.logoutAndResetApp
import com.karhoo.karhootraveller.util.playservices.PlayServicesUtil
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.network.request.NonceRequest
import com.karhoo.sdk.api.network.request.Payer
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationProvider
import com.karhoo.uisdk.screen.booking.domain.userlocation.PositionListener

@Suppress("LongParameterList", "ComplexCondition")
internal class SplashPresenter(view: SplashMVP.View,
                               private val paymentService: PaymentsService,
                               private val locationProvider: LocationProvider,
                               private val userStore: UserStore,
                               private val appVersionValidator: AppVersionValidator,
                               private val analytics: Analytics,
                               private var location: Location?,
                               private val playServicesUtil: PlayServicesUtil)
    : BasePresenter<SplashMVP.View>(),
      SplashMVP.Presenter,
      AppVersionValidator.Listener, PositionListener {

    private var isAppValid: Boolean = false
    private var isTokenValid: Boolean = false
    private var alreadyProceeding: Boolean = false

    init {
        attachView(view)
    }

    override fun checkIfUserIsLoggedIn() {
        when (val available = playServicesUtil.playServicesUpToDate()) {
            ConnectionResult.SUCCESS -> {
                appVersionValidator.isCurrentVersionValid(this)
                view?.setLoginRegVisibility(!userStore.isCurrentUserValid)
            }
            else -> view?.promptUpdatePlayServices(available)
        }
    }

    private fun setDefaultLocation() {
        location?.let {
            handler.postDelayed({ onPositionUpdated(it) }, SPLASH_SCREEN_LOCATION_DELAY)
        }
    }

    override fun getUsersLocation() {
        setDefaultLocation()
        locationProvider.listenForLocations(this, numberOfUpdates = 1)
    }

    override fun onPositionUpdated(location: Location) {
        handler.removeCallbacksAndMessages(null)
        this.location = location
        view?.saveUsersLocation(LatLng(location.latitude, location.longitude))
        proceedIfAble()
    }

    override fun onLocationServicesDisabled() {
        // Do nothing
    }

    override fun locationUpdatesDenied() {
        analytics.locationServiceRejected()
    }

    override fun onResolutionRequired(resolvableApiException: ResolvableApiException) {
        // Do nothing
    }

    override fun isAppValid(isValid: Boolean) {
        if (isValid) {
            isAppValid = true
            proceedIfAble()
            location?.let { onPositionUpdated(it) }
        } else {
            view?.setLoginRegVisibility(false)
            view?.appInvalid()
        }
    }

    override fun isTokenValid(isValid: Boolean) {
        if (isValid) {
            isTokenValid = true
            proceedIfAble()
        } else if (userStore.isCurrentUserValid) {
            logoutAndResetApp(isAutomaticLogout = true)
        }
    }

    override fun handleLoginTypeSelection(loginType: String) {
        Log.d("Adyen", userStore.paymentProvider.toString())
        userStore.removeCurrentUser()
        Log.d("Adyen", userStore.paymentProvider.toString())
        val authMethod: AuthenticationMethod = when (loginType) {
            LoginType.USERNAME_PASSWORD.value -> AuthenticationMethod.KarhooUser()
            LoginType.ADYEN_GUEST.value -> AuthenticationMethod.Guest(identifier = BuildConfig.ADYEN_GUEST_CHECKOUT_IDENTIFIER,
                                                                referer = BuildConfig.GUEST_CHECKOUT_REFERER,
                                                                organisationId = BuildConfig.ADYEN_GUEST_CHECKOUT_ORGANISATION_ID)
            LoginType.BRAINTREE_GUEST.value -> AuthenticationMethod.Guest(identifier = BuildConfig.BRAINTREE_GUEST_CHECKOUT_IDENTIFIER,
                                                                    referer = BuildConfig.GUEST_CHECKOUT_REFERER,
                                                                    organisationId = BuildConfig.BRAINTREE_GUEST_CHECKOUT_ORGANISATION_ID)
            LoginType.ADYEN_TOKEN.value -> AuthenticationMethod.KarhooUser()
            // TODO Implement with clientId and scope
            // AuthenticationMethod.TokenExchange(clientId = BuildConfig.ADYEN_CLIENT_ID,
            // scope = BuildConfig.ADYEN_CLIENT_SCOPE)
            LoginType.BRAINTREE_TOKEN.value -> AuthenticationMethod.KarhooUser()
            // TODO Implement with clientId and scope
            // AuthenticationMethod.TokenExchange(clientId = BuildConfig.BRAINTREE_CLIENT_ID,
            // scope = BuildConfig.BRAINTREE_CLIENT_SCOPE)
            else -> return
        }
        view?.setConfig(authMethod)
        if (authMethod is AuthenticationMethod.KarhooUser) {
            view?.goToLogin()
        } else {
            view?.goToBooking(null)
        }
    }

    private fun proceedIfAble() {
        if (userStore.isCurrentUserValid && isAppValid && isTokenValid && !alreadyProceeding) {
            refreshUserNonce()
            alreadyProceeding = true
            view?.goToBooking(location)
        }
    }

    private fun refreshUserNonce() {
        userStore.currentUser.let {
            val payer = Payer(id = it.userId,
                              email = it.email,
                              firstName = it.firstName,
                              lastName = it.lastName)

            val getNonceRequest = NonceRequest(payer = payer,
                                               organisationId = it.organisations[0].id)

            paymentService.getNonce(request = getNonceRequest).execute { }
        }
    }

    companion object {
        private val handler = Handler(Looper.getMainLooper())
        private const val SPLASH_SCREEN_LOCATION_DELAY = 8000L
    }

}
