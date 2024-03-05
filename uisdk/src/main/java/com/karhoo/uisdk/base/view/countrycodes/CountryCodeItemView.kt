package com.karhoo.uisdk.base.view.countrycodes

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.karhoo.uisdk.R

class CountryCodeItemView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

        private lateinit var countryDiallingCodeText: TextView
    init {
        inflate(context, R.layout.uisdk_view_country_code_item, this)
        countryDiallingCodeText = findViewById(R.id.countryDiallingCodeText)
    }

    fun bind(diallingCode: String) {
        countryDiallingCodeText.text = diallingCode
    }

}
