package com.karhoo.uisdk.screen.booking.checkout.quotes

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.extension.getCancellationText
import java.util.*


class BookingQuotesPresenter(val view: BookingQuotesMVP.View) : BookingQuotesMVP.Presenter {
    override fun checkCancellationSLAMinutes(context: Context, serviceCancellation: ServiceCancellation?, isPrebook: Boolean) {
        val text = serviceCancellation?.getCancellationText(context, isPrebook)

        if (text.isNullOrEmpty()) {
            view.showCancellationText(false)
        } else {
            view.setCancellationText(text)
            view.showCancellationText(true)
        }
    }

    override fun capitalizeCategory(category: String) {
        view.setCategoryText(category.capitalize(Locale.getDefault()))
    }

    override fun createTagsString(tags: List<String>, shortVersion: Boolean): Spannable {
        val tagsText = SpannableStringBuilder("")

        tags.forEachIndexed { index, tag ->
            val span: Spannable = SpannableString("  ${tag.capitalize(Locale.ROOT)} ")
            val icon = getTagIcon(tag)
            val image = icon?.let {
                it.setBounds(0, 0, (it.intrinsicWidth / INTRINSIC_BOUND_FACTOR).toInt(), (it.intrinsicWidth / INTRINSIC_BOUND_FACTOR).toInt())
                ImageSpan(it, ImageSpan.ALIGN_BASELINE)
            }
            image?.let {
                span.setSpan(image, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }

            tagsText.append(span)

            if (shortVersion && index == 1) {
                tagsText.append(" + ").append((tags.size - 1 - index).toString())
                return tagsText
            }
        }

        return tagsText
    }

    private fun getTagIcon(tag: String): Drawable? {
        return when (tag.toLowerCase(Locale.ROOT)) {
            "executive" -> view.getDrawableResource(R.drawable.kh_uisdk_ic_executive)
            "wheelchair" -> view.getDrawableResource(R.drawable.kh_uisdk_ic_wheelchair)
            "electric" -> view.getDrawableResource(R.drawable.kh_uisdk_ic_electric)
            "childseat" -> view.getDrawableResource(R.drawable.kh_uisdk_ic_child_seat)
            "taxi" -> view.getDrawableResource(R.drawable.kh_uisdk_ic_cab)
            "hybrid" -> view.getDrawableResource(R.drawable.kh_uisdk_ic_hybrid)
            else -> view.getDrawableResource(R.drawable.kh_uisdk_ic_other_vehicle)
        }
    }

    companion object {
        private const val INTRINSIC_BOUND_FACTOR = 1.5
    }
}
