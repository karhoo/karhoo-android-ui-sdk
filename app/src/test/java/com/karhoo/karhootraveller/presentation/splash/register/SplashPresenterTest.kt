package com.karhoo.karhootraveller.presentation.splash.register

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.karhoo.karhootraveller.BuildConfig
import com.karhoo.karhootraveller.presentation.splash.domain.AppVersionValidator
import com.karhoo.karhootraveller.util.playservices.PlayServicesUtil
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.Organisation
import com.karhoo.sdk.api.model.PaymentsNonce
import com.karhoo.sdk.api.model.Provider
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.auth.AuthService
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationProvider
import com.karhoo.uisdk.screen.booking.domain.userlocation.PositionListener
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SplashPresenterTest {

    private val view: SplashMVP.View = mock()
    private val locationProvider: LocationProvider = mock()
    private val userStore: UserStore = mock()
    private val appVersionValidator: AppVersionValidator = mock()
    private val analytics: Analytics = mock()
    private val playServicesUtil: PlayServicesUtil = mock()
    private val paymentService: PaymentsService = mock()
    private val paymentCall: Call<PaymentsNonce> = mock()
    private val authService: AuthService = mock()
    private var call: Call<UserInfo> = mock()
    private val location: Location = Location("").apply {
        latitude = 1.0
        longitude = 2.0
    }

    private val lambdaCaptor = argumentCaptor<(Resource<UserInfo>) -> Unit>()

    private val authMethodCaptor = argumentCaptor<AuthenticationMethod>()

    private val presenter: SplashPresenter = SplashPresenter(view, paymentService, locationProvider, userStore,
                                                             appVersionValidator, analytics,
                                                             location, authService, playServicesUtil)

    @Before
    fun setUp() {
        doNothing().whenever(call).execute(lambdaCaptor.capture())
    }

    /**
     * Given:   The app has started
     * When:    The splash screen is shown
     * Then:    A call should be made to get users location
     */
    @Test
    fun locationRetrievedWhenSplashIsLoading() {
        doAnswer {
            (it.arguments[0] as PositionListener).onPositionUpdated(location)
        }.whenever(locationProvider).listenForLocations(any(), anyInt())

        presenter.getUsersLocation()

        verify(view).saveUsersLocation(LatLng(location.latitude, location.longitude))
    }

    /**
     * Given:   The app has started
     * When:    The user has denied location updates
     * Then:    An analytical call should be made
     */
    @Test
    fun locationDeniedFiresEvent() {
        presenter.locationUpdatesDenied()

        verify(analytics).locationServiceRejected()
    }

    /**
     * Given:   The app has started
     * When:    The user is logged in
     * Then:    The registration and sign in buttons should be gone
     */
    @Test
    fun loggedInUserHidesRegistrationButtons() {
        whenever(playServicesUtil.playServicesUpToDate()).thenReturn(0)
        whenever(userStore.isCurrentUserValid).thenReturn(true)

        presenter.checkIfUserIsLoggedIn()

        verify(view, atLeastOnce()).setLoginRegVisibility(false)
    }

    /**
     * Given:   The app has started
     * When:    The user is not logged in
     * Then:    The registration and sign in buttons should be gone
     */
    @Test
    fun loggedOutUserShowsRegistrationButtons() {
        whenever(playServicesUtil.playServicesUpToDate()).thenReturn(0)
        whenever(userStore.isCurrentUserValid).thenReturn(false)
        doNothing().whenever(appVersionValidator).isCurrentVersionValid(presenter)

        presenter.checkIfUserIsLoggedIn()

        verify(view, atLeastOnce()).setLoginRegVisibility(true)
    }

    /**
     * Given:   The app has started
     * When:    The app version is valid
     * Then:    A call should be made to proceed
     */
    @Test
    fun ifAppIsValidThenAppShouldTryToProceedToBooking() {
        presenter.isAppValid(true)

        verify(userStore, atLeastOnce()).isCurrentUserValid
    }

    /**
     * Given:   The app has started
     * When:    The app version is invalid
     * Then:    A call should be made to update
     */
    @Test
    fun ifAppIsInvalidThenAppShouldAlertUser() {
        presenter.isAppValid(false)

        verify(view).appInvalid()
    }

    /**
     * Given:   The app has started
     * When:    The all details are valid AND Payment Provider is Braintree
     * Then:    A call should be made to proceed
     * And:     Get nonce should be called ONCE
     */
    @Test
    fun allDetailsValidAllowTheUserIntoTheAppBraintree() {
        whenever(userStore.isCurrentUserValid).thenReturn(true)
        whenever(userStore.currentUser).thenReturn(userInfo)
        whenever(userStore.paymentProvider).thenReturn(paymentProviderBraintree)
        whenever(paymentService.getNonce(any())).thenReturn(paymentCall)

        presenter.isAppValid(true)
        presenter.isTokenValid(true)

        verify(paymentService, times(1)).getNonce(any())
        verify(view).goToBooking(any())
    }

    /**
     * Given:   The app has started
     * When:    The all details are valid AND Payment Provider is Adyen
     * Then:    A call should be made to proceed
     * And:     Get nonce should NOT be called
     */
    @Test
    fun allDetailsValidAllowTheUserIntoTheAppAdyen() {
        whenever(userStore.isCurrentUserValid).thenReturn(true)
        whenever(userStore.paymentProvider).thenReturn(paymentProviderAdyen)

        presenter.isAppValid(true)
        presenter.isTokenValid(true)

        verify(paymentService, never()).getNonce(any())
        verify(view).goToBooking(any())
    }

    /**
     * Given:   The app has started
     * When:    The google play services lib is out of date
     * Then:    A dialog should be shown to the user
     */
    @Test
    fun googlePlayServicesDialogShowsIfOutOfDate() {
        whenever(playServicesUtil.playServicesUpToDate()).thenReturn(1)

        presenter.checkIfUserIsLoggedIn()

        verify(view).promptUpdatePlayServices(1)
    }

    /**
     * Given:   A login type has been chosen
     * When:    When it is an Adyen guest login
     * Then:    The correct auth method is set
     * And:     The user is taken to the booking flow
     */
    @Test
    fun `Adyen guest login sets correct auth method and takes user to booking flow`() {
        presenter.handleLoginTypeSelection(LoginType.ADYEN_GUEST.value)

        verify(view).setConfig(authMethodCaptor.capture())
        assertTrue(authMethodCaptor.firstValue is AuthenticationMethod.Guest)
        verify(view).goToBooking(null)
    }

    /**
     * Given:   A login type has been chosen
     * When:    When it is an Braintree guest login
     * Then:    The correct auth method is set
     * And:     The user is taken to the booking flow
     */
    @Test
    fun `Braintree guest login sets correct auth method and takes user to booking flow`() {
        presenter.handleLoginTypeSelection(LoginType.BRAINTREE_GUEST.value)

        verify(view).setConfig(authMethodCaptor.capture())
        assertTrue(authMethodCaptor.firstValue is AuthenticationMethod.Guest)
        verify(view).goToBooking(null)
    }

    /**
     * Given:   A login type has been chosen
     * When:    When it is a Adyen token login
     * Then:    The correct auth method is set
     * And:     The user is taken to the booking flow
     */
    @Test
    fun `Adyen token login sets correct auth method and takes user to booking flow`() {
        whenever(authService.login(BuildConfig.ADYEN_AUTH_TOKEN)).thenReturn(call)
        presenter.handleLoginTypeSelection(LoginType.ADYEN_TOKEN.value)
        lambdaCaptor.firstValue.invoke(Resource.Success(userInfo))

        verify(view).setConfig(authMethodCaptor.capture())
        assertTrue(authMethodCaptor.firstValue is AuthenticationMethod.TokenExchange)
        verify(view).goToBooking(null)
    }

    /**
     * Given:   A login type has been chosen
     * When:    When it is a Braintree token login
     * Then:    The correct auth method is set
     * And:     The user is taken to the booking flow
     */
    @Test
    fun `Braintree token login sets correct auth method and takes user to booking flow`() {
        whenever(authService.login(BuildConfig.BRAINTREE_AUTH_TOKEN)).thenReturn(call)
        presenter.handleLoginTypeSelection(LoginType.BRAINTREE_TOKEN.value)
        lambdaCaptor.firstValue.invoke(Resource.Success(userInfo))

        verify(view).setConfig(authMethodCaptor.capture())
        assertTrue(authMethodCaptor.firstValue is AuthenticationMethod.TokenExchange)
        verify(view).goToBooking(null)
    }

    /**
     * Given:   A login type has been chosen
     * When:    When it is a Adyen token login
     * Then:    A invalid token is passed through
     * And:     An error is presented on screen
     */
    @Test
    fun `Adyen token login failure with invalid token`() {
        whenever(authService.login(BuildConfig.ADYEN_AUTH_TOKEN)).thenReturn(call)
        presenter.handleLoginTypeSelection(LoginType.ADYEN_TOKEN.value)
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).setConfig(authMethodCaptor.capture())
        assertTrue(authMethodCaptor.firstValue is AuthenticationMethod.TokenExchange)
        verify(view, atLeastOnce()).showError(KarhooError.GeneralRequestError)
    }

    /**
     * Given:   A login type has been chosen
     * When:    When it is a Braintree token login
     * Then:    A invalid token is passed through
     * And:     An error is presented on screen
     */
    @Test
    fun `Braintree token login failure with invalid token`() {
        whenever(authService.login(BuildConfig.BRAINTREE_AUTH_TOKEN)).thenReturn(call)
        presenter.handleLoginTypeSelection(LoginType.BRAINTREE_TOKEN.value)
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).setConfig(authMethodCaptor.capture())
        assertTrue(authMethodCaptor.firstValue is AuthenticationMethod.TokenExchange)
        verify(view, atLeastOnce()).showError(KarhooError.GeneralRequestError)
    }

    /**
     * Given:   A login type has been chosen
     * When:    When it is a username and password
     * Then:    The correct auth method is set
     * And:     The user is taken to the booking flow
     */
    @Test
    fun `Username and password login sets correct auth method and takes user to booking flow`() {
        presenter.handleLoginTypeSelection(LoginType.USERNAME_PASSWORD.value)

        verify(view).setConfig(authMethodCaptor.capture())
        assertTrue(authMethodCaptor.firstValue is AuthenticationMethod.KarhooUser)
        verify(view).goToLogin()
    }

    /**
     * Given:   A login type has been chosen
     * When:    When it is a username and password
     * Then:    The correct auth method is set
     * And:     The user is taken to the booking flow
     */
    @Test
    fun `Invalid login type does not set the configuration or take the user to the booking flow`() {
        presenter.handleLoginTypeSelection(LoginType.USERNAME_PASSWORD.value)

        verify(view).setConfig(authMethodCaptor.capture())
        assertTrue(authMethodCaptor.firstValue is AuthenticationMethod.KarhooUser)
        verify(view, never()).goToBooking(null)
    }

    companion object {

        val userInfo = UserInfo(
                firstName = "David",
                lastName = "Smith",
                email = "test.test@test.test",
                phoneNumber = "+441234 56789",
                userId = "123",
                locale = "en-GB",
                organisations = listOf(Organisation(id = "organisation_id", name = "Organisation", roles = listOf("PERMISSION_ONE", "PERMISSION_TWO"))))

        val paymentProviderBraintree = Provider(id = "braintree", loyalty = listOf())
        val paymentProviderAdyen = Provider(id = "adyen", loyalty = listOf())

    }

}