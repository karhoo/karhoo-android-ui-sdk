package com.karhoo.uisdk.screen.booking.booking

import android.content.Context
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.BraintreeSDKToken
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.model.Organisation
import com.karhoo.sdk.api.model.PaymentProvider
import com.karhoo.sdk.api.model.PaymentsNonce
import com.karhoo.sdk.api.model.Provider
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.network.request.SDKInitRequest
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.UnitTestUISDKConfig
import com.karhoo.uisdk.screen.booking.booking.payment.BraintreeBookingPaymentPresenter
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentMVP
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
class BookingPaymentPresenterTest {

    private var context: Context = mock()
    private var paymentsService: PaymentsService = mock()
    private var userStore: UserStore = mock()
    private var cardView: BookingPaymentMVP.View = mock()
    private var paymentView: PaymentMVP.View = mock()
    private val paymentProviderCall: Call<PaymentProvider> = mock()
    private val paymentProviderCaptor = argumentCaptor<(Resource<PaymentProvider>) -> Unit>()
    private val sdkInitCall: Call<BraintreeSDKToken> = mock()
    private val sdkInitCaptor = argumentCaptor<(Resource<BraintreeSDKToken>) -> Unit>()
    private val passBraintreeTokenCall: Call<PaymentsNonce> = mock()
    private val passBraintreeTokenCaptor = argumentCaptor<(Resource<PaymentsNonce>) -> Unit>()
    @Captor
    private lateinit var paymentInfoCaptor: ArgumentCaptor<SavedPaymentInfo>

    @Captor
    private lateinit var sdkInitialisationCaptor: ArgumentCaptor<SDKInitRequest>

    private lateinit var bookingPaymentPresenter: BookingPaymentPresenter
    private lateinit var braintreeBookingPaymentPresenter: BraintreeBookingPaymentPresenter

    @Before
    fun setUp() {
        whenever(paymentsService.initialisePaymentSDK(any()))
                .thenReturn(sdkInitCall)
        doNothing().whenever(sdkInitCall).execute(sdkInitCaptor.capture())

        whenever(paymentsService.getPaymentProvider()).thenReturn(paymentProviderCall)
        doNothing().whenever(paymentProviderCall).execute(paymentProviderCaptor.capture())

        whenever(paymentsService.addPaymentMethod(any()))
                .thenReturn(passBraintreeTokenCall)
        doNothing().whenever(passBraintreeTokenCall).execute(passBraintreeTokenCaptor.capture())

        whenever(userStore.currentUser).thenReturn(userDetails)

        bookingPaymentPresenter = BookingPaymentPresenter(
                paymentsService = paymentsService,
                view = cardView)

        braintreeBookingPaymentPresenter = BraintreeBookingPaymentPresenter(
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

        braintreeBookingPaymentPresenter.sdkInit()

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
        braintreeBookingPaymentPresenter.sdkInit()

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
        braintreeBookingPaymentPresenter.sdkInit()

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(cardView, never()).showPaymentUI(any())
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
        braintreeBookingPaymentPresenter.sdkInit()

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(paymentView, never()).showPaymentUI(any())
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
        braintreeBookingPaymentPresenter.sdkInit()

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(paymentView).showPaymentUI(any())
    }

    /**
     * Given:   A request is made to change card
     * When:    The request is unsuccessful
     * Then:    The view should be asked to show an error
     */
    @Test
    fun `change card pressed and result is unsuccessful`() {
        braintreeBookingPaymentPresenter.sdkInit()

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
        braintreeBookingPaymentPresenter.passBackNonce(BRAINTREE_SDK_TOKEN)

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
        braintreeBookingPaymentPresenter.passBackNonce(BRAINTREE_SDK_TOKEN)

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
        braintreeBookingPaymentPresenter.onSavedPaymentInfoChanged(SavedPaymentInfo("", CardType.NOT_SET))
        verify(paymentView).bindCardDetails(any())
    }

    /**
     * Given:   A user updates payment card details
     * Then:    The card info is stored and the view is not updated
     */
    @Test
    fun `card info stored and correct updates made to view if there is payment nonce info`() {
        val desc = "ending in 00"

        braintreeBookingPaymentPresenter.updateCardDetails(paymentsNonce.nonce, desc, "Visa")

        verify(userStore).savedPaymentInfo = capture(paymentInfoCaptor)
        verify(paymentView).refresh()
        assertEquals(CardType.VISA, paymentInfoCaptor.value.cardType)
        assertEquals(desc, paymentInfoCaptor.value.lastFour)
    }

    /**
     * Given: A request is made to fetch payment providers
     * Then: Fetch payment providers
     */
    @Test
    fun `fetch payment providers success`() {
        paymentProviderCaptor.firstValue.invoke(Resource.Success(PaymentProvider(ADYEN_PAYMENT)))

        verify(paymentsService).getPaymentProvider()
        verify(cardView, never()).showError(any())
    }

    /**
     * Given: A request is made to fetch payment providers
     * When: Fetch payment providers fails
     * Then: Show error
     */
    @Test
    fun `fetch payment providers failure`() {
        paymentProviderCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))

        verify(paymentsService).getPaymentProvider()
        verify(cardView).showError(any())
    }

    private fun setAuthenticatedUser() {
        KarhooUISDKConfigurationProvider.setConfig(configuration = UnitTestUISDKConfig(context =
                                                                                       context,
                                                                                       authenticationMethod = AuthenticationMethod.KarhooUser(), handleBraintree = false))
    }

    companion object {
        val paymentsNonce = PaymentsNonce(
                nonce = "1234557683749328",
                cardType = CardType.VISA,
                lastFour = "2345"
                                         )

        val BRAINTREE_SDK_TOKEN = "TEST TOKEN"

        val ADYEN_PAYMENT = Provider(id = "Adyen")

        val CARD_ENDING = "....12"

        val userDetails: UserInfo = UserInfo(firstName = "David",
                                             lastName = "Smith",
                                             email = "david.smith@email.com",
                                             phoneNumber = "+441234 56789",
                                             userId = "123",
                                             locale = "en-GB",
                                             organisations = listOf(Organisation(id = "organisation_id", name = "Organisation", roles = listOf("PERMISSION_ONE", "PERMISSION_TWO"))))

    }

}