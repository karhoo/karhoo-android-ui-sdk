package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.model.adyen.AdyenPublicKey
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.booking.payment.BookingPaymentMVP
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentDropInMVP
import com.nhaarman.mockitokotlin2.any
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
class AdyenPaymentPresenterTest {

    private var paymentsService: PaymentsService = mock()
    private var userStore: UserStore = mock()
    private var paymentView: PaymentDropInMVP.Actions = mock()
    private var price: QuotePrice = mock()
    private val publicKeyCall: Call<AdyenPublicKey> = mock()
    private val publicKeyCaptor = argumentCaptor<(Resource<AdyenPublicKey>) -> Unit>()
    private val paymentMethodsCall: Call<String> = mock()
    private val paymentMethodsCaptor = argumentCaptor<(Resource<String>) -> Unit>()

    private lateinit var adyenPaymentPresenter: AdyenPaymentPresenter

    @Before
    fun setUp() {
        whenever(paymentsService.getAdyenPublicKey())
                .thenReturn(publicKeyCall)
        doNothing().whenever(publicKeyCall).execute(publicKeyCaptor.capture())

        whenever(paymentsService.getAdyenPaymentMethods(any()))
                .thenReturn(paymentMethodsCall)
        doNothing().whenever(paymentMethodsCall).execute(paymentMethodsCaptor.capture())

        adyenPaymentPresenter = AdyenPaymentPresenter(
                paymentsService = paymentsService,
                userStore = userStore,
                view = paymentView)
    }

    /**
     * Given:   A request is made to change card
     * When:    The public key retrieval fails
     * Then:    Then an error is shown
     */
    @Test
    fun `error shown when change card pressed and public key retrieval fails`() {

        adyenPaymentPresenter.sdkInit(price)

        publicKeyCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))

        verify(paymentsService, never()).getAdyenPaymentMethods(any())
        verify(paymentsService).getAdyenPublicKey()
        verify(paymentView).showError(R.string.something_went_wrong)
    }

    /**
     * Given:   A request is made to change card
     * When:    The public key retrieval succeeds
     * And:     The payment methods retrieval fails
     * Then:    Then an error is shown
     */
    @Test
    fun `error shown when change card pressed and payment methods retrieval fails`() {

        adyenPaymentPresenter.sdkInit(price)

        publicKeyCaptor.firstValue.invoke(Resource.Success(adyenPublicKey))
        publicKeyCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))

        verify(paymentsService).getAdyenPublicKey()
        verify(paymentsService).getAdyenPaymentMethods(any())
        verify(paymentView).showError(R.string.something_went_wrong)
    }

    /**
     * Given:   A request is made to change card
     * When:    The public key retrieval succeeds
     * And:     The payment methods retrieval fails
     * Then:    Then an error is shown
     */
    @Test
    fun `payment view shown when change card pressed and payment methods retrieved successfully`() {
        val paymentData: String = "{paymentMethods: []}"

        adyenPaymentPresenter.sdkInit(price)

        publicKeyCaptor.firstValue.invoke(Resource.Success(adyenPublicKey))
        paymentMethodsCaptor.firstValue.invoke(Resource.Success(paymentData))

        verify(paymentsService).getAdyenPublicKey()
        verify(paymentsService).getAdyenPaymentMethods(any())
        verify(paymentView).showPaymentUI(adyenPublicKey.publicKey, paymentData, price)
    }

    companion object {

        private val adyenPublicKey: AdyenPublicKey = AdyenPublicKey("12345678")
    }
}