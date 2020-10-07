package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import com.adyen.checkout.dropin.service.CallResult
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.screen.booking.booking.payment.adyen.AdyenDropInServicePresenter.Companion.CHANNEL
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.firstValue
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
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AdyenDropInServicePresenterTest {

    private val mutableList = mutableListOf(PAYMENT_METHOD, AMOUNT, SHOPPER_REFERENCE, RANDOM)
    private var jsonObject: JSONObject = mock()
    private var paymentJson: JSONObject = mock()
    private var amountJson: JSONObject = mock()
    private val service: AdyenDropInServiceMVP.Service = mock()
    private val paymentsService: PaymentsService = mock()
    private val paymentsCall: Call<JSONObject> = mock()
    private val paymentsCaptor = argumentCaptor<(Resource<JSONObject>) -> Unit>()
    private val paymentsDetailsCall: Call<JSONObject> = mock()
    private val paymentsDetailsCaptor = argumentCaptor<(Resource<JSONObject>) -> Unit>()

    @Captor
    private lateinit var requestCaptor: ArgumentCaptor<String>

    @Captor
    private lateinit var resultsCaptor: ArgumentCaptor<CallResult>

    private lateinit var presenter: AdyenDropInServicePresenter

    @Before
    fun setUp() {
        whenever(jsonObject.keys()).thenReturn(mutableList.iterator())
        whenever(jsonObject.get(PAYMENT_METHOD)).thenReturn(paymentJson)
        whenever(jsonObject.get(AMOUNT)).thenReturn(amountJson)
        whenever(jsonObject.get(SHOPPER_REFERENCE)).thenReturn("")
        whenever(jsonObject.get(RANDOM)).thenReturn("{}")

        whenever(paymentsService.getAdyenPayments(any())).thenReturn(paymentsCall)
        doNothing().whenever(paymentsCall).execute(paymentsCaptor.capture())

        whenever(paymentsService.getAdyenPaymentDetails(any())).thenReturn(paymentsDetailsCall)
        doNothing().whenever(paymentsDetailsCall).execute(paymentsDetailsCaptor.capture())

        presenter = AdyenDropInServicePresenter(service, paymentsService)
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

        verify(paymentsService).getAdyenPayments(capture(requestCaptor))
        val requestString = requestCaptor.value
        assertTrue(requestString.contains(PAYMENT_METHOD))
        assertTrue(requestString.contains(AMOUNT))
        assertTrue(requestString.contains(RANDOM))
        assertFalse(requestString.contains(SHOPPER_REFERENCE))
        verify(jsonObject).keys()
        verify(jsonObject).get(PAYMENT_METHOD)
        verify(jsonObject).get(AMOUNT)
        verify(jsonObject).get(SHOPPER_REFERENCE)
        verify(jsonObject).get(RANDOM)
        verify(service).handleResult(capture(resultsCaptor))
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
                .put(TRANSACTION_ID_KEY, TRANSACTION_ID)

        presenter.getAdyenPayments(jsonObject, RETURN_URL)

        paymentsCaptor.firstValue.invoke(Resource.Success(response))

        verify(paymentsService).getAdyenPayments(any())
        verify(service).storeTransactionId(TRANSACTION_ID)
        verify(service).handleResult(capture(resultsCaptor))
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
                .put(TRANSACTION_ID_KEY, TRANSACTION_ID)

        presenter.getAdyenPayments(jsonObject, RETURN_URL)

        paymentsCaptor.firstValue.invoke(Resource.Success(response))

        verify(paymentsService).getAdyenPayments(any())
        verify(service).storeTransactionId(TRANSACTION_ID)
        verify(service).handleResult(capture(resultsCaptor))
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
        verify(service).handleResult(capture(resultsCaptor))
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
        verify(service).handleResult(capture(resultsCaptor))
        assertEquals(CallResult.ResultType.ERROR, resultsCaptor.firstValue.type)
    }

    /**
     * Given:   A request is made to retrieve Adyen payment details
     * When:    The response is a failure
     * Then:    Then an error result is returned
     */
    @Test
    fun `error shown when Adyen payment details retrieval fails`() {
        presenter.getAdyenPaymentDetails(jsonObject, TRANSACTION_ID)

        paymentsDetailsCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))

        verify(paymentsService).getAdyenPaymentDetails(any())
        verify(service).handleResult(capture(resultsCaptor))
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
                .put(TRANSACTION_ID_KEY, TRANSACTION_ID)

        presenter.getAdyenPaymentDetails(jsonObject, TRANSACTION_ID)

        paymentsDetailsCaptor.firstValue.invoke(Resource.Success(response))

        verify(paymentsService).getAdyenPaymentDetails(any())
        verify(service, never()).storeTransactionId(TRANSACTION_ID)
        verify(service).handleResult(capture(resultsCaptor))
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
                .put(TRANSACTION_ID_KEY, TRANSACTION_ID)

        presenter.getAdyenPaymentDetails(jsonObject, TRANSACTION_ID)

        paymentsDetailsCaptor.firstValue.invoke(Resource.Success(response))

        verify(paymentsService).getAdyenPaymentDetails(any())
        verify(service, never()).storeTransactionId(TRANSACTION_ID)
        verify(service).handleResult(capture(resultsCaptor))
        assertEquals(CallResult.ResultType.ACTION, resultsCaptor.firstValue.type)
    }

    companion object {
        private const val ACTION = AdyenDropInServicePresenter.ACTION
        private const val AMOUNT = "amount"
        private const val RANDOM = "RANDOM"
        private const val SHOPPER_REFERENCE = "shopperReference"
        private const val PAYMENT_METHOD = "paymentMethod"
        private const val TRANSACTION_ID = "1234"
        private const val TRANSACTION_ID_KEY = AdyenDropInServicePresenter.TRANSACTION_ID
        private const val RETURN_URL = "http://adyen.return.url"
    }
}