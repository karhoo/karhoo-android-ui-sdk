package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.karhoo.sdk.api.model.LoyaltyNonce
import com.karhoo.sdk.api.model.LoyaltyStatus
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
                loyaltySwitch.toggle()
                presenter.updateLoyaltyMode(if (loyaltySwitch.isChecked) LoyaltyMode.BURN else LoyaltyMode.EARN)
            }
        }

        // This prevents TalkBack from reading the text as an acronym
        loyaltyViewSeparatorTextView.contentDescription = context.getString(R.string.kh_uisdk_loyalty_separator).lowercase()

        setSwitchContentDescription()
        setBurnLayoutContentDescription()
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
                loyaltyViewBurnSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_label))
            }
            LoyaltyMode.EARN -> {
                loyaltyViewEarnSubtitle.text = earnSubtitle
                loyaltyViewEarnSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_label))
                // Adding the content description here so that the number of points is properly calculated and TalkBack is accurate
                loyaltyViewEarnLayout.contentDescription = context.resources.getString(R.string.kh_uisdk_loyalty_title) + " " + earnSubtitle
            }
            LoyaltyMode.NONE -> {
                loyaltyViewEarnSubtitle.text = earnSubtitle
                loyaltyViewEarnSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_label))
            }
            LoyaltyMode.ERROR_BAD_CURRENCY,
            LoyaltyMode.ERROR_UNKNOWN -> {
                loyaltyViewEarnSubtitle.text = errorMessage
                loyaltyViewEarnSubtitle.visibility = VISIBLE
                loyaltyViewEarnSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_error))
                loyaltyActionsContainer.background = ContextCompat.getDrawable(
                    context, R
                        .drawable.uisdk_loyalty_error_background
                )
                loyaltyViewSeparatorLayout.visibility = GONE
                loyaltyViewFullWidthSeparator.visibility = GONE
                loyaltyViewBurnLayout.visibility = GONE
                loyaltyInfoLayout.visibility = GONE
                loyaltyViewBalance.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.uisdk_loyalty_balance_error_background
                )
            }
            LoyaltyMode.ERROR_INSUFFICIENT_FUNDS -> {
                loyaltyViewBurnSubtitle.text = errorMessage
                loyaltyViewBurnSubtitle.setTextColor(resources.getColor(R.color.kh_uisdk_label))
                loyaltyViewBurnTitle.setTextColor(resources.getColor(R.color.kh_uisdk_label))
                loyaltySwitch.isEnabled = false
            }
        }
    }

    override fun set(
        loyaltyDataModel: LoyaltyViewDataModel,
        callback: ((Resource<LoyaltyStatus>) -> Unit?)?
    ) {
        presenter.set(loyaltyDataModel)
        presenter.getLoyaltyStatus(callback)
    }

    override fun provideResources(): Resources {
        return resources
    }

    override fun getLoyaltyPreAuthNonce(callback: (Resource<LoyaltyNonce>, LoyaltyStatus?) -> Unit) {
        presenter.getLoyaltyPreAuthNonce(callback)
    }

    override fun showBalance(show: Boolean, points: Int) {
        if(show) {
            loyaltyViewBalance.visibility = VISIBLE
            loyaltyViewBalance.text =
                String.format(resources.getString(R.string.kh_uisdk_loyalty_balance_title), points)
        } else {
            loyaltyViewBalance.visibility = INVISIBLE
        }
    }

    override fun getPoints(): Int? {
        return presenter.getPoints()
    }

    private fun setSwitchContentDescription() {
        loyaltySwitch.accessibilityDelegate = object : AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfo
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val switchOnText = context.resources.getString(R.string.kh_uisdk_accessibility_loyalty_switch_enabled)
                val switchOffText = context.resources.getString(R.string.kh_uisdk_accessibility_loyalty_switch_disabled)
                info.text = if (loyaltySwitch.isChecked) switchOnText else switchOffText
            }
        }
    }

    private fun setBurnLayoutContentDescription() {
        loyaltyViewBurnTextsLayout.accessibilityDelegate = object : AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfo
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                // When burn mode is ON have TalkBack say the actual number of points being burned
                val burnOnText = context.resources.getString(R.string.kh_uisdk_loyalty_use_points_title) + " " + loyaltyViewBurnSubtitle.text
                val burnOffText = context.resources.getString(R.string.kh_uisdk_loyalty_use_points_title) + " " + context.resources.getString(R.string.kh_uisdk_loyalty_use_points_off_subtitle)
                info.text = if (loyaltySwitch.isChecked) burnOnText else burnOffText
            }
        }
    }
}
