package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
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
    : ConstraintLayout(context, attrs, defStyleAttr), LoyaltyMVP.View {

    private val presenter: LoyaltyMVP.Presenter = LoyaltyPresenter()

    init {
        inflate(context, R.layout.uisdk_view_loyalty_view, this)

        presenter.attachView(this)

        var i = 0;

        //TODO to remove cases after finishing the rest of the loyalty tickets
        loyaltyViewLayout.setOnClickListener {
            i++

            loyaltySwitch.isChecked = !loyaltySwitch.isChecked
            presenter.updateLoyaltyMode(if (loyaltySwitch.isChecked) LoyaltyMode.BURN else LoyaltyMode.EARN)

            when (i) {
                1 -> {
                    //nothing
                }
                2 -> {
                    showError(resources.getString(R.string.kh_uisdk_loyalty_insufficient_balance_for_loyalty_burn))
                }
                3 -> {
                    presenter.updateLoyaltyMode(LoyaltyMode.NONE)
                    showError(resources.getString(R.string.kh_uisdk_loyalty_unsupported_currency))
                }
                4 -> {
                    Toast.makeText(context, "Can Burn == false", Toast.LENGTH_LONG).show()
                    presenter.set(LoyaltyViewModel("", "GBP", 0.0, canEarn = false, canBurn =
                    false))
                }
                else -> {
                    i = 0
                    presenter.updateLoyaltyMode(LoyaltyMode.EARN)
                    loyaltySwitch.isChecked = false
                    Toast.makeText(context, "Can Burn == true", Toast.LENGTH_LONG).show()
                    presenter.set(LoyaltyViewModel("", "GBP", 0.0, canEarn = true, canBurn = true))
                }
            }
        }

        presenter.set(LoyaltyViewModel("", "GBP", 0.0, canEarn = true, canBurn = true))
        presenter.updateLoyaltyMode(LoyaltyMode.EARN)
    }

    override fun getCurrentMode(): LoyaltyMode {
        return presenter.getCurrentMode()
    }

    override fun set(mode: LoyaltyMode) {
        loyaltyViewSubtitle.text = presenter.getSubtitleBasedOnMode(resources)
        loyaltyViewSubtitle.setTextColor(resources.getColor(R.color.lightGrey))
        loyaltyViewLayout.background =
                ContextCompat.getDrawable(context, R.drawable.uisdk_loyalty_background)

        if (mode == LoyaltyMode.BURN) {
            showInfoView(true);
        } else {
            showInfoView(false)
        }
    }

    private fun showInfoView(show: Boolean) {
        if (show) {
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

    override fun set(viewModel: LoyaltyViewModel) {
        presenter.set(viewModel)
    }

    override fun updateLoyaltyFeatures(showEarnRelatedUI: Boolean, showBurnRelatedUI: Boolean) {
        if (!showEarnRelatedUI) {
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
}
