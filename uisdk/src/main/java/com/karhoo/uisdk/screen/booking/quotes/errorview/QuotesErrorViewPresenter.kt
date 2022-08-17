package com.karhoo.uisdk.screen.booking.quotes.errorview

import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

class QuotesErrorViewPresenter : QuotesErrorViewContract.Presenter {
    private var delegate: QuotesErrorViewContract.QuotesErrorPresenterDelegate? = null

    override fun setDelegate(delegate: QuotesErrorViewContract.QuotesErrorPresenterDelegate) {
        this.delegate = delegate
    }

    override fun createSpannable(
        keyword: String,
        baseText: String,
        linkColor: Int
    ): SpannableString {
        val spannableString = SpannableString(baseText)
        spannableString.setSpan(
            createClickableSpan(linkColor),
            baseText.indexOf(keyword),
            baseText.indexOf(keyword) + keyword.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }

    private fun createClickableSpan(color: Int): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(widget: View) {
                delegate?.onSubtitleClicked()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = color
            }
        }
    }
}
