package com.karhoo.uisdk.screen.booking.checkout.loyalty

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.LoyaltyStatus
import com.karhoo.sdk.api.network.request.LoyaltyPreAuthPayload
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.loyalty.LoyaltyService
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.formatted
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired
import java.util.Currency

class LoyaltyPresenter(val userStore: UserStore = KarhooApi.userStore,
                       private val loyaltyService: LoyaltyService = KarhooApi.loyaltyService) : LoyaltyContract
                                                                                                .Presenter {
    private var currentMode: LoyaltyMode = LoyaltyMode.NONE
        set(value) {
            field = value
            loyaltyModeCallback?.onModeChanged(currentMode)
        }

    private lateinit var view: LoyaltyContract.View
    private var loyaltyDataModel: LoyaltyViewDataModel? = null
    private var loyaltyStatus: LoyaltyStatus? = userStore.loyaltyStatus
    private var burnedPoints: Int? = null
    private var earnedPoints: Int? = null
    private var loyaltyModeCallback: LoyaltyContract.LoyaltyModeCallback? = null

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

        val hasInsufficientPoints: Boolean = loyaltyStatus?.points?.compareTo(burnedPoints ?: 0)
                ?: 0 < 0

        if (canBurn && hasInsufficientPoints && currentMode == LoyaltyMode.BURN) {
            view.setBurnSubtitle(view.provideResources().getString(R.string.kh_uisdk_loyalty_insufficient_balance_for_loyalty_burn))
            currentMode = LoyaltyMode.ERROR
            return
        }

        setSubtitleBasedOnMode(currentMode, true)

        view.updateLoyaltyFeatures(canEarn, canBurn)
        view.set(currentMode)
    }

    override fun set(loyaltyDataModel: LoyaltyViewDataModel) {
        this.loyaltyDataModel = loyaltyDataModel
    }

    override fun updateEarnedPoints() {
        val loyaltyId = userStore.paymentProvider?.loyalty?.id
        val currency = loyaltyDataModel?.currency
        val tripAmount = loyaltyDataModel?.tripAmount?.toInt()

        if (loyaltyId != null && currency != null && tripAmount != null) {
            loyaltyService.getLoyaltyEarn(loyaltyId, currency, tripAmount, 0)
                    .execute { result ->
                        when (result) {
                            is Resource.Success -> {
                                earnedPoints = result.data.points

                                setSubtitleBasedOnMode(LoyaltyMode.EARN)
                            }
                            is Resource.Failure -> {
                                val reasonId = returnErrorStringOrLogoutIfRequired(result.error)

                                view.showError(view.provideResources().getString(reasonId))
                                currentMode = LoyaltyMode.ERROR
                            }
                        }
                    }
        }
    }

    override fun updateBalancePoints() {
        loyaltyStatus?.points?.let { view.setBalancePoints(it) }
    }

    override fun updateBurnedPoints() {
        val loyaltyId = userStore.paymentProvider?.loyalty?.id
        val currency = loyaltyDataModel?.currency
        val tripAmount = loyaltyDataModel?.tripAmount?.toInt()

        if (loyaltyId != null && currency != null && tripAmount != null) {
            loyaltyService.getLoyaltyBurn(loyaltyId, currency, tripAmount)
                    .execute { result ->
                        when (result) {
                            is Resource.Success -> {
                                burnedPoints = result.data.points

                                setSubtitleBasedOnMode(LoyaltyMode.BURN)
                            }
                            is Resource.Failure -> {
                                val reasonId = returnErrorStringOrLogoutIfRequired(result.error)

                                view.showError(view.provideResources().getString(reasonId))
                                currentMode = LoyaltyMode.ERROR
                            }
                        }
                    }
        }
    }

    override fun setSubtitleBasedOnMode(mode: LoyaltyMode, updateAll: Boolean) {
        val resources = view.provideResources()

        when (mode) {
            LoyaltyMode.BURN -> {
                view.setBurnSubtitle(String.format(resources.getString(R.string
                                                                           .kh_uisdk_loyalty_use_points_on_subtitle), Currency.getInstance(loyaltyDataModel?.currency).formatted(loyaltyDataModel?.tripAmount?.toInt() ?: 0 , includeCurrencySymbol = false), loyaltyDataModel?.currency, burnedPoints))
                if(updateAll) {
                    view.setEarnSubtitle(String.format(resources.getString(R.string.kh_uisdk_loyalty_points_earned_for_trip), 0))
                }
            }
            LoyaltyMode.EARN -> {
                if(updateAll) {
                    view.setBurnSubtitle(resources.getString(R.string.kh_uisdk_loyalty_use_points_off_subtitle))
                }
                view.setEarnSubtitle(String.format(resources.getString(R.string.kh_uisdk_loyalty_points_earned_for_trip), earnedPoints))
            }
            else -> {
                String.format(
                    resources.getString(R.string.kh_uisdk_loyalty_points_earned_for_trip),
                    earnedPoints)
            }
        }
    }

    override fun getCurrentMode(): LoyaltyMode {
        return currentMode
    }

    override fun getLoyaltyStatus() {
        val loyaltyId = userStore.paymentProvider?.loyalty?.id
        loyaltyId?.let {
            loyaltyService.getLoyaltyStatus(loyaltyId).execute { result ->
                when (result) {
                    is Resource.Success -> {
                        userStore.loyaltyStatus = result.data
                        set(result.data)

                        updateBalancePoints()
                        updateEarnedPoints()
                        updateBurnedPoints()
                    }
                    is Resource.Failure -> {
                        view.updateLoyaltyFeatures(showEarnRelatedUI = false, showBurnRelatedUI = false)
                    }
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

    override fun setLoyaltyModeCallback(loyaltyModeCallback: LoyaltyContract.LoyaltyModeCallback) {
        this.loyaltyModeCallback = loyaltyModeCallback
    }

    override fun preAuthorize() {
        loyaltyDataModel?.let {
            loyaltyService.getLoyaltyPreAuth(it.loyaltyId, LoyaltyPreAuthPayload(
                    it.currency,
                    if (currentMode == LoyaltyMode.BURN) burnedPoints else 0,
                    flexpay = currentMode != LoyaltyMode.BURN,
                    membership = null)).execute { result ->
                when (result) {
                    is Resource.Success -> {
                        loyaltyModeCallback?.onPreAuthorized(result.data.nonce)
                    }
                    is Resource.Failure -> {
                        val reasonId = returnErrorStringOrLogoutIfRequired(result.error)

                        loyaltyModeCallback?.onPreAuthorizationError(reasonId)
                    }
                }
            }
        }
    }
}
