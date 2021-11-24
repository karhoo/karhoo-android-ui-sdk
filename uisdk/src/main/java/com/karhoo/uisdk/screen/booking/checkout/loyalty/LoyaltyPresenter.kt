package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.res.Resources
import com.karhoo.uisdk.R

class LoyaltyPresenter : LoyaltyMVP.Presenter {
    private var currentMode: LoyaltyMode = LoyaltyMode.NONE

    private lateinit var view: LoyaltyMVP.View
    private lateinit var viewModel: LoyaltyViewModel

    override fun attachView(view: LoyaltyMVP.View) {
        this.view = view
    }

    override fun updateLoyaltyMode(mode: LoyaltyMode) {
        if (currentMode == mode) {
            return
        }

        val canEarn = viewModel.canEarn
        val canBurn = viewModel.canBurn

        view.updateLoyaltyFeatures(canEarn, canBurn)

        currentMode = if (mode == LoyaltyMode.BURN && !canBurn) {
            return
        } else if (mode == LoyaltyMode.EARN && !canEarn) {
            LoyaltyMode.NONE
        } else {
            mode
        }

        view.set(currentMode)
    }

    override fun set(viewModel: LoyaltyViewModel) {
        this.viewModel = viewModel

        val canEarn = viewModel.canEarn
        val canBurn = viewModel.canBurn

        view.updateLoyaltyFeatures(canEarn, canBurn)
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
                resources.getString(R.string.kh_uisdk_loyalty_points_burned_for_trip)
            }
            LoyaltyMode.EARN -> {
                resources.getString(R.string.kh_uisdk_loyalty_points_earned_for_trip)
            }
            else -> {
                return ""
            }
        }
    }

    override fun getCurrentMode(): LoyaltyMode {
        return currentMode
    }

}
