package com.karhoo.uisdk.screen.booking.checkout.loyalty

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify

class LoyaltyViewPresenterTest {
    private lateinit var presenter: LoyaltyPresenter
    private var view: LoyaltyView = mock()

    @Before
    fun setUp() {
        presenter = LoyaltyPresenter()
        presenter.attachView(view)
    }

    @Test
    fun `When starting the loyalty presenter, the current mode is set to NONE`() {
        Assert.assertEquals(presenter.getCurrentMode(), LoyaltyMode.NONE)
    }

    @Test
    fun `When setting up a BURN mode, if the user can burn, then the view has the mode set`() {
        presenter.set(LoyaltyViewModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT,
                                       canEarn = false, canBurn = true))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)

        verify(view).set(LoyaltyMode.BURN)
    }

    @Test
    fun `When setting up a BURN mode, if the user cannot burn, then the mode isn't set`() {
        presenter.set(LoyaltyViewModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT,
                                       canEarn = false, canBurn = false))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)

        verify(view, never()).set(LoyaltyMode.BURN)
    }

    @Test
    fun `When setting up a BURN mode, if the user cannot burn, then the mode is set to NONE`() {
        presenter.set(LoyaltyViewModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT,
                                       canEarn = false, canBurn = false))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)

        Assert.assertEquals(presenter.getCurrentMode(), LoyaltyMode.NONE)
    }

    @Test
    fun `When setting up a EARN mode, if the user can earn, then the view has the mode set`() {
        presenter.set(LoyaltyViewModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT,
                                       canEarn = true, canBurn = true))
        presenter.updateLoyaltyMode(LoyaltyMode.EARN)

        verify(view).set(LoyaltyMode.EARN)
    }

    @Test
    fun `When setting up a EARN mode, if the user cannot earn, then the view has the mode set`() {
        presenter.set(LoyaltyViewModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT,
                                       canEarn = false, canBurn = true))
        presenter.updateLoyaltyMode(LoyaltyMode.EARN)

        verify(view, never()).set(LoyaltyMode.EARN)
    }

    @Test
    fun `When setting up a EARN mode, if the user cannot earn, then the mode is set to NONE`() {
        presenter.set(LoyaltyViewModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT,
                                       canEarn = false, canBurn = true))
        presenter.updateLoyaltyMode(LoyaltyMode.EARN)

        Assert.assertEquals(presenter.getCurrentMode(), LoyaltyMode.NONE)
    }

    @Test
    fun `When setting a mode which is already set, then the component does nothing`() {
        presenter.set(LoyaltyViewModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT,
                                       canEarn = false, canBurn = true))
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)
        presenter.updateLoyaltyMode(LoyaltyMode.BURN)

        //The first set to burn mode
        verify(view, times(1)).set(LoyaltyMode.BURN)
    }

    @Test
    fun `When setting a view model with canBurn, then the view hides the switch`() {
        presenter.set(LoyaltyViewModel(LOYALTY_ID, LOYALTY_CURRENCY, LOYALTY_AMOUNT,
                                       canEarn = false, canBurn = false))
        //The first set to burn mode
        verify(view).updateLoyaltyFeatures(showEarnRelatedUI = false, showBurnRelatedUI = false)
    }


    companion object {
        private const val LOYALTY_ID = "1"
        private const val LOYALTY_CURRENCY = "GBP"
        private const val LOYALTY_AMOUNT = 10.0
    }

}
