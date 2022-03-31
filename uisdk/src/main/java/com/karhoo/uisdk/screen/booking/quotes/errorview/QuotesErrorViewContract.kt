package com.karhoo.uisdk.screen.booking.quotes.errorview

import android.text.SpannableString

interface QuotesErrorViewContract {
    interface View {
        fun setup(reason: ErrorViewGenericReason, delegateQuotesError: QuotesErrorViewDelegate)
        fun setup(reason: ErrorViewLinkedReason, delegateQuotesError: QuotesErrorViewDelegate)
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
        fun setSpannableOnSubititle(keyword: String, link: String?, baseText: String, linkColor: Int):
                SpannableString
    }
}
