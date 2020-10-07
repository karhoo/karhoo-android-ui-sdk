package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.model.adyen.AdyenPublicKey
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.UnitTestUISDKConfig
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentDropInMVP
import com.karhoo.uisdk.util.DEFAULT_CURRENCY
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AdyenPaymentPresenterTest {

    private var context: Context = mock()
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

    /**
     * Given:   A payment nonce retrieval is attempted
     * When:    There is no nonce set
     * Then:    Then an error is shown
     */
    @Test
    fun `error shown when retrieval of a nonce is attempted and it is null`() {

        adyenPaymentPresenter.getPaymentNonce(price)

        verify(paymentView).showError(R.string.payment_issue_message)
    }

    /**
     * Given:   A payment nonce retrieval is attempted
     * When:    There is a nonce set
     * Then:    Then an error is shown
     */
    @Test
    fun `nonce retrieved for 3ds when retrieval is attempted and it is not null`() {
        KarhooUISDKConfigurationProvider.setConfig(
                configuration = UnitTestUISDKConfig(
                        context = context,
                        authenticationMethod = AuthenticationMethod.Guest("identifier", "referer", "guestOrganisationId"),
                        handleBraintree = false))

        whenever(price.currencyCode).thenReturn(DEFAULT_CURRENCY)
        whenever(price.highPrice).thenReturn(100)

        val payload = JSONObject()
                .put(RESULT_CODE, AUTHORISED)
                .put(MERCHANT_REFERENCE, TRANSACTION_ID).toString()

        val data: Intent = mock()
        whenever(data.getStringExtra(RESULT_KEY)).thenReturn(payload)

        adyenPaymentPresenter.handleActivityResult(
                requestCode = 1,
                resultCode = AppCompatActivity.RESULT_OK,
                data = data)

        adyenPaymentPresenter.getPaymentNonce(price)

        verify(paymentView).threeDSecureNonce(TRANSACTION_ID, TRANSACTION_ID, "1.00")
    }

    companion object {
        private val adyenPublicKey: AdyenPublicKey = AdyenPublicKey("12345678")
        private const val AUTHORISED = AdyenPaymentView.AUTHORISED
        private const val TRANSACTION_ID = "1234"
        private const val MERCHANT_REFERENCE = AdyenPaymentView.MERCHANT_REFERENCE
        private const val RESULT_KEY = AdyenResultActivity.RESULT_KEY
        private const val RESULT_CODE = AdyenPaymentView.RESULT_CODE
    }
}