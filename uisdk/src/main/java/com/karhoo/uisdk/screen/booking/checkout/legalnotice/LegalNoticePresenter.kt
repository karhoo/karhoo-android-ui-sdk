package com.karhoo.uisdk.screen.booking.checkout.legalnotice

import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

class LegalNoticePresenter : LegalNoticeContract.Presenter {
    private var view: LegalNoticeContract.View? = null
    override fun attachView(v: LegalNoticeContract.View) {
        this.view = v
    }

    override fun formatLegalNoticeText(
        title: String,
        link: String?,
        baseText: String,
        linkColor: Int): SpannableString {

        val formattedText = baseText.format(title)
        val spannableString = SpannableString(formattedText)
        spannableString.setSpan(
            createClickableSpan(link, linkColor),
            baseText.indexOf(FORMATTER_KEYWORD),
            baseText.indexOf(FORMATTER_KEYWORD) + title.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannableString
    }

    private fun createClickableSpan(url: String?, color: Int): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(widget: View) {
                url?.let {
                    view?.showWebView(it)
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = color
            }
        }
    }

    private companion object {
        private const val FORMATTER_KEYWORD = "%s"
    }
}
