package com.karhoo.uisdk.screen.booking.checkout.payment.braintree

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.braintreepayments.api.DropInPaymentMethod
import com.braintreepayments.api.DropInResult
import com.braintreepayments.api.PaymentMethodNonce
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.BraintreeSDKToken
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.model.Organisation
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.network.request.SDKInitRequest
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.UnitTestUISDKConfig
import com.karhoo.uisdk.screen.booking.checkout.payment.PaymentDropInContract
import com.nhaarman.mockitokotlin2.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BraintreePaymentPresenterTest {

    private val price: QuotePrice = QuotePrice(highPrice = 100, currencyCode = "GBP")

    private var context: Context = mock()
    private var data: Intent = mock()
    private var dropInResult: DropInResult = mock()
    private var paymentMethodNonce: PaymentMethodNonce = mock()
    private var paymentsService: PaymentsService = mock()
    private var userStore: UserStore = mock()
    private var paymentView: PaymentDropInContract.Actions = mock()
    private var quote: Quote = mock()
    private val sdkInitCall: Call<BraintreeSDKToken> = mock()
    private val sdkInitCaptor = argumentCaptor<(Resource<BraintreeSDKToken>) -> Unit>()

    @Captor
    private lateinit var paymentInfoCaptor: ArgumentCaptor<SavedPaymentInfo>

    @Captor
    private lateinit var sdkInitialisationCaptor: ArgumentCaptor<SDKInitRequest>

    private lateinit var braintreePaymentPresenter: BraintreePaymentPresenter

    @Before
    fun setUp() {
        UnitTestUISDKConfig.setKarhooAuthentication(context)

        whenever(paymentsService.initialisePaymentSDK(any()))
                .thenReturn(sdkInitCall)
        doNothing().whenever(sdkInitCall).execute(sdkInitCaptor.capture())

        whenever(userStore.currentUser).thenReturn(userDetails)
        whenever(quote.price).thenReturn(price)

        braintreePaymentPresenter = BraintreePaymentPresenter(
                paymentsService = paymentsService,
                userStore = userStore)

        braintreePaymentPresenter.view = paymentView
    }

    @After
    fun tearDown() {
        setAuthenticatedUser()
    }

    /**
     * Given:   Guest payment is initialisee
     * Then:    Then the nonce is returned
     */
    @Test
    fun `nonce returned when guest payment is initialised`() {
        braintreePaymentPresenter.initialiseGuestPayment(quote)

        verify(paymentView).threeDSecureNonce("", "", "1.00")
    }

    /**
     * Given:   A request is made to change card
     * When:    The user is in the Guest flow
     * Then:    Then the correct organisation id is used
     */
    @Test
    fun `change card pressed for guest and correct organisation id is used`() {
        KarhooUISDKConfigurationProvider.setConfig(
                configuration = UnitTestUISDKConfig(
                        context = context,
                        authenticationMethod = AuthenticationMethod.Guest("identifier", "referer", "guestOrganisationId"),
                        handleBraintree = false))

        braintreePaymentPresenter.sdkInit(quote)

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(paymentsService).initialisePaymentSDK(capture(sdkInitialisationCaptor))

        assertEquals("guestOrganisationId", sdkInitialisationCaptor.value.organisationId)
    }

    /**
     * Given:   A request is made to change card
     * When:    The user is in the logged in flow
     * Then:    Then the correct organisation id is used
     */
    @Test
    fun `change card pressed for logged in user and correct organisation id is used`() {
        braintreePaymentPresenter.sdkInit(quote)

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(paymentsService).initialisePaymentSDK(capture(sdkInitialisationCaptor))

        assertEquals("organisation_id", sdkInitialisationCaptor.value.organisationId)
    }

    /**
     * Given:   A request is made to change card
     * And:     It is a test run
     * When:    The request is successful
     * Then:    The view should be asked to show payment UI
     */
    @Test
    fun `change card pressed and result is successful for test`() {
        KarhooUISDKConfigurationProvider.setConfig(
                configuration = UnitTestUISDKConfig(
                        context = context,
                        authenticationMethod = AuthenticationMethod.Guest("identifier", "referer", "guestOrganisationId"),
                        handleBraintree = true))
        whenever(userStore.savedPaymentInfo).thenReturn(SavedPaymentInfo(CARD_ENDING, CardType.VISA))
        braintreePaymentPresenter.sdkInit(quote)

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(paymentView, never()).showError(R.string.kh_uisdk_something_went_wrong, null)
        verify(userStore).savedPaymentInfo

    }

    /**
     * Given:   A request is made to change card
     * And:     It is a guest test run
     * When:    The request is successful
     * Then:    The view should be asked to show payment UI
     */
    @Test
    fun `change card pressed and result is successful for guest test`() {
        KarhooUISDKConfigurationProvider.setConfig(configuration = UnitTestUISDKConfig(context =
                                                                                       context,
                                                                                       authenticationMethod = AuthenticationMethod.Guest("identifier", "referer", "guestOrganisationId"), handleBraintree = true))
        whenever(userStore.savedPaymentInfo).thenReturn(SavedPaymentInfo(CARD_ENDING, CardType.VISA))
        braintreePaymentPresenter.sdkInit(quote)

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(paymentView, never()).showPaymentUI(BRAINTREE_SDK_TOKEN, null, quote)
        verify(userStore).savedPaymentInfo = capture(paymentInfoCaptor)
        assertEquals(CardType.VISA, paymentInfoCaptor.value.cardType)
        assertEquals(CARD_ENDING, paymentInfoCaptor.value.lastFour)
    }

    /**
     * Given:   A request is made to change card
     * When:    The request is successful
     * Then:    The view should be asked to show payment UI
     */
    @Test
    fun `change card pressed and result is successful`() {
        setAuthenticatedUser()
        braintreePaymentPresenter.sdkInit(quote)

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(paymentView).showPaymentUI(sdkToken = BRAINTREE_SDK_TOKEN, paymentData = null,
                                          quote = null)
    }

    /**
     * Given:   A request is made to change card
     * When:    The request is unsuccessful
     * Then:    The view should be asked to show an error
     */
    @Test
    fun `change card pressed and result is unsuccessful`() {
        braintreePaymentPresenter.sdkInit(quote)

        sdkInitCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(paymentView).showError(R.string.kh_uisdk_something_went_wrong, KarhooError.GeneralRequestError)
    }

    /**
     * Given:   The user has been changed
     * When:    The presenter is subscribed to the observer
     * Then:    A call should be made to update the payment info for the user
     */
    @Test
    fun `user update calls the view to display the new payment info`() {
        val savedPaymentInfo = SavedPaymentInfo("", CardType.NOT_SET)

        braintreePaymentPresenter.onSavedPaymentInfoChanged(savedPaymentInfo)
        verify(paymentView).updatePaymentDetails(SavedPaymentInfo("", CardType.NOT_SET))
        verify(paymentView).handlePaymentDetailsUpdate()
    }

    /**
     * Given:   A user updates payment card details
     * Then:    The card info is stored and the view is not updated
     */
    @Test
    fun `card info stored and correct updates made to view if there is payment nonce info`() {
        val desc = "ending in 00"

        braintreePaymentPresenter.updateCardDetails(desc, "Visa")

        verify(userStore).savedPaymentInfo = capture(paymentInfoCaptor)
        assertEquals(CardType.VISA, paymentInfoCaptor.value.cardType)
        assertEquals(desc, paymentInfoCaptor.value.lastFour)
    }

    /**
     * Given:   Book trip flow is initiated
     * And:     The user is logged in
     * When:    SDK Init returns an error
     * Then:    View shows booking error
     */
    @Test
    fun `logged in user sdk init error shows error`() {
        setAuthenticatedUser()
        UnitTestUISDKConfig.setKarhooAuthentication(context)
        whenever(paymentsService.initialisePaymentSDK(any())).thenReturn(sdkInitCall)

        braintreePaymentPresenter.getPaymentNonce(quote)

        sdkInitCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(paymentView).showError(R.string.kh_uisdk_something_went_wrong, KarhooError.GeneralRequestError)
    }

    /**
     * Given:   Book trip flow is initiated
     * And:     The user is logged in
     * When:    SDK Init returns success
     * And:     Get nonce returns success
     * Then:    Three D Secure the nonce
     */
    @Test
    fun `logged in user get nonce success shows three d secure`() {
        setAuthenticatedUser()

        whenever(paymentsService.initialisePaymentSDK(any())).thenReturn(sdkInitCall)

        braintreePaymentPresenter.getPaymentNonce(quote)

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(paymentView).threeDSecureNonce(BRAINTREE_SDK_TOKEN, "", "1.00")
    }

    /**
     * Given:   Book trip flow is initiated
     * And:     The user is logged in
     * When:    SDK Init returns success
     * And:     Get nonce returns success
     * Then:    Three D Secure the nonce
     */
    @Test
    fun `logged in user get nonce success for test does not shows three d secure`() {
        KarhooUISDKConfigurationProvider.setConfig(configuration = UnitTestUISDKConfig(context =
                                                                                       context,
                                                                                       authenticationMethod = AuthenticationMethod.KarhooUser(), handleBraintree = true))

        whenever(paymentsService.initialisePaymentSDK(any())).thenReturn(sdkInitCall)

        braintreePaymentPresenter.getPaymentNonce(quote)

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(paymentView).threeDSecureNonce(BRAINTREE_SDK_TOKEN)
    }

    /**
     * Given:   An activity result is handled
     * When:    The result is RESULT_OK
     * And:     There is data
     * And:     It is a guest Braintree user request
     * Then:    The card details are not updated anymore
     */
    @Test
    fun `card details not updated anymore for activity result RESULT_OK for guest Braintree user`() {
        whenever(data.getParcelableExtra<DropInResult>(BraintreePaymentActivity.BRAINTREE_ACTIVITY_DROP_IN_RESULT))
                .thenReturn(dropInResult)
        whenever(data.hasExtra(BraintreePaymentActivity.BRAINTREE_ACTIVITY_DROP_IN_RESULT))
                .thenReturn(true)

        whenever(dropInResult.paymentMethodNonce).thenReturn(paymentMethodNonce)
        whenever(paymentMethodNonce.string).thenReturn(BRAINTREE_SDK_TOKEN)
        whenever(dropInResult.paymentDescription).thenReturn(CARD_ENDING)
        whenever(dropInResult.paymentMethodType).thenReturn(DropInPaymentMethod.MASTERCARD)

        braintreePaymentPresenter.handleActivityResult(
                requestCode = BraintreePaymentView.REQ_CODE_BRAINTREE_GUEST,
                resultCode = AppCompatActivity.RESULT_OK,
                data = data)

        verify(userStore).savedPaymentInfo = capture(paymentInfoCaptor)
        assertEquals(CardType.MASTERCARD, paymentInfoCaptor.value.cardType)
        assertEquals(CARD_ENDING, paymentInfoCaptor.value.lastFour)
    }

    /**
     * Given:   An activity result is handled
     * When:    The result is RESULT_OK
     * And:     There is data
     * And:     It is a guest Braintree user request
     * Then:    The booking succeeds
     */
    @Test
    fun `payment succeeds for activity result RESULT_OK for guest Braintree user`() {
        whenever(data.getParcelableExtra<DropInResult>(BraintreePaymentActivity.BRAINTREE_ACTIVITY_DROP_IN_RESULT))
            .thenReturn(dropInResult)
        whenever(data.hasExtra(BraintreePaymentActivity.BRAINTREE_ACTIVITY_DROP_IN_RESULT))
            .thenReturn(true)

        whenever(dropInResult.paymentMethodNonce).thenReturn(paymentMethodNonce)
        whenever(paymentMethodNonce.string).thenReturn(BRAINTREE_SDK_TOKEN)
        whenever(dropInResult.paymentDescription).thenReturn(CARD_ENDING)
        whenever(dropInResult.paymentMethodType).thenReturn(DropInPaymentMethod.MASTERCARD)

        braintreePaymentPresenter.handleActivityResult(
            requestCode = BraintreePaymentView.REQ_CODE_BRAINTREE_GUEST,
            resultCode = AppCompatActivity.RESULT_OK,
            data = data)

        verify(userStore).savedPaymentInfo = capture(paymentInfoCaptor)
        assertEquals(CardType.MASTERCARD, paymentInfoCaptor.value.cardType)
        assertEquals(CARD_ENDING, paymentInfoCaptor.value.lastFour)

        assertEquals(braintreePaymentPresenter.getNonceForTesting(), dropInResult.paymentMethodNonce?.string)
    }

    /**
     * Given:   An activity result is handled
     * When:    The result is RESULT_OK
     * And:     There is data
     * And:     It is a token Braintree user request
     * Then:    The booking succeeds
     */
    @Test
    fun `payment succeeds for activity result RESULT_OK for token Braintree user`() {
        whenever(data.getParcelableExtra<DropInResult>(BraintreePaymentActivity.BRAINTREE_ACTIVITY_DROP_IN_RESULT))
            .thenReturn(dropInResult)
        whenever(data.hasExtra(BraintreePaymentActivity.BRAINTREE_ACTIVITY_DROP_IN_RESULT))
            .thenReturn(true)

        whenever(dropInResult.paymentMethodNonce).thenReturn(paymentMethodNonce)
        whenever(paymentMethodNonce.string).thenReturn(BRAINTREE_SDK_TOKEN)

        setAuthenticatedUser()

        braintreePaymentPresenter.handleActivityResult(
            requestCode = BraintreePaymentView.REQ_CODE_BRAINTREE,
            resultCode = AppCompatActivity.RESULT_OK,
            data = data)


        verify(paymentView).threeDSecureNonce(dropInResult.paymentMethodNonce!!.string)
    }

    private fun setAuthenticatedUser() {
        KarhooUISDKConfigurationProvider.setConfig(configuration = UnitTestUISDKConfig(context =
                                                                                       context,
                                                                                       authenticationMethod = AuthenticationMethod.KarhooUser(), handleBraintree = false))
    }

    companion object {
        private const val BRAINTREE_SDK_TOKEN = "TEST TOKEN"
        private const val CARD_ENDING = "....12"
        private const val EXPECTED_AMOUNT_AS_STRING = "1500"

        private val userDetails: UserInfo = UserInfo(firstName = "David",
                                                     lastName = "Smith",
                                                     email = "david.smith@email.com",
                                                     phoneNumber = "+441234 56789",
                                                     userId = "123",
                                                     locale = "en-GB",
                                                     organisations = listOf(Organisation(id = "organisation_id", name = "Organisation", roles = listOf("PERMISSION_ONE", "PERMISSION_TWO"))))

    }

}
