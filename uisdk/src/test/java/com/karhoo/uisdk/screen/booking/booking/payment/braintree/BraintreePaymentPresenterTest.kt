package com.karhoo.uisdk.screen.booking.booking.payment.braintree

import android.content.Context
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.BraintreeSDKToken
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.model.Organisation
import com.karhoo.sdk.api.model.PaymentsNonce
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.network.request.SDKInitRequest
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.UnitTestUISDKConfig
import com.karhoo.uisdk.screen.booking.booking.payment.BookingPaymentMVP
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
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

    private var context: Context = mock()
    private var paymentsService: PaymentsService = mock()
    private var userStore: UserStore = mock()
    private var paymentView: BookingPaymentMVP.View = mock()
    private var price: QuotePrice = mock()
    private val sdkInitCall: Call<BraintreeSDKToken> = mock()
    private val sdkInitCaptor = argumentCaptor<(Resource<BraintreeSDKToken>) -> Unit>()
    private val passBraintreeTokenCall: Call<PaymentsNonce> = mock()
    private val passBraintreeTokenCaptor = argumentCaptor<(Resource<PaymentsNonce>) -> Unit>()
    private val getNonceCall: Call<PaymentsNonce> = mock()
    private val getNonceCaptor = argumentCaptor<(Resource<PaymentsNonce>) -> Unit>()
    @Captor
    private lateinit var paymentInfoCaptor: ArgumentCaptor<SavedPaymentInfo>

    @Captor
    private lateinit var sdkInitialisationCaptor: ArgumentCaptor<SDKInitRequest>

    private lateinit var braintreePaymentPresenter: BraintreePaymentPresenter

    @Before
    fun setUp() {
        whenever(paymentsService.initialisePaymentSDK(any()))
                .thenReturn(sdkInitCall)
        doNothing().whenever(sdkInitCall).execute(sdkInitCaptor.capture())

        doNothing().whenever(getNonceCall).execute(getNonceCaptor.capture())

        whenever(paymentsService.addPaymentMethod(any()))
                .thenReturn(passBraintreeTokenCall)
        doNothing().whenever(passBraintreeTokenCall).execute(passBraintreeTokenCaptor.capture())

        whenever(userStore.currentUser).thenReturn(userDetails)

        braintreePaymentPresenter = BraintreePaymentPresenter(
                paymentsService = paymentsService,
                userStore = userStore,
                view = paymentView)
    }

    @After
    fun tearDown() {
        setAuthenticatedUser()
    }

    /**
     * Given:   A request is made to change card
     * When:    The user is in the Guest flow
     * Then:    Then the correct organisation id is used
     */
    @Test
    fun `change card pressed for guest and correct organisation id is used`() {
        KarhooUISDKConfigurationProvider.setConfig(configuration = UnitTestUISDKConfig(context =
                                                                                       context,
                                                                                       authenticationMethod = AuthenticationMethod.Guest("identifier", "referer", "guestOrganisationId"), handleBraintree = false))

        braintreePaymentPresenter.sdkInit(price)

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
        braintreePaymentPresenter.sdkInit(price)

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
        KarhooUISDKConfigurationProvider.setConfig(configuration = UnitTestUISDKConfig(context =
                                                                                       context,
                                                                                       authenticationMethod = AuthenticationMethod.Guest("identifier", "referer", "guestOrganisationId"), handleBraintree = true))
        whenever(userStore.savedPaymentInfo).thenReturn(SavedPaymentInfo(CARD_ENDING, CardType.VISA))
        braintreePaymentPresenter.sdkInit(price)

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(paymentView, never()).showError(any())
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
        braintreePaymentPresenter.sdkInit(price)

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(paymentView, never()).showPaymentUI(BRAINTREE_SDK_TOKEN, null, price)
        verify(paymentView).handlePaymentDetailsUpdate(BRAINTREE_SDK_TOKEN)
        verify(userStore).savedPaymentInfo = capture(paymentInfoCaptor)
        verify(paymentView).refresh()
        assertEquals(CardType.VISA, paymentInfoCaptor.value.cardType)
        assertEquals(CARD_ENDING, paymentInfoCaptor.value.lastFour)
        verify(paymentView).refresh()
    }

    /**
     * Given:   A request is made to change card
     * When:    The request is successful
     * Then:    The view should be asked to show payment UI
     */
    @Test
    fun `change card pressed and result is successful`() {
        setAuthenticatedUser()
        braintreePaymentPresenter.sdkInit(price)

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(paymentView).showPaymentUI(sdkToken = BRAINTREE_SDK_TOKEN, paymentData = null,
                                          price = null)
    }

    /**
     * Given:   A request is made to change card
     * When:    The request is unsuccessful
     * Then:    The view should be asked to show an error
     */
    @Test
    fun `change card pressed and result is unsuccessful`() {
        braintreePaymentPresenter.sdkInit(price)

        sdkInitCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(paymentView).showError(any())
    }

    /**
     * Given:   A request is made to pass the braintree token to the BE
     * When:    The request is successful
     * Then:    The view should be asked to bind the card details
     */
    @Test
    fun `pass braintree token to BE and bind card details should be called`() {
        braintreePaymentPresenter.passBackNonce(BRAINTREE_SDK_TOKEN)

        passBraintreeTokenCaptor.firstValue.invoke(Resource.Success(paymentsNonce))

        verify(paymentsService).addPaymentMethod(any())
    }

    /**
     * Given:   A request is made to pass the braintree token to the BE
     * When:    The request is successful
     * Then:    The view should be asked to show an error
     */
    @Test
    fun `pass braintree token to BE and an error occurs`() {
        braintreePaymentPresenter.passBackNonce(BRAINTREE_SDK_TOKEN)

        passBraintreeTokenCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(paymentView).showError(any())
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
        verify(paymentView).bindPaymentDetails(SavedPaymentInfo("", CardType.NOT_SET), null)
    }

    /**
     * Given:   A user updates payment card details
     * Then:    The card info is stored and the view is not updated
     */
    @Test
    fun `card info stored and correct updates made to view if there is payment nonce info`() {
        val desc = "ending in 00"

        braintreePaymentPresenter.updateCardDetails(paymentsNonce.nonce, desc, "Visa")

        verify(userStore).savedPaymentInfo = capture(paymentInfoCaptor)
        verify(paymentView).refresh()
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

        whenever(paymentsService.initialisePaymentSDK(any())).thenReturn(sdkInitCall)

        braintreePaymentPresenter.getPaymentNonce(price)

        sdkInitCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(paymentView).showError(R.string.something_went_wrong)
    }

    /**
     * Given:   Book trip flow is initiated
     * And:     The user is logged in
     * When:    SDK Init returns success
     * And:     Get Nonce returns failure
     * Then:    Show payment dialog
     */
    @Test
    fun `logged in user get nonce failure shows payment dialog`() {
        setAuthenticatedUser()

        whenever(paymentsService.initialisePaymentSDK(any())).thenReturn(sdkInitCall)
        whenever(paymentsService.getNonce(any())).thenReturn(getNonceCall)
        whenever(price.highPrice).thenReturn(EXPECTED_AMOUNT_AS_STRING.toInt())
        whenever(price.currencyCode).thenReturn("GBP")

        braintreePaymentPresenter.getPaymentNonce(price)

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))
        getNonceCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(paymentView).showPaymentDialog(BRAINTREE_SDK_TOKEN)
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
        whenever(paymentsService.getNonce(any())).thenReturn(getNonceCall)
        whenever(price.highPrice).thenReturn(1500)
        whenever(price.currencyCode).thenReturn("GBP")

        braintreePaymentPresenter.getPaymentNonce(price)

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))
        getNonceCaptor.firstValue.invoke(Resource.Success(PaymentsNonce(paymentsNonce.nonce, CardType.VISA)))

        verify(paymentView).threeDSecureNonce(BRAINTREE_SDK_TOKEN, paymentsNonce.nonce, "15.00")
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
        whenever(paymentsService.getNonce(any())).thenReturn(getNonceCall)
        whenever(price.highPrice).thenReturn(1500)
        whenever(price.currencyCode).thenReturn("GBP")

        braintreePaymentPresenter.getPaymentNonce(price)

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))
        getNonceCaptor.firstValue.invoke(Resource.Success(PaymentsNonce(paymentsNonce.nonce, CardType.VISA)))

        verify(paymentView).threeDSecureNonce(BRAINTREE_SDK_TOKEN)
    }

    private fun setAuthenticatedUser() {
        KarhooUISDKConfigurationProvider.setConfig(configuration = UnitTestUISDKConfig(context =
                                                                                       context,
                                                                                       authenticationMethod = AuthenticationMethod.KarhooUser(), handleBraintree = false))
    }

    companion object {
        private val paymentsNonce = PaymentsNonce(
                nonce = "1234557683749328",
                cardType = CardType.VISA,
                lastFour = "2345"
                                         )

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
