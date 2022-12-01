package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.res.Resources
import com.karhoo.sdk.api.model.LoyaltyNonce
import com.karhoo.sdk.api.model.LoyaltyStatus
import com.karhoo.sdk.api.network.response.Resource

interface LoyaltyContract {
    interface View {
        var delegate: LoyaltyViewDelegate?

        fun getCurrentMode(): LoyaltyMode
        fun set(loyaltyDataModel: LoyaltyViewDataModel, callback: ((Resource<LoyaltyStatus>) -> Unit?)? = null)
        fun getLoyaltyPreAuthNonce(callback: (Resource<LoyaltyNonce>, LoyaltyStatus?) -> Unit)
        fun getPoints(): Int?
    }

    interface LoyaltyPresenterDelegate {
        fun updateWith(
            mode: LoyaltyMode? = null,
            earnSubtitle: String? = null,
            burnSubtitle: String? = null,
            errorMessage: String? = null
        )

        fun toggleFeatures(earnOn: Boolean, burnON: Boolean)
        fun provideResources(): Resources
        fun showBalance(show: Boolean, points: Int = 0)
        fun set(mode: LoyaltyMode)
    }

    interface Presenter {
        var loyaltyPresenterDelegate: LoyaltyPresenterDelegate?
        var loyaltyViewDelegate: LoyaltyViewDelegate?

        fun set(loyaltyDataModel: LoyaltyViewDataModel)
        fun updateEarnedPoints()
        fun updateBurnedPoints()
        fun updateBalancePoints()
        fun updateLoyaltyMode(mode: LoyaltyMode)
        fun getCurrentMode(): LoyaltyMode
        fun getLoyaltyStatus(callback: ((Resource<LoyaltyStatus>) -> Unit?)? = null)
        fun getLoyaltyPreAuthNonce(callback: (Resource<LoyaltyNonce>, LoyaltyStatus?) -> Unit)
        fun getPoints(): Int?
    }

    interface LoyaltyViewDelegate {
        fun onModeChanged(mode: LoyaltyMode)
        fun onStartLoading()
        fun onEndLoading()
    }
}
