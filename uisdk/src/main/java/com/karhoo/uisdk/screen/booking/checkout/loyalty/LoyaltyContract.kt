package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.res.Resources

interface LoyaltyContract {
    interface View {
        fun getCurrentMode(): LoyaltyMode
        fun set(mode: LoyaltyMode)
        fun set(loyaltyDataModel: LoyaltyViewDataModel)
        fun setSubtitle(subtitle: String)
        fun provideResources(): Resources
        fun showError(message: String)
        fun getLoyaltyStatus()
        fun updateLoyaltyFeatures(showEarnRelatedUI: Boolean, showBurnRelatedUI: Boolean)
        fun setLoyaltyModeCallback(loyaltyModeCallback: LoyaltyModeCallback)
        fun preAuthorize()
        fun setBalancePoints(points: Int)
    }

    interface Presenter {
        fun attachView(view: View)
        fun set(loyaltyDataModel: LoyaltyViewDataModel)
        fun updateEarnedPoints()
        fun updateBurnedPoints()
        fun updateBalancePoints()
        fun updateLoyaltyMode(mode: LoyaltyMode)
        fun getCurrentMode(): LoyaltyMode
        fun getLoyaltyStatus()
        fun getSubtitleBasedOnMode()
        fun setLoyaltyModeCallback(loyaltyModeCallback: LoyaltyModeCallback)
        fun preAuthorize()
    }

    interface LoyaltyModeCallback {
        fun onModeChanged(mode: LoyaltyMode)
        fun onPreAuthorized(nonce: String)
        fun onPreAuthorizationError(reasonId: Int)
    }
}
