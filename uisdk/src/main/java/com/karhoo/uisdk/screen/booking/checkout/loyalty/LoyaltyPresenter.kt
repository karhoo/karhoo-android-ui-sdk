package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.res.Resources
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.LoyaltyStatus
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.loyalty.LoyaltyService
import com.karhoo.uisdk.R

class LoyaltyPresenter(val userStore: UserStore = KarhooApi.userStore,
                       private val loyaltyService: LoyaltyService = KarhooApi.loyaltyService) : LoyaltyContract
                                                                                                .Presenter {
    private var currentMode: LoyaltyMode = LoyaltyMode.NONE

    private lateinit var view: LoyaltyContract.View
    private var loyaltyRequest: LoyaltyViewRequest? = null
    private var loyaltyStatus: LoyaltyStatus? = userStore.loyaltyStatus

    override fun attachView(view: LoyaltyContract.View) {
        this.view = view
    }

    override fun updateLoyaltyMode(mode: LoyaltyMode) {
        if (currentMode == mode) {
            return
        }

        currentMode = if (mode == LoyaltyMode.BURN && loyaltyStatus?.canBurn == false) {
            return
        } else if (mode == LoyaltyMode.EARN && loyaltyStatus?.canEarn == false) {
            LoyaltyMode.NONE
        } else {
            mode
        }

        val canEarn = loyaltyStatus?.canEarn ?: false
        val canBurn = loyaltyStatus?.canBurn ?: false

        view.updateLoyaltyFeatures(canEarn, canBurn)
        view.set(currentMode)
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

    override fun getLoyaltyStatus() {
        val loyaltyId = userStore.paymentProvider?.loyalty?.loyaltyID
        loyaltyId?.let {
            loyaltyService.getLoyaltyStatus(loyaltyId).execute { result ->
                when (result) {
                    is Resource.Success -> {
                        userStore.loyaltyStatus = result.data
                        set(result.data)
                    }
                    is Resource.Failure ->
                        view.updateLoyaltyFeatures(showEarnRelatedUI = false, showBurnRelatedUI = false)
                }
            }
        }
    }

    private fun set(loyaltyStatus: LoyaltyStatus) {
        this.loyaltyStatus = loyaltyStatus

        val canEarn = loyaltyStatus.canEarn ?: false
        val canBurn = loyaltyStatus.canBurn ?: false

        view.updateLoyaltyFeatures(canEarn, canBurn)
    }
}
