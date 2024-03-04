package com.karhoo.uisdk.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import com.karhoo.uisdk.R

class LoadingButtonView @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet? = null,
                                                  @AttrRes defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {

    var actions: Actions? = null

    private lateinit var bookingButtonLayout: FrameLayout
    private lateinit var bookingRequestLabel: TextView
    private lateinit var bookingProgressBar: ProgressBar

    init {
        inflate(context, R.layout.uisdk_view_booking_button, this)

        bookingButtonLayout = findViewById(R.id.bookingButtonLayout)
        bookingRequestLabel = findViewById(R.id.bookingRequestLabel)
        bookingProgressBar = findViewById(R.id.bookingProgressBar)
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

    fun isButtonEnabled(): Boolean {
        return bookingButtonLayout.isEnabled
    }

    interface Actions {
        fun onLoadingButtonClick()
    }
}
