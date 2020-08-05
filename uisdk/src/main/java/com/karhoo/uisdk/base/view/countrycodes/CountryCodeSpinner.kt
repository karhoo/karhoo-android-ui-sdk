package com.karhoo.uisdk.base.view.countrycodes

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSpinner

import com.karhoo.uisdk.R

class CountryCodeSpinner @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : AppCompatSpinner(context, attrs, defStyleAttr) {

    companion object {
        val COUNTRY_CODES = R.array.country_codes
    }

    init {
        val countryCodes = resources.getStringArray(COUNTRY_CODES)
        val adapter = CountryCodeAdapter(countryCodes)
        setAdapter(adapter)
    }

    fun setCountryCode(code: String?) {
        setSelection(resources.getStringArray(COUNTRY_CODES).indexOf(code))
    }

}
