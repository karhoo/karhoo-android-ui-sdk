package com.karhoo.karhootraveller.presentation.register.registration

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.user.UserService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.analytics.Analytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RegistrationPresenterTest {

    private var view: RegistrationMVP.View = mock()
    private var userService: UserService = mock()
    private var userInfo: UserInfo = mock()
    private var call: Call<UserInfo> = mock()
    private var analytics: Analytics = mock()

    private val lambdaCaptor = argumentCaptor<(Resource<UserInfo>) -> Unit>()

    @InjectMocks
    private lateinit var presenter: RegistrationPresenter

    @Before
    fun setUp() {
        doNothing().whenever(call).execute(lambdaCaptor.capture())
    }

    /**
     * Given    a boolean to see if registration is possible
     * When     registration isn't possible
     * then     the view should be called telling it registration isnt possible
     *
     */
    @Test
    fun `tell the view registration is not possible`() {
        presenter.setRegistrationMode(false)
        verify(view).registrationPossible(false)
    }

    /**
     * Given    a boolean to see if registration is possible
     * When     registration is possible
     * then     the view should be called telling it registration is possible
     *
     */
    @Test
    fun `tell the view registration is possible`() {
        presenter.setRegistrationMode(true)
        verify(view).registrationPossible(true)
    }

    /**
     * Given:   A mobile number
     * When:    Registering a user
     * Then:    The number should have a prefix 0 removed and country code added
     *
     */
    @Test
    fun `create international phone number from number`() {
        val number = presenter.validateMobileNumber("+44", "01234567")
        Assert.assertEquals(number, "+441234567")
    }

    /**
     * Given:   A user has decided to register
     * When:    They sign up with valid credentials
     * Then:    The button should disable while registering
     * And:     The view should be told registration was successful
     */
    @Test
    fun `registering valid user disables register button`() {
        whenever(userService.register(any())).thenReturn(call)

        presenter.registerUser()
        presenter.completeRegistration(userInfo, "12345678", userService)
        lambdaCaptor.firstValue.invoke(Resource.Success(userInfo))

        verify(view).registrationPossible(false)
        verify(view).userRegistered(userInfo)
    }

    /**
     * Given:   A user has decided to register
     * When:    They sign up with valid credentials
     * Then:    The button should disable while registering
     * And:     The view should be told registration was successful
     */
    @Test
    fun `registering valid user fires registration complete`() {
        whenever(userService.register(any())).thenReturn(call)

        presenter.registerUser()
        presenter.completeRegistration(userInfo, "12345678", userService)
        lambdaCaptor.firstValue.invoke(Resource.Success(userInfo))

        verify(analytics).registrationComplete()
        verify(view).userRegistered(userInfo)
    }

    /**
     * Given:   A user has decided to register
     * When:    They sign up with valid credentials
     * Then:    The button should disable while registering
     * And:     The view should be told registration was successful
     */
    @Test
    fun `registering valid user but returns NOT ENABLED error still fires registration complete`() {
        val notEnabledError = KarhooError.RequiredRolesNotAvailable
        whenever(userService.register(any())).thenReturn(call)

        presenter.registerUser()
        presenter.completeRegistration(userInfo, "12345678", userService)
        lambdaCaptor.firstValue.invoke(Resource.Failure(notEnabledError))

        verify(analytics).registrationComplete()
        verify(view).userRegistered(userInfo)
    }

    /**
     * Given:   A user has decided to register
     * When:    They sign up with invalid credentials
     * Then:    The button should reshow when there is an error
     *
     */
    @Test
    fun `registering valid user disables register button until registration fails`() {
        whenever(userService.register(any())).thenReturn(call)

        presenter.registerUser()
        presenter.completeRegistration(userInfo, "12345678", userService)
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).registrationPossible(false)
        verify(view).registrationPossible(true)
    }

    /**
     * Given:   A user is able to register
     * When:    Signing up to the a platform
     * Then:    The presenter should tell the view that the user is now registering
     *
     */
    @Test
    fun `registering valid user tells the view`() {
        presenter.registerUser()
        verify(view).userRegistering()
    }

    /**
     * Given:   The presenter is told registration has started
     * When:    Signing up to the a platform
     * Then:    The presenter should fire event saying registration has begun
     *
     */
    @Test
    fun `registering valid user fires reg started event`() {
        presenter.registrationStarted()
        verify(analytics).registrationStarted()
    }

    /**
     * Given:   A user presses go to terms
     * When:    The call has been made
     * Then:    An analytical call should be made
     */
    @Test
    fun `go to terms fires event`() {
        presenter.goToTerms()
        verify(analytics).termsReviewed()
    }

    /**
     * Given:   A user presses go to privacy
     * When:    The call has been made
     * Then:    An analytical call should be made
     */
    @Test
    fun `go to privacy fires event`() {
        presenter.goToPrivacy()
        verify(analytics).termsReviewed()
    }

}
