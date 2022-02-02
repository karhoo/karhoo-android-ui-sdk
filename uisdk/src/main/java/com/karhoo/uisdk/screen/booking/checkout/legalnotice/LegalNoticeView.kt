package com.karhoo.uisdk.screen.booking.checkout.legalnotice

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import kotlinx.android.synthetic.main.uisdk_view_legal_notice.view.legalNoticeContainer
import kotlinx.android.synthetic.main.uisdk_view_legal_notice.view.legalNoticeIcon

import android.net.Uri
import android.content.Intent
import android.text.method.LinkMovementMethod
import kotlinx.android.synthetic.main.uisdk_view_legal_notice.view.legalNoticeLabelContainer
import kotlinx.android.synthetic.main.uisdk_view_legal_notice.view.legalNoticeText

class LegalNoticeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
                                               ) : ConstraintLayout(context, attrs, defStyleAttr),
                                                   LegalNoticeContract.View {

    var actions: WebViewActions? = null
    private var presenter: LegalNoticePresenter
    private var isExpanded: Boolean = false

    init {
        inflate(context, R.layout.uisdk_view_legal_notice, this)

        presenter = LegalNoticePresenter()

        presenter.attachView(this)

        legalNoticeLabelContainer.setOnClickListener {
            val arrowIcon = if (isExpanded)
                getDrawableResource(R.drawable.kh_uisdk_ic_arrow_up_small)
            else
                getDrawableResource(R.drawable.kh_uisdk_ic_arrow_down_small)

            legalNoticeIcon.setImageDrawable(arrowIcon)

            expandLegalNoticeSection(isExpanded, legalNoticeText)

            isExpanded = !isExpanded
        }

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
                resources.getColor(R.color.kh_uisdk_colorAccent)
                                                                  )
        } else {
            legalNoticeContainer.visibility = GONE
        }
    }

    override fun expandLegalNoticeSection(expand: Boolean, view: View) {
        val animationType = if (expand) {
            R.anim.kh_uisdk_fade_in
        } else {
            R.anim.kh_uisdk_fade_out
        }

        val animation = AnimationUtils.loadAnimation(context, animationType)

        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                if (expand) {
                    view.visibility = VISIBLE
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {
                //do nothing
            }

            override fun onAnimationEnd(animation: Animation?) {
                view.clearAnimation()

                if (!expand) {
                    view.visibility = GONE
                }
            }
        })

        view.startAnimation(animation)
    }

    override fun getDrawableResource(id: Int): Drawable? {
        return ResourcesCompat.getDrawable(resources, id, null)
    }

    override fun showWebView(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
