package com.karhoo.uisdk.base.view.countrycodes

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_country_code_item.view.countryDiallingCodeText

class CountryCodeItemView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.uisdk_view_country_code_item, this)
    }

    fun bind(diallingCode: String) {
        countryDiallingCodeText.text = diallingCode
    }

}
