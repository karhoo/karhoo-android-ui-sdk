package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.res.Resources
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.LoyaltyPoints
import com.karhoo.sdk.api.model.LoyaltyProgramme
import com.karhoo.sdk.api.model.LoyaltyStatus
import com.karhoo.sdk.api.model.PaymentProvider
import com.karhoo.sdk.api.model.Provider
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.loyalty.LoyaltyService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.R
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class LoyaltyViewPresenterTest {
    private lateinit var presenter: LoyaltyPresenter
    private var view: LoyaltyView = mock()
    private var userStore: UserStore = mock()
    private var resources: Resources = mock()
    private var loyaltyService: LoyaltyService = mock()
    private var loyaltyStatusCall: Call<LoyaltyStatus> = mock()
    private var loyaltyBurnedPointsCall: Call<LoyaltyPoints> = mock()
    private var loyaltyEarnedPointsCall: Call<LoyaltyPoints> = mock()
    private val lambdaCaptor = argumentCaptor<(Resource<LoyaltyStatus>) -> Unit>()
    private val lambdaCaptorLoyaltyPoints = argumentCaptor<(Resource<LoyaltyPoints>) -> Unit>()

    @Before
    fun setUp() {
        whenever(view.provideResources()).thenReturn(resources)
        whenever(resources.getString(R.string.kh_uisdk_loyalty_use_points_on_subtitle)).thenReturn(BURN_POINTS_SUBTITLE)
        whenever(resources.getString(R.string.kh_uisdk_loyalty_points_earned_for_trip)).thenReturn(EARN_POINTS_SUBTITLE)
        whenever(resources.getString(R.string.kh_uisdk_loyalty_unsupported_currency)).thenReturn(CURRENCY_NOT_SUPPORTED_SUBTITLE)
        whenever(resources.getString(R.string.kh_uisdk_loyalty_insufficient_balance_for_loyalty_burn)).thenReturn(INSUFFICIENT_BALANCE_SUBTITLE)

        whenever(resources.getString(R.string.kh_uisdk_loyalty_points_earned_for_trip))
            .thenReturn(EARN_POINTS_SUBTITLE)
        whenever(resources.getString(R.string.kh_uisdk_loyalty_use_points_off_subtitle))
            .thenReturn(EARN_POINTS_SUBTITLE_OFF)

        whenever(resources.getString(R.string.kh_uisdk_loyalty_use_points_on_subtitle))
            .thenReturn(BURN_POINTS_SUBTITLE)

        whenever(userStore.paymentProvider).thenReturn(PaymentProvider(
                Provider("id"),
                LoyaltyProgramme("1", "")))
        whenever(loyaltyService.getLoyaltyStatus(LOYALTY_ID)).thenReturn(loyaltyStatusCall)
        doReturn(loyaltyBurnedPointsCall).`when`(loyaltyService).getLoyaltyBurn(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT.toInt())

        whenever(loyaltyService.getLoyaltyEarn(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT.toInt
        (), 0))
                .thenReturn(loyaltyEarnedPointsCall)

        doNothing().whenever(loyaltyStatusCall).execute(lambdaCaptor.capture())
        doNothing().whenever(loyaltyBurnedPointsCall).execute(lambdaCaptorLoyaltyPoints.capture())
        doNothing().whenever(loyaltyEarnedPointsCall).execute(lambdaCaptorLoyaltyPoints.capture())

        presenter = LoyaltyPresenter(userStore, loyaltyService)
        presenter.loyaltyPresenterDelegate = view
    }

    @Test
    fun `When starting the loyalty presenter, the current mode is set to NONE`() {
        Assert.assertEquals(presenter.getCurrentMode(), LoyaltyMode.NONE)
    }

    @Test
    fun `When setting up a BURN mode, if the user can burn, then the view has the mode set`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false,
                                          canBurn = true)
        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)
        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))

        verify(view).set(LoyaltyMode.BURN)
    }

    @Test
    fun `When setting up a BURN mode, if the user cannot burn, then the mode isn't set`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false, canBurn = false)
        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)

        verify(view, never()).set(LoyaltyMode.BURN)
    }

    @Test
    fun `When setting up a BURN mode, if the user cannot burn, then the mode is set to NONE`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false, canBurn = false)
        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(loyaltyStatus)
        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)


        Assert.assertEquals(presenter.getCurrentMode(), LoyaltyMode.NONE)
    }

    @Test
    fun `When setting up a EARN mode, if the user can earn, then the view has the mode set`() {
        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(LoyaltyStatus(LOYALTY_POINTS, canEarn = true,
                                                                   canBurn = true))
        presenter.updateLoyaltyMode(LoyaltyMode.EARN)

        verify(view).set(LoyaltyMode.EARN)
    }

    @Test
    fun `When setting up a EARN mode, if the user cannot earn, then the view has the mode set`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false, canBurn = true)
        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))
        presenter.updateLoyaltyMode(LoyaltyMode.EARN)

        verify(view, never()).set(LoyaltyMode.EARN)
    }

    @Test
    fun `When setting up a EARN mode, if the user cannot earn, then the mode is set to NONE`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false, canBurn = true)
        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(loyaltyStatus)
        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))

        presenter.updateLoyaltyMode(LoyaltyMode.EARN)

        Assert.assertEquals(presenter.getCurrentMode(), LoyaltyMode.NONE)
    }

    @Test
    fun `When setting a view model with canBurn, then the view hides the switch`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false,
                                          canBurn = false)

        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(loyaltyStatus)
        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))
        //The first set to burn mode
        verify(view).toggleFeatures(earnOn = false, burnON = false)
    }

    @Test
    fun `When getting the burned points number, the loyalty subtitle shows the burned points number`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false,
                                          canBurn = true)

        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(loyaltyStatus)
        presenter.updateBurnedPoints()
        lambdaCaptorLoyaltyPoints.firstValue.invoke(Resource.Success(LoyaltyPoints(1)))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)
        verify(view).updateWith(LoyaltyMode.BURN, burnSubtitle = "Pay 0.10 GBP with 1 loyalty points")
    }

    @Test
    fun `After getting the burned points number, if the balance is insufficient, show error`() {
        val loyaltyStatus = LoyaltyStatus(1, canEarn = false,
                                          canBurn = true)

        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(loyaltyStatus)

        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))

        presenter.updateBurnedPoints()
        lambdaCaptorLoyaltyPoints.secondValue.invoke(Resource.Success(LoyaltyPoints(LOYALTY_POINTS)))

        verify(view).updateWith(LoyaltyMode.ERROR_INSUFFICIENT_FUNDS, errorMessage = INSUFFICIENT_BALANCE_SUBTITLE)
    }

    @Test
    fun `When failing to get the burned points number, the loyalty component is hidden`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false,
                                          canBurn = true)

        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(loyaltyStatus)

        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))

        lambdaCaptorLoyaltyPoints.firstValue.invoke(Resource.Failure(KarhooError.LoyaltyUnknownCurrency))
        verify(view).updateWith(LoyaltyMode.ERROR_BAD_CURRENCY, errorMessage = CURRENCY_NOT_SUPPORTED_SUBTITLE)
    }

    @Test
    fun `When retrieving the earned points number, the loyalty subtitle shows the burned points number`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false,
                                          canBurn = true)

        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(loyaltyStatus)
        presenter.updateEarnedPoints()
        lambdaCaptorLoyaltyPoints.firstValue.invoke(Resource.Success(LoyaltyPoints(1)))
        verify(view).updateWith(
            LoyaltyMode.EARN, earnSubtitle = "1 loyalty points will be added to your account balance at " +
                                            "the end of the ride")
    }

    @Test
    fun `When failing to get the earned points number, the loyalty component is hidden`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false,
                                          canBurn = true)

        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(loyaltyStatus)

        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))

        lambdaCaptorLoyaltyPoints.firstValue.invoke(Resource.Failure(KarhooError.LoyaltyUnknownCurrency))
        verify(view).updateWith(LoyaltyMode.ERROR_BAD_CURRENCY, errorMessage = CURRENCY_NOT_SUPPORTED_SUBTITLE)
    }

    @Test
    fun `When failing to get the points number, the loyalty balance is hidden`() {
        val loyaltyStatus = LoyaltyStatus(null, canEarn = false,
                                          canBurn = true)

        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(loyaltyStatus)

        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))

        verify(view, never()).setBalancePoints(LOYALTY_POINTS)
    }

    @Test
    fun `When having points number, the loyalty balance is shown`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false,
                                          canBurn = true)

        presenter.set(LoyaltyViewDataModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(loyaltyStatus)

        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))

        verify(view).setBalancePoints(LOYALTY_POINTS)
    }

    companion object {
        private const val LOYALTY_ID = "1"
        private const val LOYALTY_CURRENCY = "GBP"
        private const val LOYALTY_AMOUNT = 10.0
        private const val LOYALTY_POINTS = 10
        private const val CURRENCY_NOT_SUPPORTED_SUBTITLE = "An error has occurred. The currency is not supported"
        private const val INSUFFICIENT_BALANCE_SUBTITLE = "Your points balance is insufficient"
        private const val EARN_POINTS_SUBTITLE = "%1\$s loyalty points will be added to your " +
                "account balance at the end of the ride"
        private const val EARN_POINTS_SUBTITLE_OFF = "You can enable your loyalty points to pay for the ride"
        private const val BURN_POINTS_SUBTITLE = "Pay %s %s with %s loyalty points"
    }

}
