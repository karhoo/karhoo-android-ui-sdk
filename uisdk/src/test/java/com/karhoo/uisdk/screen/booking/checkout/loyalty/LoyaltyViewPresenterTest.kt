package com.karhoo.uisdk.screen.booking.checkout.loyalty

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.LoyaltyStatus
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

    @Before
    fun setUp() {
        whenever(userStore.loyaltyStatus).thenReturn(null)
        presenter = LoyaltyPresenter(userStore)
        presenter.attachView(view)
    }

    @Test
    fun `When starting the loyalty presenter, the current mode is set to NONE`() {
        Assert.assertEquals(presenter.getCurrentMode(), LoyaltyMode.NONE)
    }

    @Test
    fun `When setting up a BURN mode, if the user can burn, then the view has the mode set`() {
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.set(LoyaltyStatus(LOYALTY_POINTS, earnable = false, burnable = true))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)

        verify(view).set(LoyaltyMode.BURN)
    }

    @Test
    fun `When setting up a BURN mode, if the user cannot burn, then the mode isn't set`() {
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.set(LoyaltyStatus(LOYALTY_POINTS, earnable = false, burnable = false))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)

        verify(view, never()).set(LoyaltyMode.BURN)
    }

    @Test
    fun `When setting up a BURN mode, if the user cannot burn, then the mode is set to NONE`() {
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.set(LoyaltyStatus(LOYALTY_POINTS, earnable = false, burnable = false))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)

        Assert.assertEquals(presenter.getCurrentMode(), LoyaltyMode.NONE)
    }

    @Test
    fun `When setting up a EARN mode, if the user can earn, then the view has the mode set`() {
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.set(LoyaltyStatus(LOYALTY_POINTS, earnable = true, burnable = true))
        presenter.updateLoyaltyMode(LoyaltyMode.EARN)

        verify(view).set(LoyaltyMode.EARN)
    }

    @Test
    fun `When setting up a EARN mode, if the user cannot earn, then the view has the mode set`() {
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.set(LoyaltyStatus(LOYALTY_POINTS, earnable = false, burnable = true))
        presenter.updateLoyaltyMode(LoyaltyMode.EARN)

        verify(view, never()).set(LoyaltyMode.EARN)
    }

    @Test
    fun `When setting up a EARN mode, if the user cannot earn, then the mode is set to NONE`() {
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.set(LoyaltyStatus(LOYALTY_POINTS, earnable = false, burnable = true))
        presenter.updateLoyaltyMode(LoyaltyMode.EARN)

        Assert.assertEquals(presenter.getCurrentMode(), LoyaltyMode.NONE)
    }

    @Test
    fun `When setting a mode which is already set, then the component does nothing`() {
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.set(LoyaltyStatus(LOYALTY_POINTS, earnable = false, burnable = true))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)

        //The first set to burn mode
        verify(view, times(1)).set(LoyaltyMode.BURN)
    }

    @Test
    fun `When setting a view model with canBurn, then the view hides the switch`() {
        presenter.set(LoyaltyViewRequest(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT))
        presenter.set(LoyaltyStatus(LOYALTY_POINTS, earnable = false, burnable = false))
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
