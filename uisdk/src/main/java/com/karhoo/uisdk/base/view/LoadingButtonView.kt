package com.karhoo.uisdk.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_booking_button.view.bookingButtonLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_button.view.bookingProgressBar
import kotlinx.android.synthetic.main.uisdk_view_booking_button.view.bookingRequestLabel

class LoadingButtonView @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet? = null,
                                                  @AttrRes defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {

    var actions: Actions? = null

    init {
        inflate(context, R.layout.uisdk_view_booking_button, this)
        setOnClickListener(this)
    }

    fun setText(@StringRes stringResId: Int) {
        bookingRequestLabel.setText(stringResId)
    }

    fun setText(string: String) {
        bookingRequestLabel.text = string
    }

    override fun onClick(v: View) {
        if(bookingButtonLayout.isEnabled) {
            showLoading()
            actions?.onLoadingButtonClick()
        }
    }

    fun onLoadingComplete() {
        bookingRequestLabel.visibility = View.VISIBLE
        bookingProgressBar.visibility = View.GONE
    }

    fun showLoading() {
        bookingRequestLabel.visibility = View.INVISIBLE
        bookingProgressBar.visibility = View.VISIBLE
    }

    fun enableButton(enable: Boolean) {
        bookingButtonLayout.isEnabled = enable
    }

    interface Actions {
        fun onLoadingButtonClick()
    }
}
