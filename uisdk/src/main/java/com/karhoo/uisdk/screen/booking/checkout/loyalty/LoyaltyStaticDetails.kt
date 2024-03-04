package com.karhoo.uisdk.screen.booking.checkout.loyalty

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.karhoo.uisdk.R

class LoyaltyStaticDetails @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    var loyaltyMode: LoyaltyMode = LoyaltyMode.EARN

    private lateinit var loyaltyStaticText: TextView

    init {
        inflate(context, R.layout.uisdk_loyalty_static_details, this)
    }

    fun setup(
        context: Context,
        loyaltyMode: LoyaltyMode,
        loyaltyPoints: Int
    ) {
        this.loyaltyMode = loyaltyMode
        loyaltyStaticText = findViewById(R.id.loyaltyStaticText)

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

            else -> {}
        }

        loyaltyStaticText.contentDescription =
            context.getString(R.string.kh_uisdk_acc_loyalty_information) + " " + loyaltyStaticText.text
    }
}
