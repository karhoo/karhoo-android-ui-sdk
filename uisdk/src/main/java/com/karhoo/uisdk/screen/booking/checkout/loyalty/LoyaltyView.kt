package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_loyalty_view.view.loyaltyInfoLayout
import kotlinx.android.synthetic.main.uisdk_view_loyalty_view.view.loyaltySwitch
import kotlinx.android.synthetic.main.uisdk_view_loyalty_view.view.loyaltyViewLayout
import kotlinx.android.synthetic.main.uisdk_view_loyalty_view.view.loyaltyViewSubtitle

class LoyaltyView @JvmOverloads constructor(context: Context,
                                            attrs: AttributeSet? = null,
                                            defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr), LoyaltyContract.View {

    private val presenter: LoyaltyContract.Presenter = LoyaltyPresenter()

    init {
        inflate(context, R.layout.uisdk_view_loyalty_view, this)

        presenter.attachView(this)

        loyaltyViewLayout.setOnClickListener {
            loyaltySwitch.isChecked = !loyaltySwitch.isChecked
            presenter.updateLoyaltyMode(if (loyaltySwitch.isChecked) LoyaltyMode.BURN else LoyaltyMode.EARN)
        }
    }

    override fun getCurrentMode(): LoyaltyMode {
        return presenter.getCurrentMode()
    }

    override fun set(mode: LoyaltyMode) {
        loyaltyViewLayout.background =
                ContextCompat.getDrawable(context, R.drawable.uisdk_loyalty_background)

        if (mode == LoyaltyMode.BURN) {
            loyaltyInfoLayout.visibility = VISIBLE
        } else {
            loyaltyInfoLayout.visibility = GONE
        }

        presenter.getSubtitleBasedOnMode()
    }

    override fun showError(message: String) {
        loyaltyViewSubtitle.text = message
        loyaltyViewSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_primary_red))
        loyaltyViewLayout.background = ContextCompat.getDrawable(context, R
                .drawable.uisdk_loyalty_error_background)
        loyaltyInfoLayout.visibility = GONE
    }

    override fun updateLoyaltyFeatures(showEarnRelatedUI: Boolean, showBurnRelatedUI: Boolean) {
        if (!showEarnRelatedUI && presenter.getCurrentMode() != LoyaltyMode.BURN) {
            loyaltyViewSubtitle.visibility = GONE
        } else {
            loyaltyViewSubtitle.visibility = VISIBLE
        }

        if (!showBurnRelatedUI) {
            loyaltySwitch.visibility = GONE
        } else {
            loyaltySwitch.visibility = VISIBLE
        }

        if (!showEarnRelatedUI && !showBurnRelatedUI) {
            loyaltyViewLayout.visibility = GONE
        } else {
            loyaltyViewLayout.visibility = VISIBLE
        }
    }

    override fun set(loyaltyDataModel: LoyaltyViewDataModel) {
        presenter.set(loyaltyDataModel)
    }

    override fun getLoyaltyStatus() {
        presenter.getLoyaltyStatus()
    }

    override fun setSubtitle(subtitle: String) {
        loyaltyViewSubtitle.text = subtitle
        loyaltyViewSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_secondary_text))
    }

    override fun provideResources(): Resources {
        return resources
    }

    override fun setLoyaltyModeCallback(loyaltyModeCallback: LoyaltyContract.LoyaltyModeCallback) {
        presenter.setLoyaltyModeCallback(loyaltyModeCallback)
    }
}
