package com.karhoo.karhootraveller.presentation.login.password

import com.karhoo.karhootraveller.R
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.user.UserService
import com.karhoo.sdk.call.Call
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ForgotPasswordPresenterTest {

    private val userService: UserService = mock()
    private val view: ForgotPasswordMVP.View = mock()
    private val call: Call<Void> = mock()

    private lateinit var presenter: ForgotPasswordMVP.Presenter

    private val lambdaCaptor = argumentCaptor<(Resource<Void>) -> Unit>()

    @Before
    fun setUp() {
        presenter = ForgotPasswordPresenter(userService, view)
        doNothing().whenever(call).execute(lambdaCaptor.capture())
    }

    /**
     * Given:   A user presses the forgot password button
     * Then:    The view should be told to ask for the users email
     **/
    @Test
    fun `user asked to enter email when user clicks forgot password`() {
        presenter.userForgotPassword()
        verify(view).showEnterEmailDialog()
    }

    /**
     * Given:   A user enters a correct email
     * When:    The response is successful
     * Then:    The view should be told to stop loading
     **/
    @Test
    fun `successful reset stops the view loading`() {
        whenever(userService.resetPassword(any())).thenReturn(call)

        presenter.sendResetLink(EMAIL)
        lambdaCaptor.firstValue.invoke(Resource.Success(data = mock()))

        verify(view).resetEmailSent()
    }

    /**
     * Given:   A user enters a correct email
     * When:    The user does not exist on the system
     * Then:    The view is alerted with the correct message
     **/
    @Test
    fun `error with resetting password shows snackbar with message`() {
        whenever(userService.resetPassword(any())).thenReturn(call)

        presenter.sendResetLink(EMAIL)
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).couldNotSendResetLink(any(), any())
    }

    /**
     * Given:   A user enters a email to reset password
     * When:    There is an issue
     * Then:    The view should be told to stop loading
     **/
    @Test
    fun `error with resetting password dismisses loading`() {
        whenever(userService.resetPassword(any())).thenReturn(call)

        presenter.sendResetLink(EMAIL)
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).dismissLoading()
    }

    /**
     * Given:   A user enters a correct email
     * When:    There is an issue
     * Then:    The view should be told the response was unsuccessful
     **/
    @Test
    fun `error when resetting password alerts the view with the correct message`() {
        whenever(userService.resetPassword(any())).thenReturn(call)

        presenter.sendResetLink(EMAIL)
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.Unexpected))


        verify(view).couldNotSendResetLink(R.string.kh_uisdk_temporary_message_error_codes_unknown, KarhooError.Unexpected)
    }

    companion object {
        private const val EMAIL = "name@email.com"
        private const val ERROR_DESCRIPTION = "we seem to be having trouble at the moment"
    }

}