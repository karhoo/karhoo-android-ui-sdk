package com.karhoo.uisdk.screen.booking.quotes.errorview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_quotes_error.view.*

class QuotesErrorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), QuotesErrorViewContract.View,
    QuotesErrorViewContract.QuotesErrorPresenterDelegate {

    private var presenter: QuotesErrorViewPresenter = QuotesErrorViewPresenter()
    private var delegateQuotesError: QuotesErrorViewContract.QuotesErrorViewDelegate? = null

    init {
        View.inflate(context, R.layout.uisdk_view_quotes_error, this)

        presenter.setDelegate(this)

        quotesErrorContainer.setOnClickListener {
            delegateQuotesError?.onClicked()
        }
    }

    override fun setup(
        reason: ErrorViewGenericReason,
        delegateQuotesError: QuotesErrorViewContract.QuotesErrorViewDelegate
    ) {
        quotesErrorIcon.setImageResource(reason.iconId)
        quotesErrorTitle.text = reason.title
        quotesErrorSubtitle.text = reason.subtitle

        this.delegateQuotesError = delegateQuotesError
    }

    override fun setup(
        reason: ErrorViewLinkedReason,
        delegateQuotesError: QuotesErrorViewContract.QuotesErrorViewDelegate
    ) {
        quotesErrorIcon.setImageResource(reason.iconId)
        quotesErrorTitle.text = reason.title
        quotesErrorSubtitle.text = presenter.setSpannableOnSubititle(
            reason.linkTitle,
            reason.subtitleLink,
            reason.title,
            resources.getColor(R.color.kh_uisdk_colorAccent)
        )

        this.delegateQuotesError = delegateQuotesError
    }

    override fun show(show: Boolean) {
        if (show) {
            quotesErrorContainer.visibility = VISIBLE
        } else {
            quotesErrorContainer.visibility = GONE
        }
    }

    override fun onSubtitleClicked() {
        delegateQuotesError?.onSubtitleClicked()
    }
}
