package com.karhoo.uisdk.screen.booking.checkout.legalnotice

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import kotlinx.android.synthetic.main.uisdk_view_legal_notice.view.legalNoticeContainer

import android.net.Uri
import android.content.Intent
import android.text.method.LinkMovementMethod
import com.karhoo.uisdk.screen.booking.domain.support.KarhooFeedbackEmailComposer
import kotlinx.android.synthetic.main.uisdk_view_legal_notice.view.legalNoticeText

class LegalNoticeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
                                               ) : ConstraintLayout(context, attrs, defStyleAttr),
                                                   LegalNoticeContract.View {

    var actions: WebViewActions? = null
    private var presenter: LegalNoticePresenter
    private lateinit var emailComposer: KarhooFeedbackEmailComposer

    init {
        inflate(context, R.layout.uisdk_view_legal_notice, this)

        presenter = LegalNoticePresenter()

        presenter.attachView(this)

        emailComposer = KarhooFeedbackEmailComposer(context)

        bindView()
    }

    override fun bindView() {
        val noticeText = resources.getString(R.string.kh_uisdk_legal_notice_text)
        if (noticeText.isNotEmpty()) {
            legalNoticeText.movementMethod = LinkMovementMethod.getInstance();
            legalNoticeText.text = presenter.formatLegalNoticeText(
                resources.getString(R.string.kh_uisdk_legal_notice_title),
                resources.getString(R.string.kh_uisdk_legal_notice_link),
                noticeText,
                resources.getColor(R.color.kh_uisdk_accent)
            )
        } else {
            legalNoticeContainer.visibility = GONE
        }
    }

    override fun getDrawableResource(id: Int): Drawable? {
        return ResourcesCompat.getDrawable(resources, id, null)
    }

    override fun showWebView(url: String) {
        val intent: Intent? = if (url.contains(MAIL_KEYWORD)) {
            emailComposer.createLegalNoticeEmail(url)
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        }

        intent?.let {
            context.startActivity(it)
        }
    }

    companion object {
        private const val MAIL_KEYWORD = "mailto"
    }
}
