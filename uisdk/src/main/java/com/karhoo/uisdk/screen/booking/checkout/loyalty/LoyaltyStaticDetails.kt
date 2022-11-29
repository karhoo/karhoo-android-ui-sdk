package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_loyalty_static_details.view.*

class LoyaltyStaticDetails @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    var loyaltyMode: LoyaltyMode = LoyaltyMode.EARN

    init {
        inflate(context, R.layout.uisdk_loyalty_static_details, this)
    }

    fun setup(
        context: Context,
        loyaltyMode: LoyaltyMode,
        loyaltyPoints: Int
    ) {
        this.loyaltyMode = loyaltyMode

        when (loyaltyMode) {
            LoyaltyMode.NONE,
            LoyaltyMode.EARN -> {
                loyaltyStaticText.text = String.format(
                    context.getString(R.string.kh_uisdk_loyalty_info_add_points),
                    loyaltyPoints
                )
            }
            LoyaltyMode.BURN -> {
                loyaltyStaticText.text = String.format(
                    context.getString(R.string.kh_uisdk_loyalty_info_remove_points),
                    loyaltyPoints
                )
            }
        }
    }
}
