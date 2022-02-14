package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_loyalty_view.view.loyaltyActionsContainer
import kotlinx.android.synthetic.main.uisdk_view_loyalty_view.view.loyaltyInfoLayout
import kotlinx.android.synthetic.main.uisdk_view_loyalty_view.view.loyaltySwitch
import kotlinx.android.synthetic.main.uisdk_view_loyalty_view.view.loyaltyViewBurnLayout
import kotlinx.android.synthetic.main.uisdk_view_loyalty_view.view.loyaltyViewBurnSubtitle
import kotlinx.android.synthetic.main.uisdk_view_loyalty_view.view.loyaltyViewBalance
import kotlinx.android.synthetic.main.uisdk_view_loyalty_view.view.loyaltyViewEarnSubtitle
import kotlinx.android.synthetic.main.uisdk_view_loyalty_view.view.loyaltyViewSeparatorLayout

class LoyaltyView @JvmOverloads constructor(context: Context,
                                            attrs: AttributeSet? = null,
                                            defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr), LoyaltyContract.View {

    private val presenter: LoyaltyContract.Presenter = LoyaltyPresenter()

    init {
        inflate(context, R.layout.uisdk_view_loyalty_view, this)

        presenter.attachView(this)

        loyaltyViewBurnLayout.setOnClickListener {
            loyaltySwitch.isChecked = !loyaltySwitch.isChecked
            presenter.updateLoyaltyMode(if (loyaltySwitch.isChecked) LoyaltyMode.BURN else LoyaltyMode.EARN)
        }
    }

    override fun getCurrentMode(): LoyaltyMode {
        return presenter.getCurrentMode()
    }

    override fun set(mode: LoyaltyMode) {
        loyaltyActionsContainer.background =
                ContextCompat.getDrawable(context, R.drawable.uisdk_loyalty_background)
        loyaltyViewBalance.background = ContextCompat.getDrawable(context, R.drawable.uisdk_loyalty_balance_background)

        if (mode == LoyaltyMode.BURN) {
            loyaltyInfoLayout.visibility = VISIBLE
        } else {
            loyaltyInfoLayout.visibility = GONE
        }

        presenter.setSubtitleBasedOnMode(mode)
    }

    override fun showError(message: String) {
        loyaltyViewBurnSubtitle.text = message
        loyaltyViewBurnSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_primary_red))
        loyaltyActionsContainer.background = ContextCompat.getDrawable(context, R
                .drawable.uisdk_loyalty_error_background)
        loyaltyInfoLayout.visibility = GONE
        loyaltyViewBalance.background = ContextCompat.getDrawable(context, R.drawable.uisdk_loyalty_balance_error_background)
    }

    override fun updateLoyaltyFeatures(showEarnRelatedUI: Boolean, showBurnRelatedUI: Boolean) {
        if (!showEarnRelatedUI) {
            loyaltyViewEarnSubtitle.visibility = GONE
        } else {
            loyaltyViewEarnSubtitle.visibility = VISIBLE
        }

        if (!showBurnRelatedUI) {
            loyaltyViewBurnLayout.visibility = GONE
            loyaltyViewSeparatorLayout.visibility = GONE
        } else {
            loyaltyViewBurnLayout.visibility = VISIBLE
            loyaltyViewSeparatorLayout.visibility = VISIBLE
        }

        if (!showEarnRelatedUI && !showBurnRelatedUI) {
            loyaltyViewBurnLayout.visibility = GONE
        } else {
            loyaltyViewBurnLayout.visibility = VISIBLE
        }
    }

    override fun set(loyaltyDataModel: LoyaltyViewDataModel) {
        presenter.set(loyaltyDataModel)
    }

    override fun getLoyaltyStatus() {
        presenter.getLoyaltyStatus()
    }

    override fun setEarnSubtitle(subtitle: String) {
        loyaltyViewEarnSubtitle.text = subtitle
        loyaltyViewEarnSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_secondary_text))
    }

    override fun setBurnSubtitle(subtitle: String) {
        loyaltyViewBurnSubtitle.text = subtitle
        loyaltyViewBurnSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_secondary_text))
    }

    override fun provideResources(): Resources {
        return resources
    }

    override fun setLoyaltyModeCallback(loyaltyModeCallback: LoyaltyContract.LoyaltyModeCallback) {
        presenter.setLoyaltyModeCallback(loyaltyModeCallback)
    }

    override fun preAuthorize() {
        presenter.preAuthorize()
    }

    override fun setBalancePoints(points: Int) {
        loyaltyViewBalance.visibility = VISIBLE
        loyaltyViewBalance.text = String.format(resources.getString(R.string.kh_uisdk_loyalty_balance_title), points)
    }
}
