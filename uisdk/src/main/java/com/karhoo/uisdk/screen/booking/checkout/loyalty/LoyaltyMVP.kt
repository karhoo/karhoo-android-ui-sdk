package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.res.Resources

interface LoyaltyMVP {
    interface View {
        fun getCurrentMode(): LoyaltyMode
        fun set(mode: LoyaltyMode)
        fun set(viewModel: LoyaltyViewModel)
        fun showError(message: String)
        fun updateLoyaltyFeatures(showEarnRelatedUI: Boolean, showBurnRelatedUI: Boolean)
    }

    interface Presenter {
        fun attachView(view: View)
        fun set(viewModel: LoyaltyViewModel)
        fun updateEarnedPoints()
        fun updateBurnedPoints()
        fun updateLoyaltyMode(mode: LoyaltyMode)
        fun getCurrentMode(): LoyaltyMode
        fun getSubtitleBasedOnMode(resources: Resources): String
    }
}
