package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.res.Resources
import com.karhoo.sdk.api.model.LoyaltyStatus

interface LoyaltyContract {
    interface View {
        fun getCurrentMode(): LoyaltyMode
        fun set(mode: LoyaltyMode)
        fun set(loyaltyStatus: LoyaltyStatus)
        fun set(loyaltyRequest: LoyaltyViewRequest)
        fun showError(message: String)
        fun updateLoyaltyFeatures(showEarnRelatedUI: Boolean, showBurnRelatedUI: Boolean)
    }

    interface Presenter {
        fun attachView(view: View)
        fun set(loyaltyStatus: LoyaltyStatus)
        fun set(loyaltyRequest: LoyaltyViewRequest)
        fun updateEarnedPoints()
        fun updateBurnedPoints()
        fun updateLoyaltyMode(mode: LoyaltyMode)
        fun getCurrentMode(): LoyaltyMode
        fun getSubtitleBasedOnMode(resources: Resources): String
    }
}
