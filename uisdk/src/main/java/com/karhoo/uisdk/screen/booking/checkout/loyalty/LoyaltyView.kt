package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.karhoo.sdk.api.model.LoyaltyStatus
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
        loyaltyViewSubtitle.text = presenter.getSubtitleBasedOnMode(resources)
        loyaltyViewSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_secondary_text))
        loyaltyViewLayout.background =
                ContextCompat.getDrawable(context, R.drawable.uisdk_loyalty_background)

        if (mode == LoyaltyMode.BURN) {
            loyaltyInfoLayout.visibility = VISIBLE
        } else {
            loyaltyInfoLayout.visibility = GONE
        }
    }

    override fun showError(message: String) {
        loyaltyViewSubtitle.text = message
        loyaltyViewSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_primary_red))
        loyaltyViewLayout.background = ContextCompat.getDrawable(context, R
                .drawable.uisdk_loyalty_error_background)
        loyaltyInfoLayout.visibility = GONE
    }

    override fun set(loyaltyStatus: LoyaltyStatus) {
        presenter.set(loyaltyStatus)
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

    override fun set(loyaltyRequest: LoyaltyViewRequest) {
        presenter.set(loyaltyRequest)
    }
}
