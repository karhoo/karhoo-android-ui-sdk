package com.karhoo.uisdk.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.Nullable
import com.karhoo.sdk.api.KarhooError

import com.karhoo.uisdk.R

class ErrorStateView @JvmOverloads constructor(context: Context,
                                               @Nullable attrs: AttributeSet? = null,
                                               @AttrRes defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private val errorMessage: TextView
    private val errorCode: TextView
    private val retryButton: LoadingButtonView

    init {
        View.inflate(context, R.layout.uisdk_view_error_state, this)
        errorMessage = findViewById(R.id.errorText)
        errorCode = findViewById(R.id.errorCode)
        retryButton = findViewById(R.id.retryButton)
        retryButton.setText(R.string.kh_uisdk_retry)
    }

    fun setRetryButtonClickListener(listener: OnClickListener?) {
        retryButton.actions = object : LoadingButtonView.Actions {
            override fun onLoadingButtonClick() {
                if (listener != null) {
                    listener.onClick(this@ErrorStateView)
                }
            }
        }
    }

    fun setErrorMessage(message: String, karhooError: KarhooError?) {
        retryButton.onLoadingComplete()
        errorMessage.text = message
        karhooError?.let {
            errorCode.text = it.code
        }
    }

}
