package com.karhoo.karhootraveller.presentation.login.signin

import com.karhoo.karhootraveller.presentation.splash.domain.AppVersionValidator
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.user.UserService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.analytics.Analytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LoginPresenterTest {

    private var userInfo = UserInfo(userId = "1234",
                                    firstName = "Sammy",
                                    lastName = "Ruffler",
                                    email = "yoyo@yoyo.yoyo",
                                    phoneNumber = "12345678")

    private var userService: UserService = mock()
    private var analyticsManager: Analytics = mock()
    private var appVersionValidator: AppVersionValidator = mock()
    private var view: LoginMVP.View = mock()
    private var call: Call<UserInfo> = mock()

    private val lambdaCaptor = argumentCaptor<(Resource<UserInfo>) -> Unit>()

    @InjectMocks
    private lateinit var loginPresenter: LoginPresenter

    @Before
    fun setUp() {
        doNothing().whenever(call).execute(lambdaCaptor.capture())
    }

    /**
     * Given:   A user has filled out there details successfully
     * When:    The user presses the login button
     * Then:    A call should be made to the login service to log the user in
     *
     */
    @Test
    fun `login user with valid credentials`() {
        whenever(userService.loginUser(any())).thenReturn(call)
        doNothing().whenever(call).execute(any())
        loginPresenter.loginUser("fullname@email.com", "password123")
        verify(userService).loginUser(any())
    }

    /**
     * Given:   A user has filled out there details successfully
     * When:    The user presses the login button
     * Then:    A successful login should call the view
     */
    @Test
    fun `login user with valid creds tells view`() {
        whenever(userService.loginUser(any())).thenReturn(call)

        loginPresenter.loginUser("fullname@email.com", "password123")
        lambdaCaptor.firstValue.invoke(Resource.Success(userInfo))

        verify(view).loginSuccessful()
    }

    /**
     * Given:   A user has filled out there details successfully
     * When:    The user presses the login button
     * Then:    A successful login should fire analytics
     */
    @Test
    fun `login user with valid creds tells fires analytics`() {
        whenever(userService.loginUser(any())).thenReturn(call)

        loginPresenter.loginUser("fullname@email.com", "password123")
        lambdaCaptor.firstValue.invoke(Resource.Success(userInfo))

        verify(analyticsManager).userLoggedIn(userInfo)
    }

    /**
     * Given:   A user has filled out there details badly
     * When:    The user presses the login button
     * Then:    A error should be returned
     */
    @Test
    fun `login user with invalid creds returns error`() {
        whenever(userService.loginUser(any())).thenReturn(call)

        loginPresenter.loginUser("fullname@email.com", "password123")
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view, atLeastOnce()).onError()
    }

    /**
     * Given:   A user has filled out there details badly
     * When:    The presenter is asked to enable the login button
     * Then:    A view should be given a false to say the details aren't valid
     */
    @Test
    fun `validiating fields with bad value turns login button disabled`() {
        loginPresenter.setLoginMode(false)
        verify(view).enableLogin(false)
    }

    /**
     * Given:   A user has filled out there details correctly
     * When:    The presenter is asked to enable the login button
     * Then:    A view should be given a true to say the details are valid
     */
    @Test
    fun `validiating fields with good values turns login button enabled`() {
        loginPresenter.setLoginMode(true)
        verify(view).enableLogin(true)
    }

    /**
     * Given:   A user has filled out there details badly
     * When:    The presenter is asked to enable the login button
     * Then:    A view should be told there was an error
     */
    @Test
    fun `validiating fields with bad value turns error on password`() {
        whenever(userService.loginUser(any())).thenReturn(call)

        loginPresenter.loginUser("fullname@email.com", "password123")
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).onError()
    }

}