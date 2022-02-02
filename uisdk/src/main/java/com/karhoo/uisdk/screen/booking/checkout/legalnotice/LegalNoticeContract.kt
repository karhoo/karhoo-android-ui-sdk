package com.karhoo.uisdk.screen.booking.checkout.legalnotice

import android.graphics.drawable.Drawable
import android.text.SpannableString

interface LegalNoticeContract {
    interface View {
        fun expandLegalNoticeSection(expand: Boolean, view: android.view.View)
        fun bindView()
        fun showWebView(url: String)
        fun getDrawableResource(id: Int): Drawable?
    }

    interface Presenter {
        fun attachView(v: View)
        fun formatLegalNoticeText(title:String, link: String?, baseText: String, linkColor: Int):
                SpannableString
    }
}
