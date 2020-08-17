package com.karhoo.uisdk.screen.booking.booking

import android.content.Context
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.BraintreeSDKToken
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.model.Organisation
import com.karhoo.sdk.api.model.PaymentsNonce
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.network.request.SDKInitRequest
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.UnitTestUISDKConfig
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
    private var view: BookingPaymentMVP.View = mock()
    private val sdkInitCall: Call<BraintreeSDKToken> = mock()
    private val sdkInitCaptor = argumentCaptor<(Resource<BraintreeSDKToken>) -> Unit>()
    private val passBraintreeTokenCall: Call<PaymentsNonce> = mock()
    private val passBraintreeTokenCaptor = argumentCaptor<(Resource<PaymentsNonce>) -> Unit>()
    @Captor private lateinit var paymentInfoCaptor: ArgumentCaptor<SavedPaymentInfo>

    @Captor
    private lateinit var sdkInitialisationCaptor: ArgumentCaptor<SDKInitRequest>

    private lateinit var bookingPaymentPresenter: BookingPaymentPresenter

    @Before
    fun setUp() {
        whenever(paymentsService.initialisePaymentSDK(any()))
                .thenReturn(sdkInitCall)
        doNothing().whenever(sdkInitCall).execute(sdkInitCaptor.capture())

        whenever(paymentsService.addPaymentMethod(any()))
                .thenReturn(passBraintreeTokenCall)
        doNothing().whenever(passBraintreeTokenCall).execute(passBraintreeTokenCaptor.capture())

        whenever(userStore.currentUser).thenReturn(userDetails)

        bookingPaymentPresenter = BookingPaymentPresenter(
                paymentsService = paymentsService,
                userStore = userStore,
                view = view
                                                         )
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

        bookingPaymentPresenter.changeCard()

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
        bookingPaymentPresenter.changeCard()

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
        bookingPaymentPresenter.changeCard()

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(view, never()).showPaymentUI(any())
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
        bookingPaymentPresenter.changeCard()

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(view, never()).showPaymentUI(any())
        verify(view).handlePaymentDetailsUpdate(BRAINTREE_SDK_TOKEN)
        verify(userStore).savedPaymentInfo = capture(paymentInfoCaptor)
        verify(view).refresh()
        assertEquals(CardType.VISA, paymentInfoCaptor.value.cardType)
        assertEquals(CARD_ENDING, paymentInfoCaptor.value.lastFour)
        verify(view).refresh()
    }

    /**
     * Given:   A request is made to change card
     * When:    The request is successful
     * Then:    The view should be asked to show payment UI
     */
    @Test
    fun `change card pressed and result is successful`() {
        setAuthenticatedUser()
        bookingPaymentPresenter.changeCard()

        sdkInitCaptor.firstValue.invoke(Resource.Success(BraintreeSDKToken(BRAINTREE_SDK_TOKEN)))

        verify(view).showPaymentUI(any())
    }

    /**
     * Given:   A request is made to change card
     * When:    The request is unsuccessful
     * Then:    The view should be asked to show an error
     */
    @Test
    fun `change card pressed and result is unsuccessful`() {
        bookingPaymentPresenter.changeCard()

        sdkInitCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).showError(any())
    }

    /**
     * Given:   A request is made to pass the braintree token to the BE
     * When:    The request is successful
     * Then:    The view should be asked to bind the card details
     */
    @Test
    fun `pass braintree token to BE and bind card details should be called`() {
        bookingPaymentPresenter.passBackBraintreeSDKNonce(BRAINTREE_SDK_TOKEN)

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
        bookingPaymentPresenter.passBackBraintreeSDKNonce(BRAINTREE_SDK_TOKEN)

        passBraintreeTokenCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).showError(any())
    }

    /**
     * Given:   The user has been changed
     * When:    The presenter is subscribed to the observer
     * Then:    A call should be made to update the payment info for the user
     */
    @Test
    fun `user update calls the view to display the new payment info`() {
        bookingPaymentPresenter.onSavedPaymentInfoChanged(SavedPaymentInfo("", CardType.NOT_SET))
        verify(view).bindCardDetails(any())
    }

    /**
     * Given:   A user updates payment card details
     * Then:    The card info is stored and the view is not updated
     */
    @Test
    fun `card info stored and correct updates made to view if there is payment nonce info`() {
        val desc = "ending in 00"

        bookingPaymentPresenter.updateCardDetails(desc, "Visa")

        verify(userStore).savedPaymentInfo = capture(paymentInfoCaptor)
        verify(view).refresh()
        assertEquals(CardType.VISA, paymentInfoCaptor.value.cardType)
        assertEquals(desc, paymentInfoCaptor.value.lastFour)
    }

    /**
     * Given: A request is made to fetch payment providers
     * Then: Fetch payment providers
     */
    @Test
    fun `fetch payment providers`() {
        
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