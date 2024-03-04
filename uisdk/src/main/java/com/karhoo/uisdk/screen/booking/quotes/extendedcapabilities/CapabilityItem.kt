package com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.karhoo.uisdk.R

class CapabilityItem constructor(context: Context,
                                 attrs: AttributeSet? = null,
                                 defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

        private lateinit var extendedCapabilityItemIcon: ImageView
        private lateinit var extendedCapabilityItemText: TextView

    init {
        inflate(context, R.layout.uisdk_view_extended_capability_item, this)

        extendedCapabilityItemIcon = findViewById(R.id.extendedCapabilityItemIcon)
        extendedCapabilityItemText = findViewById(R.id.extendedCapabilityItemText)
    }

    fun bind(iconId: Int, text: String) {
        extendedCapabilityItemIcon.setImageResource(iconId)
        extendedCapabilityItemText.text = text
    }
}
