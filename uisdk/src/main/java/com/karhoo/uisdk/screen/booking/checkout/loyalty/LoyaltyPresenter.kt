package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.res.Resources
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.LoyaltyStatus
import com.karhoo.uisdk.R

class LoyaltyPresenter : LoyaltyContract.Presenter {
    private var currentMode: LoyaltyMode = LoyaltyMode.NONE

    private lateinit var view: LoyaltyContract.View
    private var loyaltyRequest: LoyaltyViewRequest? = null
    private var loyaltyStatus: LoyaltyStatus? = KarhooApi.userStore.loyaltyStatus

    override fun attachView(view: LoyaltyContract.View) {
        this.view = view
    }

    override fun updateLoyaltyMode(mode: LoyaltyMode) {
        if (currentMode == mode) {
            return
        }

        currentMode = if (mode == LoyaltyMode.BURN && loyaltyStatus?.burnable == false) {
            return
        } else if (mode == LoyaltyMode.EARN && loyaltyStatus?.earnable == false) {
            LoyaltyMode.NONE
        } else {
            mode
        }

        val canEarn = loyaltyStatus?.earnable ?: false
        val canBurn = loyaltyStatus?.burnable ?: false

        view.updateLoyaltyFeatures(canEarn, canBurn)
        view.set(currentMode)
    }

    override fun set(loyaltyStatus: LoyaltyStatus) {
        this.loyaltyStatus = loyaltyStatus

        val canEarn = loyaltyStatus.earnable ?: false
        val canBurn = loyaltyStatus.burnable ?: false

        view.updateLoyaltyFeatures(canEarn, canBurn)
    }

    override fun set(loyaltyRequest: LoyaltyViewRequest) {
        this.loyaltyRequest = loyaltyRequest
    }

    override fun updateEarnedPoints() {
        //nothing
    }

    override fun updateBurnedPoints() {
        //nothing
    }

    override fun getSubtitleBasedOnMode(resources: Resources): String {
        return when (currentMode) {
            LoyaltyMode.BURN -> {
                String.format(
                        resources.getString(R.string.kh_uisdk_loyalty_points_burned_for_trip),
                        loyaltyRequest?.tripAmount?.toInt())
            }
            LoyaltyMode.EARN -> {
                String.format(
                        resources.getString(R.string.kh_uisdk_loyalty_points_earned_for_trip),
                        loyaltyRequest?.tripAmount?.toInt())
            }
            else -> {
                String.format(
                        resources.getString(R.string.kh_uisdk_loyalty_points_earned_for_trip),
                        loyaltyRequest?.tripAmount?.toInt())
            }
        }
    }

    override fun getCurrentMode(): LoyaltyMode {
        return currentMode
    }

}
