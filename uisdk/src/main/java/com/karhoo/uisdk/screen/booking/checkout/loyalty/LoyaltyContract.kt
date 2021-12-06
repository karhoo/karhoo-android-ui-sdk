package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.res.Resources

interface LoyaltyContract {
    interface View {
        fun getCurrentMode(): LoyaltyMode
        fun set(mode: LoyaltyMode)
        fun set(loyaltyRequest: LoyaltyViewRequest)
        fun setSubtitle(subtitle: String)
        fun provideResources(): Resources
        fun showError(message: String)
        fun getLoyaltyStatus()
        fun updateLoyaltyFeatures(showEarnRelatedUI: Boolean, showBurnRelatedUI: Boolean)
    }

    interface Presenter {
        fun attachView(view: View)
        fun set(loyaltyRequest: LoyaltyViewRequest)
        fun updateEarnedPoints()
        fun updateBurnedPoints()
        fun updateLoyaltyMode(mode: LoyaltyMode)
        fun getCurrentMode(): LoyaltyMode
        fun getLoyaltyStatus()
        fun getSubtitleBasedOnMode(resources: Resources)
    }
}
