package com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_extended_capability_item.view.*

class CapabilityItem constructor(context: Context,
                                 attrs: AttributeSet? = null,
                                 defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.uisdk_view_extended_capability_item, this)
    }

    fun bind(iconId: Int, text: String) {
        extendedCapabilityItemIcon.setImageResource(iconId)
        extendedCapabilityItemText.text = text
    }
}
