package com.karhoo.uisdk.screen.booking.checkout.payment

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.PaymentProvider
import com.karhoo.sdk.api.model.Provider
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.ADYEN
import com.karhoo.uisdk.util.BRAINTREE
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BookingPaymentPresenterTest {

    private var paymentsService: PaymentsService = mock()
    private var userStore: UserStore = mock()
    private var view: BookingPaymentContract.View = mock()
    private val paymentProviderCall: Call<PaymentProvider> = mock()
    private val paymentProviderCaptor = argumentCaptor<(Resource<PaymentProvider>) -> Unit>()

    private lateinit var presenter: BookingPaymentPresenter

    @Before
    fun setUp() {
        whenever(paymentsService.getPaymentProvider())
                .thenReturn(paymentProviderCall)
        doNothing().whenever(paymentProviderCall).execute(paymentProviderCaptor.capture())

        presenter = BookingPaymentPresenter(view, userStore, paymentsService)
    }

    /**
     * Given:   A call is made to retrieve the payment provider
     * When:    There is a stored provider
     * Then:    Then the call is not made
     */
    @Test
    fun `provider call not made if there is a stored provider`() {
        whenever(userStore.paymentProvider).thenReturn(PaymentProvider(Provider(ADYEN), null))

        presenter.getPaymentProvider()

        verify(paymentsService, never()).getPaymentProvider()
        verify(view).bindDropInView()
    }

    /**
     * Given:   A call is made to retrieve the payment provider
     * And:     The provider is not stored
     * When:    The call fails
     * Then:    An error is shown
     */
    @Test
    fun `error shown when get provider call fails`() {
        presenter.getPaymentProvider()

        paymentProviderCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))

        verify(view).showError(R.string.kh_uisdk_something_went_wrong, KarhooError.InternalSDKError)
    }

    /**
     * Given:   A call is made to retrieve the payment provider
     * And:     The provider is not stored
     * When:    A provider is successfully retrieved
     * Then:    The drop in view is bound
     */
    @Test
    fun `drop in view bound when get provider call succeeds`() {
        presenter.getPaymentProvider()

        paymentProviderCaptor.firstValue.invoke(Resource.Success(PaymentProvider(adyenProvider,
                                                                                 null)))

        verify(view).bindDropInView()
    }

    companion object {
        private val adyenProvider = Provider(id = ADYEN)
        private val braintreeProvider = Provider(id = BRAINTREE)
    }
}
