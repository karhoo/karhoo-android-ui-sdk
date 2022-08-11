package com.karhoo.uisdk.screen.booking.quotes.errorview

import android.text.SpannableString

interface QuotesErrorViewContract {
    interface View {
        fun setup(reason: ErrorViewGenericReason, delegateQuotesError: QuotesErrorViewDelegate)
        fun setupWithSpan(reason: ErrorViewGenericReason, delegateQuotesError: QuotesErrorViewDelegate)
        fun show(show: Boolean)
    }

    interface QuotesErrorViewDelegate {
        fun onSubtitleClicked()
        fun onClicked()
    }

    interface QuotesErrorPresenterDelegate {
        fun onSubtitleClicked()
    }

    interface Presenter {
        fun setDelegate(delegate: QuotesErrorPresenterDelegate)
        fun createSpannable(keyword: String, baseText: String, linkColor: Int):
                SpannableString
    }
}
