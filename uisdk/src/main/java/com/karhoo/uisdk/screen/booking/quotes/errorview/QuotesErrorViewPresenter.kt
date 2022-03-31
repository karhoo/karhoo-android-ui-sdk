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

    override fun setSpannableOnSubititle(
        keyword: String,
        link: String?,
        baseText: String,
        linkColor: Int
    ): SpannableString {
        val formattedText = baseText.format(keyword)
        val spannableString = SpannableString(formattedText)
        spannableString.setSpan(
            createClickableSpan(link, linkColor),
            baseText.indexOf(FORMATTER_KEYWORD),
            baseText.indexOf(FORMATTER_KEYWORD) + keyword.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }

    private fun createClickableSpan(url: String?, color: Int): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(widget: View) {
                url?.let {
                    delegate?.onSubtitleClicked()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = color
            }
        }
    }

    companion object {
        private const val FORMATTER_KEYWORD = "%s"
    }
}
