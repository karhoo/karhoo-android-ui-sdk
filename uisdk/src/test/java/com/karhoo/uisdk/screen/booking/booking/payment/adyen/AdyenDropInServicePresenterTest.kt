package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.content.Context
import com.adyen.checkout.dropin.service.CallResult
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.screen.booking.booking.payment.adyen.AdyenDropInServicePresenter.Companion.ACCEPT_HEADER
import com.karhoo.uisdk.screen.booking.booking.payment.adyen.AdyenDropInServicePresenter.Companion.ADDITIONAL_DATA
import com.karhoo.uisdk.screen.booking.booking.payment.adyen.AdyenDropInServicePresenter.Companion.ALLOW_3DS
import com.karhoo.uisdk.screen.booking.booking.payment.adyen.AdyenDropInServicePresenter.Companion.BROWSER_INFO
import com.karhoo.uisdk.screen.booking.booking.payment.adyen.AdyenDropInServicePresenter.Companion.PAYMENTS_PAYLOAD
import com.karhoo.uisdk.screen.booking.booking.payment.adyen.AdyenDropInServicePresenter.Companion.SUPPLY_PARTNER_ID
import com.karhoo.uisdk.screen.booking.booking.payment.adyen.AdyenDropInServicePresenter.Companion.USER_AGENT
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AdyenDropInServicePresenterTest {

    private val mutableList = mutableListOf(PAYMENT_METHOD, AMOUNT, SHOPPER_REFERENCE, RANDOM)
    private var jsonObject: JSONObject = mock()
    private var context: Context = mock()
    private var paymentJson: JSONObject = JSONObject("{\"type\": \"scheme\",\n\"holderName\": " +
                                                             "\"Test\"}")
    private var amountJson: JSONObject = JSONObject("{ \"currency\": \"GBP\" }")
    private val service: AdyenDropInServiceMVP.Service = mock()
    private var dropInRepository: AdyenDropInServiceMVP.Repository = mock()
    private val paymentsService: PaymentsService = mock()
    private val paymentsCall: Call<JSONObject> = mock()
    private val paymentsCaptor = argumentCaptor<(Resource<JSONObject>) -> Unit>()
    private val paymentsDetailsCall: Call<JSONObject> = mock()
    private val paymentsDetailsCaptor = argumentCaptor<(Resource<JSONObject>) -> Unit>()
    private val requestCaptor = argumentCaptor<String>()
    private val resultsCaptor = argumentCaptor<CallResult>()

    private lateinit var presenter: AdyenDropInServicePresenter

    @Before
    fun setUp() {
        whenever(jsonObject.keys()).thenReturn(mutableList.iterator())
        whenever(jsonObject.get(PAYMENT_METHOD)).thenReturn(paymentJson)
        whenever(jsonObject.get(AMOUNT)).thenReturn(amountJson)
        whenever(jsonObject.get(SHOPPER_REFERENCE)).thenReturn("")
        whenever(jsonObject.get(RANDOM)).thenReturn("{}")
        whenever(dropInRepository.supplyPartnerId).thenReturn(SUPPLIER_ID)

        whenever(paymentsService.getAdyenPayments(any())).thenReturn(paymentsCall)
        doNothing().whenever(paymentsCall).execute(paymentsCaptor.capture())

        whenever(paymentsService.getAdyenPaymentDetails(any())).thenReturn(paymentsDetailsCall)
        doNothing().whenever(paymentsDetailsCall).execute(paymentsDetailsCaptor.capture())

        presenter = AdyenDropInServicePresenter(context, service, paymentsService, dropInRepository)
    }

    /**
     * Given:   A request is made to retrieve Adyen payments
     * Then:    The correct payload elements are added to the request
     */
    @Test
    fun `correct payments payload elements added to the payments request`() {
        presenter.getAdyenPayments(jsonObject, RETURN_URL)

        paymentsCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))

        verify(paymentsService).getAdyenPayments(requestCaptor.capture())

        val requestJson = JSONObject(requestCaptor.firstValue)
        val payloadJson = requestJson.getJSONObject(PAYMENTS_PAYLOAD)
        val partnerId = requestJson.getString(SUPPLY_PARTNER_ID)
        assertEquals(SUPPLIER_ID, partnerId)
        assertTrue(payloadJson.has(AMOUNT))
        assertTrue(payloadJson.has(PAYMENT_METHOD))
        assertTrue(payloadJson.has(RANDOM))
        assertFalse(payloadJson.has(SHOPPER_REFERENCE))
        assertTrue(payloadJson.has(ADDITIONAL_DATA))
        val additionalData = payloadJson.getJSONObject(ADDITIONAL_DATA)
        assertTrue(additionalData.has(ALLOW_3DS))
        assertEquals("true", additionalData.get(ALLOW_3DS))
        assertTrue(payloadJson.has(BROWSER_INFO))
        val browserInfo = payloadJson.getJSONObject(BROWSER_INFO)
        assertTrue(browserInfo.has(USER_AGENT))
        assertTrue(browserInfo.has(ACCEPT_HEADER))
        verify(jsonObject).keys()
        verify(jsonObject).get(PAYMENT_METHOD)
        verify(jsonObject).get(AMOUNT)
        verify(jsonObject).get(SHOPPER_REFERENCE)
        verify(jsonObject).get(RANDOM)
    }

    /**
     * Given:   A request is made to retrieve Adyen payments
     * When:    The response is a failure
     * Then:    Then an error result is returned
     */
    @Test
    fun `error shown when Adyen payment retrieval fails`() {
        presenter.getAdyenPayments(jsonObject, RETURN_URL)

        paymentsCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))

        verify(paymentsService).getAdyenPayments(requestCaptor.capture())

        verify(service).handleResult(resultsCaptor.capture())
        assertEquals(CallResult.ResultType.ERROR, resultsCaptor.firstValue.type)
    }

    /**
     * Given:   A request is made to retrieve Adyen payments
     * When:    The response is a success
     * And:     No action object is returned in the response
     * Then:    Then a result type of FINISHED is returned
     */
    @Test
    fun `finished result returned when Adyen payment retrieval succeeds with no action`() {

        val response = JSONObject()
                .put(TRIP_ID_KEY, TRIP_ID)

        presenter.getAdyenPayments(jsonObject, RETURN_URL)

        paymentsCaptor.firstValue.invoke(Resource.Success(response))

        verify(paymentsService).getAdyenPayments(any())
        verify(dropInRepository).tripId = TRIP_ID
        verify(service).handleResult(resultsCaptor.capture())
        assertEquals(CallResult.ResultType.FINISHED, resultsCaptor.firstValue.type)
    }

    /**
     * Given:   A request is made to retrieve Adyen payments
     * When:    The response is a success
     * And:     An action object is returned in the response
     * Then:    Then a result type of ACTION is returned
     */
    @Test
    fun `action result returned when Adyen payment retrieval succeeds with an action`() {
        val response = JSONObject()
                .put(ACTION, "some action")
                .put(TRIP_ID_KEY, TRIP_ID)

        presenter.getAdyenPayments(jsonObject, RETURN_URL)

        paymentsCaptor.firstValue.invoke(Resource.Success(response))

        verify(paymentsService).getAdyenPayments(any())
        verify(dropInRepository).tripId = TRIP_ID
        verify(service).handleResult(resultsCaptor.capture())
        assertEquals(CallResult.ResultType.ACTION, resultsCaptor.firstValue.type)
    }

    /**
     * Given:   A request is made to retrieve Adyen payment details
     * When:    The response is a failure
     * Then:    Then an error result is returned
     */
    @Test
    fun `error shown when Adyen payment details is called when there is no transaction id`() {
        presenter.getAdyenPaymentDetails(jsonObject, RETURN_URL)

        paymentsDetailsCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))

        verify(paymentsService).getAdyenPaymentDetails(any())
        verify(service).handleResult(resultsCaptor.capture())
        assertEquals(CallResult.ResultType.ERROR, resultsCaptor.firstValue.type)
    }

    /**
     * Given:   A request is made to retrieve Adyen payment details
     * When:    There is no transaction id
     * Then:    Then an error result is returned
     */
    @Test
    fun `error shown when Adyen payment details retrieval is attempted with no transaction id`() {
        presenter.getAdyenPaymentDetails(jsonObject, null)

        verify(paymentsService, never()).getAdyenPaymentDetails(any())
        verify(service).handleResult(resultsCaptor.capture())
        assertEquals(CallResult.ResultType.ERROR, resultsCaptor.firstValue.type)
    }

    /**
     * Given:   A request is made to retrieve Adyen payment details
     * When:    The response is a failure
     * Then:    Then an error result is returned
     */
    @Test
    fun `error shown when Adyen payment details retrieval fails`() {
        presenter.getAdyenPaymentDetails(jsonObject, TRIP_ID)

        paymentsDetailsCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))

        verify(paymentsService).getAdyenPaymentDetails(any())
        verify(service).handleResult(resultsCaptor.capture())
        assertEquals(CallResult.ResultType.ERROR, resultsCaptor.firstValue.type)
    }

    /**
     * Given:   A request is made to retrieve Adyen payment details
     * When:    The response is a success
     * And:     No action object is returned in the response
     * Then:    Then a result type of FINISHED is returned
     */
    @Test
    fun `finished result returned when Adyen payment details retrieval succeeds with no action`() {

        val response = JSONObject()
                .put(TRIP_ID_KEY, TRIP_ID)

        presenter.getAdyenPaymentDetails(jsonObject, TRIP_ID)

        paymentsDetailsCaptor.firstValue.invoke(Resource.Success(response))

        verify(paymentsService).getAdyenPaymentDetails(any())
        verify(service).handleResult(resultsCaptor.capture())
        assertEquals(CallResult.ResultType.FINISHED, resultsCaptor.firstValue.type)
    }

    /**
     * Given:   A request is made to retrieve Adyen payment details
     * When:    The response is a success
     * And:     An action object is returned in the response
     * Then:    Then a result type of ACTION is returned
     */
    @Test
    fun `action result returned when Adyen payment details retrieval succeeds with an action`() {

        val response = JSONObject()
                .put(ACTION, "some action")
                .put(TRIP_ID_KEY, TRIP_ID)

        presenter.getAdyenPaymentDetails(jsonObject, TRIP_ID)

        paymentsDetailsCaptor.firstValue.invoke(Resource.Success(response))

        verify(paymentsService).getAdyenPaymentDetails(any())
        verify(service).handleResult(resultsCaptor.capture())
        assertEquals(CallResult.ResultType.ACTION, resultsCaptor.firstValue.type)
    }

    companion object {
        private const val ACTION = AdyenDropInServicePresenter.ACTION
        private const val AMOUNT = "amount"
        private const val RANDOM = "RANDOM"
        private const val SHOPPER_REFERENCE = "shopperReference"
        private const val PAYMENT_METHOD = "paymentMethod"
        private const val TRIP_ID = "1234"
        private const val SUPPLIER_ID = "SPID"
        private const val TRIP_ID_KEY = AdyenDropInServicePresenter.TRIP_ID
        private const val RETURN_URL = "http://adyen.return.url"
    }
}
