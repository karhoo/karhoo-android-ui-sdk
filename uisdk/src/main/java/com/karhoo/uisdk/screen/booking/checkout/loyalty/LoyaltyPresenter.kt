package com.karhoo.uisdk.screen.booking.checkout.loyalty

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.LoyaltyNonce
import com.karhoo.sdk.api.model.LoyaltyStatus
import com.karhoo.sdk.api.network.request.LoyaltyPreAuthPayload
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.loyalty.LoyaltyService
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.formatted
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.*
import java.util.Currency

class LoyaltyPresenter(
    val userStore: UserStore = KarhooApi.userStore,
    private val loyaltyService: LoyaltyService = KarhooApi.loyaltyService
) : LoyaltyContract
.Presenter {
    private var currentMode: LoyaltyMode = LoyaltyMode.NONE
        set(value) {
            field = value
            loyaltyViewDelegate?.onModeChanged(value)
        }

    private var loyaltyDataModel: LoyaltyViewDataModel? = null
    private var loyaltyStatus: LoyaltyStatus? = userStore.loyaltyStatus
    private var burnedPoints: Int? = null
    private var earnedPoints: Int? = 0
    override var loyaltyPresenterDelegate: LoyaltyContract.LoyaltyPresenterDelegate? = null
    override var loyaltyViewDelegate: LoyaltyContract.LoyaltyViewDelegate? = null

    override fun updateLoyaltyMode(mode: LoyaltyMode) {
        currentMode = if (mode == LoyaltyMode.BURN && loyaltyStatus?.canBurn == false) {
            return
        } else if (mode == LoyaltyMode.EARN && loyaltyStatus?.canEarn == false) {
            LoyaltyMode.NONE
        } else {
            mode
        }

        setSubtitleBasedOnMode(currentMode, true)

        loyaltyPresenterDelegate?.set(currentMode)
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

                            if (currentMode != LoyaltyMode.ERROR_BAD_CURRENCY && currentMode != LoyaltyMode.ERROR_UNKNOWN) {
                                setSubtitleBasedOnMode(LoyaltyMode.EARN)
                            }
                        }
                        is Resource.Failure -> {
                            getErrorFromResponse(result.error)
                        }
                    }
                }
        }
    }

    override fun updateBalancePoints() {
        loyaltyStatus?.points?.let { loyaltyPresenterDelegate?.showBalance(true, it) }
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

                            val hasInsufficientPoints: Boolean =
                                loyaltyStatus?.points?.compareTo(burnedPoints ?: 0) ?: 0 < 0

                            if (loyaltyStatus?.canBurn == true && hasInsufficientPoints) {
                                loyaltyPresenterDelegate?.updateWith(
                                    mode = LoyaltyMode.ERROR_INSUFFICIENT_FUNDS,
                                    errorMessage = loyaltyPresenterDelegate?.provideResources()
                                        ?.getString(R.string.kh_uisdk_loyalty_insufficient_balance_for_loyalty_burn)
                                )
                                currentMode = LoyaltyMode.ERROR_INSUFFICIENT_FUNDS

                                return@execute
                            }

                            setSubtitleBasedOnMode(LoyaltyMode.BURN)
                        }
                        is Resource.Failure -> {
                            getErrorFromResponse(result.error)
                        }
                    }
                }
        }
    }

    private fun setSubtitleBasedOnMode(mode: LoyaltyMode, updateAll: Boolean = false) {
        val resources = loyaltyPresenterDelegate?.provideResources()

        if (resources != null) {
            when (mode) {
                LoyaltyMode.BURN -> {
                    val burnSubtitle = if (currentMode == LoyaltyMode.BURN) {
                        String.format(
                            resources.getString(R.string.kh_uisdk_loyalty_use_points_on_subtitle),
                            Currency.getInstance(loyaltyDataModel?.currency).formatted(
                                loyaltyDataModel?.tripAmount?.toInt() ?: 0,
                                includeCurrencySymbol = false
                            ),
                            loyaltyDataModel?.currency,
                            burnedPoints
                        )
                    } else {
                        resources.getString(R.string.kh_uisdk_loyalty_use_points_off_subtitle)
                    }
                    loyaltyPresenterDelegate?.updateWith(
                        mode = mode,
                        burnSubtitle = burnSubtitle
                    )
                    if (updateAll) {
                        loyaltyPresenterDelegate?.updateWith(
                            mode = LoyaltyMode.EARN,
                            earnSubtitle = String.format(
                                resources.getString(R.string.kh_uisdk_loyalty_points_earned_for_trip),
                                0
                            )
                        )
                    }
                }
                LoyaltyMode.EARN -> {
                    if (updateAll) {
                        loyaltyPresenterDelegate?.updateWith(
                            mode = LoyaltyMode.BURN,
                            burnSubtitle = resources.getString(R.string.kh_uisdk_loyalty_use_points_off_subtitle)
                        )
                    }
                    loyaltyPresenterDelegate?.updateWith(
                        mode = mode,
                        earnSubtitle = String.format(
                            resources.getString(R.string.kh_uisdk_loyalty_points_earned_for_trip),
                            earnedPoints
                        )
                    )
                }
                else -> {
                    loyaltyPresenterDelegate?.updateWith(
                        mode = mode,
                        earnSubtitle = String.format(
                            resources.getString(R.string.kh_uisdk_loyalty_points_earned_for_trip),
                            earnedPoints
                        )
                    )
                }
            }
        }
    }

    override fun getCurrentMode(): LoyaltyMode {
        return currentMode
    }

    override fun getLoyaltyStatus(callback: ((Resource<LoyaltyStatus>) -> Unit?)?) {
        val loyaltyId = userStore.paymentProvider?.loyalty?.id
        loyaltyId?.let {
            loyaltyService.getLoyaltyStatus(loyaltyId).execute { result ->
                when (result) {
                    is Resource.Success -> {
//                        val loyaltyStatus = LoyaltyStatus(1500, canBurn = true, canEarn = true)

                        userStore.loyaltyStatus = result.data
                        set(result.data)

                        updateBalancePoints()
                        updateEarnedPoints()
                        updateBurnedPoints()

                        callback?.invoke(result)
                    }
                    is Resource.Failure -> {
                        getErrorFromResponse(result.error)

                        callback?.invoke(result)
                    }
                }
            }
        }
    }

    private fun set(loyaltyStatus: LoyaltyStatus) {
        this.loyaltyStatus = loyaltyStatus

        val canEarn = loyaltyStatus.canEarn ?: false
        val canBurn = loyaltyStatus.canBurn ?: false

        loyaltyPresenterDelegate?.toggleFeatures(canEarn, canBurn)
    }

    override fun getLoyaltyPreAuthNonce(callback: (Resource<LoyaltyNonce>, LoyaltyStatus?) -> Unit) {
        if (currentMode == LoyaltyMode.ERROR_BAD_CURRENCY || currentMode == LoyaltyMode.ERROR_UNKNOWN) {
            // Loyalty related web-services return slug based errors, not error code based ones
            // this error does not coincide with any error returned by the backend
            // Although the message is not shown in the UISDK implementation it will serve DPs when integrating as a standalone component
            callback.invoke(
                Resource.Failure(
                    KarhooError.fromCustomError(
                        erCode = KarhooError.ErrMissingBrowserInfo.code,
                        erInternalMessage = loyaltyPresenterDelegate?.provideResources()
                            ?.getString(R.string.kh_uisdk_loyalty_not_eligible_for_pre_auth) ?: "",
                        erUserFriendlyMessage = loyaltyPresenterDelegate?.provideResources()
                            ?.getString(R.string.kh_uisdk_loyalty_not_eligible_for_pre_auth) ?: ""
                    )
                ),
                loyaltyStatus
            )
            return
        }
        loyaltyDataModel?.let {
            loyaltyService.getLoyaltyPreAuth(
                it.loyaltyId,
                LoyaltyPreAuthPayload(
                    it.currency,
                    if (currentMode == LoyaltyMode.BURN) burnedPoints else 0,
                    flexpay = currentMode != LoyaltyMode.BURN,
                    membership = null
                )
            ).execute { result ->
                when (result) {
                    is Resource.Success -> {
                        callback.invoke(result, loyaltyStatus)
                    }
                    is Resource.Failure -> {
                        callback.invoke(result, loyaltyStatus)
                    }
                }
            }
        }
    }

    private fun getErrorFromResponse(error: KarhooError) {
        val reasonId = returnErrorStringOrLogoutIfRequired(error)

        currentMode = getErrorMode(error)
        loyaltyPresenterDelegate?.updateWith(
            currentMode,
            errorMessage = loyaltyPresenterDelegate?.provideResources()?.getString(reasonId)
        )

        loyaltyPresenterDelegate?.showBalance(false)
    }

    private fun getErrorMode(error: KarhooError): LoyaltyMode {
        return when (error) {
            KarhooError.LoyaltyNotAllowedToBurnPoints -> LoyaltyMode.ERROR_UNKNOWN
            KarhooError.LoyaltyIncomingPointsExceedBalance -> LoyaltyMode.ERROR_INSUFFICIENT_FUNDS
            KarhooError.LoyaltyEmptyCurrency -> LoyaltyMode.ERROR_BAD_CURRENCY
            KarhooError.LoyaltyUnknownCurrency -> LoyaltyMode.ERROR_BAD_CURRENCY
            KarhooError.LoyaltyInternalError -> LoyaltyMode.ERROR_UNKNOWN
            else -> LoyaltyMode.ERROR_UNKNOWN
        }
    }

    override fun getPoints(): Int? {
        return earnedPoints ?: burnedPoints
    }
}
