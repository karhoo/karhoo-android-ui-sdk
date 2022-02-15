package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.karhoo.sdk.api.model.LoyaltyNonce
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_booking_checkout_view.view.*
import kotlinx.android.synthetic.main.uisdk_view_loyalty_view.view.*

class LoyaltyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), LoyaltyContract.View,
    LoyaltyContract.LoyaltyPresenterDelegate {

    private val presenter: LoyaltyContract.Presenter = LoyaltyPresenter()

    override var delegate: LoyaltyContract.LoyaltyViewDelegate? = null
    set(value) {
        field = value
        presenter.loyaltyViewDelegate = delegate
    }

    init {
        inflate(context, R.layout.uisdk_view_loyalty_view, this)

        presenter.loyaltyPresenterDelegate = this
        presenter.updateLoyaltyMode(LoyaltyMode.NONE)

        loyaltyViewBurnLayout.setOnClickListener {
            if (loyaltySwitch.isEnabled) {
                loyaltySwitch.isChecked = !loyaltySwitch.isChecked
                presenter.updateLoyaltyMode(if (loyaltySwitch.isChecked) LoyaltyMode.BURN else LoyaltyMode.EARN)
            }
        }
    }

    override fun toggleFeatures(earnOn: Boolean, burnON: Boolean) {
        if (!earnOn && !burnON) {
            loyaltyView.visibility = GONE
        } else {
            loyaltyView.visibility = VISIBLE
        }

        if (!burnON) {
            loyaltyViewBurnLayout.visibility = GONE
            loyaltyViewSeparatorLayout.visibility = GONE
        } else {
            loyaltyViewBurnLayout.visibility = VISIBLE
            loyaltyViewSeparatorLayout.visibility = VISIBLE
        }

        if (!earnOn) {
            loyaltyViewEarnSubtitle.visibility = GONE
            loyaltyViewSeparatorLayout.visibility = GONE
            loyaltyViewFullWidthSeparator.visibility = VISIBLE
        } else {
            loyaltyViewEarnSubtitle.visibility = VISIBLE
            loyaltyViewFullWidthSeparator.visibility = GONE

            if(burnON) {
                loyaltyViewSeparatorLayout.visibility = VISIBLE
            }
        }
    }

    override fun getCurrentMode(): LoyaltyMode {
        return presenter.getCurrentMode()
    }

    override fun set(mode: LoyaltyMode) {
        loyaltyActionsContainer.background =
            ContextCompat.getDrawable(context, R.drawable.uisdk_loyalty_background)
        loyaltyViewBalance.background =
            ContextCompat.getDrawable(context, R.drawable.uisdk_loyalty_balance_background)

        if (mode == LoyaltyMode.BURN) {
            loyaltyInfoLayout.visibility = VISIBLE
        } else {
            loyaltyInfoLayout.visibility = GONE
        }
    }

    override fun updateWith(
        mode: LoyaltyMode?,
        earnSubtitle: String?,
        burnSubtitle: String?,
        errorMessage: String?
    ) {
        when (mode) {
            LoyaltyMode.BURN -> {
                loyaltyViewBurnSubtitle.text = burnSubtitle
                loyaltyViewBurnSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_secondary_text))
            }
            LoyaltyMode.EARN -> {
                loyaltyViewEarnSubtitle.text = earnSubtitle
                loyaltyViewEarnSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_secondary_text))
            }
            LoyaltyMode.NONE -> {
                loyaltyViewEarnSubtitle.text = earnSubtitle
                loyaltyViewEarnSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_secondary_text))
            }
            LoyaltyMode.ERROR_BAD_CURRENCY,
            LoyaltyMode.ERROR_UNKNOWN -> {
                loyaltyViewEarnSubtitle.text = errorMessage
                loyaltyViewEarnSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_primary_red))
                loyaltyActionsContainer.background = ContextCompat.getDrawable(
                    context, R
                        .drawable.uisdk_loyalty_error_background
                )
                loyaltyViewSeparatorLayout.visibility = GONE
                loyaltyViewBurnLayout.visibility = GONE
                loyaltyInfoLayout.visibility = GONE
                loyaltyViewBalance.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.uisdk_loyalty_balance_error_background
                )
            }
            LoyaltyMode.ERROR_INSUFFICIENT_FUNDS -> {
                loyaltyViewBurnSubtitle.text = errorMessage
                loyaltyViewBurnSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_secondary_text))
                loyaltyViewBurnTitle.setTextColor(resources.getColor(R.color.kh_uisdk_secondary_text))
                loyaltySwitch.isEnabled = false
            }
        }
    }

    override fun set(loyaltyDataModel: LoyaltyViewDataModel) {
        presenter.set(loyaltyDataModel)
        getLoyaltyStatus()
    }

    override fun getLoyaltyStatus() {
        presenter.getLoyaltyStatus()
    }

    override fun provideResources(): Resources {
        return resources
    }

    override fun getLoyaltyPreAuthNonce(callback: (Resource<LoyaltyNonce>) -> Unit) {
        presenter.getLoyaltyPreAuthNonce(callback)
    }

    override fun setBalancePoints(points: Int) {
        loyaltyViewBalance.visibility = VISIBLE
        loyaltyViewBalance.text =
            String.format(resources.getString(R.string.kh_uisdk_loyalty_balance_title), points)
    }
}
