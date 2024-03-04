package com.karhoo.uisdk.screen.booking.checkout.component.views

import android.content.Context
import android.content.res.ColorStateList
import android.text.SpannableString
import android.util.AttributeSet
import android.widget.LinearLayout
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.util.extension.hideSoftKeyboard

class BookingTermsView @JvmOverloads constructor(context: Context,
                                                 attrs: AttributeSet? = null,
                                                 defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    var actions: WebViewActions? = null

    private lateinit var khTermsAndConditionsText: TextView
    lateinit var khTermsAndConditionsCheckBox: MaterialCheckBox
    private lateinit var khTermsAndConditionsTextWithCheckBox: TextView
    private lateinit var khTermsAndConditionsCheckBoxLayout: LinearLayout

    init {
        inflate(context, R.layout.uisdk_view_booking_terms, this)

        khTermsAndConditionsText = findViewById(R.id.khTermsAndConditionsText)
        khTermsAndConditionsCheckBox = findViewById(R.id.khTermsAndConditionsCheckBox)
        khTermsAndConditionsTextWithCheckBox = findViewById(R.id.khTermsAndConditionsTextWithCheckBox)
        khTermsAndConditionsCheckBoxLayout = findViewById(R.id.khTermsAndConditionsCheckBoxLayout)
    }

    private var initialCheckBoxColorStateList: ColorStateList? = null
    var checkBoxChangedCallback: (() -> Unit?)? = null

    fun bindViews(vehicle: Quote) {
        val bookingTermsAndConditionsText = resources.getString(R.string.kh_uisdk_booking_terms_and_conditions)
        val bookingPrivacyPolicyText = resources.getString(R.string.kh_uisdk_karhoo_privacy_policy)
        val fleetTermsAndConditionsText = resources.getString(R.string.kh_uisdk_label_fleet_terms_and_conditions)
        val fleetCancellationText = resources.getString(R.string.kh_uisdk_label_fleet_cancellation_policy)

        val simpleText = String.format(
                resources.getString(R.string.kh_uisdk_booking_terms),
                bookingTermsAndConditionsText,
                bookingPrivacyPolicyText,
                vehicle.fleet.name,
                fleetTermsAndConditionsText,
                fleetCancellationText)

        val spannableString = SpannableString(simpleText)
        spannableString.setSpan(createClickableSpan(resources.getString(R.string.kh_uisdk_karhoo_general_terms_url)),
                                simpleText.indexOf(bookingTermsAndConditionsText),
                                simpleText.indexOf(bookingTermsAndConditionsText) + bookingTermsAndConditionsText.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(createClickableSpan(resources.getString(R.string.kh_uisdk_karhoo_privacy_policy_url)),
                                simpleText.indexOf(bookingPrivacyPolicyText),
                                simpleText.indexOf(bookingPrivacyPolicyText) + bookingPrivacyPolicyText.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(createClickableSpan(vehicle.fleet.termsConditionsUrl),
                                simpleText.lastIndexOf(fleetTermsAndConditionsText),
                                simpleText.lastIndexOf(fleetTermsAndConditionsText) + fleetTermsAndConditionsText.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(createClickableSpan(vehicle.fleet.termsConditionsUrl),
                                simpleText.indexOf(fleetCancellationText),
                                simpleText.indexOf(fleetCancellationText) + fleetCancellationText.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        khTermsAndConditionsText.text = spannableString
        khTermsAndConditionsText.movementMethod = LinkMovementMethod.getInstance()
        initialCheckBoxColorStateList = khTermsAndConditionsCheckBox.buttonTintList

        if(KarhooUISDKConfigurationProvider.configuration.isExplicitTermsAndConditionsConsentRequired()){
            khTermsAndConditionsText.visibility = View.GONE

            khTermsAndConditionsCheckBoxLayout.visibility = View.VISIBLE
            khTermsAndConditionsTextWithCheckBox.text = spannableString
            khTermsAndConditionsTextWithCheckBox.movementMethod = LinkMovementMethod.getInstance()
            khTermsAndConditionsCheckBox.setOnCheckedChangeListener { _, _ ->
                khTermsAndConditionsCheckBox.buttonTintList =  initialCheckBoxColorStateList
                checkBoxChangedCallback?.invoke()
            }
        }
    }

    private fun createClickableSpan(url: String?): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(widget: View) {
                widget.hideSoftKeyboard()
                actions?.showWebView(url)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = resources.getColor(R.color.kh_uisdk_accent)
            }
        }
    }
}
