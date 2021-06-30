package com.karhoo.uisdk.screen.booking.address

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.karhoo.uisdk.R

class TimePickerTitleView @JvmOverloads constructor(context: Context,
                                                    attrs: AttributeSet? = null) : AppCompatTextView(context, attrs) {

    fun setTitle(@StringRes title: Int, timezone: String): View {
        val pickerTitle = "${resources.getString(title)} ($timezone)"
        text = pickerTitle
        setBackgroundColor(ContextCompat.getColor(context, R.color.khTimePickerColor))
        setTextColor(ContextCompat.getColor(context, R.color.opacity_off_white_time_picker))
        setPadding(resources.getDimension(R.dimen.spacing_small).toInt(),
                   resources.getDimension(R.dimen.spacing_xsmall).toInt(),
                   resources.getDimension(R.dimen.spacing_small).toInt(), 0)
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0F)
        return this
    }

}
