package com.karhoo.uisdk.screen.booking.checkout.loyalty

import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.LoyaltyProgramme
import com.karhoo.sdk.api.model.LoyaltyStatus
import com.karhoo.sdk.api.model.Provider
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.loyalty.LoyaltyService
import com.karhoo.sdk.call.Call
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever

class LoyaltyViewPresenterTest {
    private lateinit var presenter: LoyaltyPresenter
    private var view: LoyaltyView = mock()
    private var userStore: UserStore = mock()
    private var loyaltyService: LoyaltyService = mock()
    private var loyaltyStatusCall: Call<LoyaltyStatus> = mock()
    private val lambdaCaptor = argumentCaptor<(Resource<LoyaltyStatus>) -> Unit>()

    @Before
    fun setUp() {
//        whenever(userStore.loyaltyStatus).thenReturn(null)
        whenever(userStore.paymentProvider).thenReturn(Provider("id", LoyaltyProgramme("1", "")))
        whenever(loyaltyService.getLoyaltyStatus(LOYALTY_ID)).thenReturn(loyaltyStatusCall)

        doNothing().whenever(loyaltyStatusCall).execute(lambdaCaptor.capture())

        presenter = LoyaltyPresenter(userStore, loyaltyService)
        presenter.attachView(view)
    }

    @Test
    fun `When starting the loyalty presenter, the current mode is set to NONE`() {
        Assert.assertEquals(presenter.getCurrentMode(), LoyaltyMode.NONE)
    }

    @Test
    fun `When setting up a BURN mode, if the user can burn, then the view has the mode set`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false,
                                          canBurn = true)
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)
        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))

        verify(view).set(LoyaltyMode.BURN)
    }

    @Test
    fun `When setting up a BURN mode, if the user cannot burn, then the mode isn't set`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false, canBurn = false)
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)

        verify(view, never()).set(LoyaltyMode.BURN)
    }

    @Test
    fun `When setting up a BURN mode, if the user cannot burn, then the mode is set to NONE`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false, canBurn = false)
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(loyaltyStatus)
        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)


        Assert.assertEquals(presenter.getCurrentMode(), LoyaltyMode.NONE)
    }

    @Test
    fun `When setting up a EARN mode, if the user can earn, then the view has the mode set`() {
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(LoyaltyStatus(LOYALTY_POINTS, canEarn = true,
                                                                   canBurn = true))
        presenter.updateLoyaltyMode(LoyaltyMode.EARN)

        verify(view).set(LoyaltyMode.EARN)
    }

    @Test
    fun `When setting up a EARN mode, if the user cannot earn, then the view has the mode set`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false, canBurn = true)
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))
        presenter.updateLoyaltyMode(LoyaltyMode.EARN)

        verify(view, never()).set(LoyaltyMode.EARN)
    }

    @Test
    fun `When setting up a EARN mode, if the user cannot earn, then the mode is set to NONE`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false, canBurn = true)
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(loyaltyStatus)
        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))

        presenter.updateLoyaltyMode(LoyaltyMode.EARN)

        Assert.assertEquals(presenter.getCurrentMode(), LoyaltyMode.NONE)
    }

    @Test
    fun `When setting a mode which is already set, then the component does nothing`() {
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(LoyaltyStatus(LOYALTY_POINTS, canEarn = false,
                                                                   canBurn = true))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)

        //The first set to burn mode
        verify(view, times(1)).set(LoyaltyMode.BURN)
    }

    @Test
    fun `When setting a view model with canBurn, then the view hides the switch`() {
        val loyaltyStatus = LoyaltyStatus(LOYALTY_POINTS, canEarn = false,
                                          canBurn = false)

        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        whenever(userStore.loyaltyStatus).thenReturn(loyaltyStatus)
        presenter.getLoyaltyStatus()
        lambdaCaptor.firstValue.invoke(Resource.Success(loyaltyStatus))
        //The first set to burn mode
        verify(view).updateLoyaltyFeatures(showEarnRelatedUI = false, showBurnRelatedUI = false)
    }

    companion object {
        private const val LOYALTY_ID = "1"
        private const val LOYALTY_CURRENCY = "GBP"
        private const val LOYALTY_AMOUNT = 10.0
        private const val LOYALTY_POINTS = 10
    }

}