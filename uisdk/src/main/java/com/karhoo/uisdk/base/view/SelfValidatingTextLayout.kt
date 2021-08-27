package com.karhoo.uisdk.base.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.karhoo.uisdk.R

class SelfValidatingTextLayout @JvmOverloads constructor(context: Context,
                                                         attrs: AttributeSet? = null,
                                                         defStyleAttr: Int = 0) : HelperTextInputLayout(context, attrs, defStyleAttr) {

    private var validator: Validator? = null
    private var errorMsg: String? = null
    private var isHelper: Boolean = false

    val isValid: Boolean
        get() {
            return validator?.validate(editText?.text.toString()) ?: false
        }

    var errorColorEnabled = false

    fun setValidator(validator: Validator) {
        this.validator = validator
    }

    fun setErrorMsg(errorMsg: Int) {
        this.errorMsg = context.getString(errorMsg)
    }

    fun setHelper(helper: Boolean) {
        isHelper = helper
        helperText = errorMsg
    }

    fun setFocus(hasFocus: Boolean) {
        if (!hasFocus) {
            fieldLostFocus()
        } else {
            error = ""
        }
    }

    fun enableColoredError() {
        if (errorColorEnabled) {
            if (!isValid) {
                setHelperTextColor(ColorStateList.valueOf(Color.RED))
            } else {
                setHelperTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.kh_uisdk_med_grey)))
            }
        }
    }

    private fun fieldLostFocus() {
        when {
            isValid -> enableError(false)
            isHelper -> setHelperEnabled(!isValid)
            else -> enableError(true)
        }
    }

    private fun enableError(showMessage: Boolean) {
        if (isHelper) {
            setHelperEnabled(showMessage)
        } else {
            error = if (showMessage) errorMsg else ""
        }
    }

    private fun setHelperEnabled(isEnabled: Boolean) {
        helperText = if (isEnabled) errorMsg else ""
    }

    interface Validator {

        fun validate(field: String): Boolean

    }
}
