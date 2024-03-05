package com.karhoo.uisdk.screen.booking.quotes.errorview

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.karhoo.uisdk.R

class QuotesErrorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), QuotesErrorViewContract.View,
    QuotesErrorViewContract.QuotesErrorPresenterDelegate {

    private var presenter: QuotesErrorViewPresenter = QuotesErrorViewPresenter()
    private var delegateQuotesError: QuotesErrorViewContract.QuotesErrorViewDelegate? = null

    private lateinit var quotesErrorContainer: LinearLayout
    private lateinit var quotesErrorIcon: ImageView
    private lateinit var quotesErrorTitle: TextView
    private lateinit var quotesErrorSubtitle: TextView

    init {
        View.inflate(context, R.layout.uisdk_view_quotes_error, this)

        quotesErrorContainer = findViewById(R.id.quotesErrorContainer)
        quotesErrorIcon = findViewById(R.id.quotesErrorIcon)
        quotesErrorTitle = findViewById(R.id.quotesErrorTitle)
        quotesErrorSubtitle = findViewById(R.id.quotesErrorSubtitle)

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

    override fun setupWithSpan(
        reason: ErrorViewGenericReason,
        delegateQuotesError: QuotesErrorViewContract.QuotesErrorViewDelegate
    ) {
        val contactUsLabel = resources.getString(R.string.kh_uisdk_contact_us)

        quotesErrorIcon.setImageResource(reason.iconId)
        quotesErrorTitle.text = reason.title
        quotesErrorSubtitle.text = presenter.createSpannable(
            contactUsLabel[0].uppercaseChar() + contactUsLabel.substring(1).toLowerCase(),
            reason.subtitle,
            resources.getColor(R.color.kh_uisdk_accent)
        )

        quotesErrorSubtitle.movementMethod = LinkMovementMethod.getInstance()

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
