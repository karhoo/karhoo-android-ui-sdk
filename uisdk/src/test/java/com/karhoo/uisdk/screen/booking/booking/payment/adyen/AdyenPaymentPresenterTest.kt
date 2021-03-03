package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.CardType
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AdyenPaymentPresenterTest {

    private var context: Context = mock()
    private var data: Intent = mock()
    private var paymentsService: PaymentsService = mock()
    private var userStore: UserStore = mock()
    private var price: QuotePrice = mock()
    private var savedPaymentInfo: SavedPaymentInfo = mock()
    private var paymentDropInActions: PaymentDropInMVP.Actions = mock()

    private val paymentInfoCaptor = argumentCaptor<SavedPaymentInfo>()
    private val karhooErrorCaptor = argumentCaptor<KarhooError>()
    private val paymentMethodsCall: Call<String> = mock()
    private val paymentMethodsCaptor = argumentCaptor<(Resource<String>) -> Unit>()
    private val publicKeyCall: Call<AdyenPublicKey> = mock()
    private val publicKeyCaptor = argumentCaptor<(Resource<AdyenPublicKey>) -> Unit>()

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
                view = paymentDropInActions)
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
        verify(paymentDropInActions).showError(R.string.something_went_wrong, KarhooError.InternalSDKError)
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
        paymentMethodsCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))

        verify(paymentsService).getAdyenPublicKey()
        verify(paymentsService).getAdyenPaymentMethods(any())
        verify(paymentDropInActions).showError(R.string.something_went_wrong, KarhooError.InternalSDKError)
    }

    /**
     * Given:   A request is made to change card
     * When:    The public key retrieval succeeds
     * And:     The payment methods retrieval fails
     * Then:    Then an error is shown
     */
    @Test
    fun `payment view shown when change card pressed and payment methods retrieved successfully`() {
        val paymentData = "{paymentMethods: []}"
        setConfig()

        adyenPaymentPresenter.sdkInit(price)

        publicKeyCaptor.firstValue.invoke(Resource.Success(adyenPublicKey))
        paymentMethodsCaptor.firstValue.invoke(Resource.Success(paymentData))

        verify(paymentsService).getAdyenPublicKey()
        verify(paymentsService).getAdyenPaymentMethods(any())
        verify(paymentDropInActions).showPaymentUI(adyenPublicKey.publicKey, paymentData, price)
    }

    /**
     * Given:   A payment nonce retrieval is attempted
     * When:    There is no nonce set
     * Then:    Then an error is shown
     */
    @Test
    fun `error shown when retrieval of a nonce is attempted and it is null`() {
        setConfig()

        whenever(price.currencyCode).thenReturn(DEFAULT_CURRENCY)
        whenever(price.highPrice).thenReturn(100)

        adyenPaymentPresenter.getPaymentNonce(price)

        verify(paymentDropInActions).showError(R.string.something_went_wrong, KarhooError.FailedToCallMoneyService)
    }

    /**
     * Given:   A payment nonce retrieval is attempted
     * When:    There is a nonce set
     * Then:    Then an error is shown
     */
    @Test
    fun `nonce retrieved for 3ds when retrieval is attempted and it is not null`() {
        setConfig()

        whenever(price.currencyCode).thenReturn(DEFAULT_CURRENCY)
        whenever(price.highPrice).thenReturn(100)

        setMockNonce()

        adyenPaymentPresenter.getPaymentNonce(price)

        verify(paymentDropInActions).threeDSecureNonce(TRIP_ID, TRIP_ID, "1.00")
    }

    /**
     * Given:   An activity result is handled
     * When:    The result is not RESULT_OK
     * Then:    Then an error is shown
     */
    @Test
    fun `error shown is activity result is not RESULT_OK or RESULT_CANCELLED`() {
        adyenPaymentPresenter.handleActivityResult(
                requestCode = REQUEST_CODE,
                resultCode = AppCompatActivity.RESULT_CANCELED,
                data = null)

        verify(paymentDropInActions, never()).showPaymentFailureDialog()
        verify(paymentDropInActions).refresh()
    }

    /**
     * Given:   An activity result is handled
     * When:    The result is RESULT_OK
     * And:     There is no data
     * Then:    Then an error is shown
     */
    @Test
    fun `error shown is activity result is RESULT_OK but there is no data`() {
        adyenPaymentPresenter.handleActivityResult(
                requestCode = REQUEST_CODE,
                resultCode = AppCompatActivity.RESULT_OK,
                data = null)

        verify(paymentDropInActions).showPaymentFailureDialog()
    }

    /**
     * Given:   An activity result is handled
     * When:    The result is RESULT_OK
     * And:     The result code is not AUTHORISED
     * Then:    Then an error is shown
     */
    @Test
    fun `error shown is activity result is RESULT_OK and the result code is not authorised`() {
        val response = """
            {
                "additionalData": {
                  "cardSummary": "4305",
                  "fundingSource": "CREDIT",
                  "paymentMethod": "visa",
                  "recurringProcessingModel": "Subscription"
                },
                "merchantReference": "0d10e9f0-f000-4a22-95bc-bfc6f8530e37/preauth/0",
                "pspReference": "852614786431484C",
                "refusalReason": "FRAUD-CANCELLED",
                "refusalReasonCode": "22",
                "resultCode": "Cancelled"
            }
        """.trimIndent()
        whenever(data.getStringExtra(RESULT_KEY)).thenReturn(response)
        adyenPaymentPresenter.handleActivityResult(
                requestCode = REQUEST_CODE,
                resultCode = AppCompatActivity.RESULT_OK,
                data = data)

        verify(paymentDropInActions).showPaymentFailureDialog(karhooErrorCaptor.capture())
        assertEquals(karhooErrorCaptor.firstValue.code, "KSDK00 Cancelled")
        assertEquals(karhooErrorCaptor.firstValue.internalMessage, "22")
        assertEquals(karhooErrorCaptor.firstValue.userFriendlyMessage, "FRAUD-CANCELLED")
    }

    /**
     * Given:   An activity result is handled
     * When:    The result is RESULT_OK
     * And:     The result code is AUTHORISED
     * And:     The card type is unknown
     * Then:    Then the card details are updated with card type of NOT_SET
     */
    @Test
    fun `card details updated as not set for unrecognised card type`() {
        val additionalData = JSONObject()
                .put(CARD_SUMMARY, "1234")
                .put(PAYMENT_METHOD, "")
        val response = JSONObject()
                .put(RESULT_CODE, AUTHORISED)
                .put(MERCHANT_REFERENCE, TRIP_ID)
                .put(ADDITIONAL_DATA, additionalData)

        whenever(data.getStringExtra(RESULT_KEY)).thenReturn(response.toString())
        adyenPaymentPresenter.handleActivityResult(
                requestCode = REQUEST_CODE,
                resultCode = AppCompatActivity.RESULT_OK,
                data = data)

        verify(userStore).savedPaymentInfo = paymentInfoCaptor.capture()
        assertEquals("1234", paymentInfoCaptor.firstValue.lastFour)
        assertEquals(CardType.NOT_SET, paymentInfoCaptor.firstValue.cardType)
    }

    /**
     * Given:   An activity result is handled
     * When:    The result is RESULT_OK
     * And:     The result code is AUTHORISED
     * Then:    Then the card details are updated
     * And:     The transaction id is set as the nonce
     */
    @Test
    fun `card details and nonce updated for activity RESULT_OK and the result code is authorised`() {
        val additionalData = JSONObject()
                .put(CARD_SUMMARY, "1234")
                .put(PAYMENT_METHOD, "mc")
        val response = JSONObject()
                .put(RESULT_CODE, AUTHORISED)
                .put(MERCHANT_REFERENCE, TRIP_ID)
                .put(ADDITIONAL_DATA, additionalData)

        whenever(data.getStringExtra(RESULT_KEY)).thenReturn(response.toString())
        adyenPaymentPresenter.handleActivityResult(
                requestCode = REQUEST_CODE,
                resultCode = AppCompatActivity.RESULT_OK,
                data = data)

        verify(userStore).savedPaymentInfo = paymentInfoCaptor.capture()
        assertEquals("1234", paymentInfoCaptor.firstValue.lastFour)
        assertEquals(CardType.MASTERCARD, paymentInfoCaptor.firstValue.cardType)
    }

    /**
     * Given:   Saved payment info has changed
     * Then:    The view is updated with the payment details
     */
    @Test
    fun `payment details updated when saved payment info changes`() {
        adyenPaymentPresenter.onSavedPaymentInfoChanged(userPaymentInfo = savedPaymentInfo)

        verify(paymentDropInActions).updatePaymentDetails(savedPaymentInfo)
        verify(paymentDropInActions).handlePaymentDetailsUpdate()
    }

    /**
     * Given:   Guest payment is initialised
     * Then:    The nonce and payment details are returned
     */
    @Test
    fun `nonce is passed back when guest payment is initialised for test`() {
        setConfig(handleBraintree = true)
        setMockNonce()

        whenever(price.currencyCode).thenReturn(DEFAULT_CURRENCY)
        whenever(price.highPrice).thenReturn(100)

        adyenPaymentPresenter.initialiseGuestPayment(price)

        verify(paymentDropInActions).threeDSecureNonce(TRIP_ID, TRIP_ID)
    }

    /**
     * Given:   Guest payment is initialised
     * Then:    The nonce and payment details are returned
     */
    @Test
    fun `token and amount are passed back when guest payment is initialised`() {
        setConfig()
        setMockNonce()

        whenever(price.currencyCode).thenReturn(DEFAULT_CURRENCY)
        whenever(price.highPrice).thenReturn(100)

        adyenPaymentPresenter.initialiseGuestPayment(price)

        verify(paymentDropInActions).threeDSecureNonce(TRIP_ID, TRIP_ID, "1.00")
    }

    private fun setConfig(handleBraintree: Boolean = false) {
        KarhooUISDKConfigurationProvider.setConfig(
                configuration = UnitTestUISDKConfig(
                        context = context,
                        authenticationMethod = guestAuth,
                        handleBraintree = handleBraintree))
    }

    private fun setMockNonce() {
        val payload = JSONObject()
                .put(RESULT_CODE, AUTHORISED)
                .put(TRIP_ID_KEY, TRIP_ID).toString()
        whenever(data.getStringExtra(RESULT_KEY)).thenReturn(payload)

        adyenPaymentPresenter.handleActivityResult(
                requestCode = REQUEST_CODE,
                resultCode = AppCompatActivity.RESULT_OK,
                data = data)
    }

    companion object {
        private val adyenPublicKey: AdyenPublicKey = AdyenPublicKey("12345678")
        private val guestAuth: AuthenticationMethod.Guest = AuthenticationMethod.Guest("identifier", "referer", "guestOrganisationId")
        private const val ADDITIONAL_DATA = AdyenPaymentView.ADDITIONAL_DATA
        private const val AUTHORISED = AdyenPaymentView.AUTHORISED
        private const val CARD_SUMMARY = AdyenPaymentPresenter.CARD_SUMMARY
        private const val PAYMENT_METHOD = AdyenPaymentPresenter.PAYMENT_METHOD
        private const val TRIP_ID = "1234"
        private const val TRIP_ID_KEY = AdyenDropInServicePresenter.TRIP_ID
        private const val MERCHANT_REFERENCE = AdyenPaymentView.MERCHANT_REFERENCE
        private const val RESULT_KEY = AdyenResultActivity.RESULT_KEY
        private const val REQUEST_CODE = AdyenPaymentView.REQ_CODE_ADYEN
        private const val RESULT_CODE = AdyenPaymentPresenter.RESULT_CODE
    }
}