package com.karhoo.uisdk.screen.address.map.picker

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.karhoo.uisdk.R

class AddressPickerView @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet? = null,
                                                  @AttrRes defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr) {

        private lateinit var addressResultTextView: TextView

    init {
        View.inflate(context, R.layout.uisdk_view_address_map_picker, this)
        addressResultTextView = findViewById(R.id.addressResultTextView)
    }

    fun setText(text: String) {
        addressResultTextView.text = text
    }
}
