package com.karhoo.uisdk.screen.booking.checkout.quotes

import android.content.Context
import android.content.res.Resources
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.uisdk.util.VehicleTags
import com.karhoo.uisdk.util.extension.getCancellationText
import java.util.Locale

class BookingVehicleDetailsPresenter(val view: BookingVehicleDetailsMVP.View) : BookingVehicleDetailsMVP.Presenter {
    override fun checkCancellationSLAMinutes(context: Context, serviceCancellation: ServiceCancellation?, isPrebook: Boolean) {
        val text = serviceCancellation?.getCancellationText(context, isPrebook)

        if (text.isNullOrEmpty()) {
            view.showCancellationText(false)
        } else {
            view.setCancellationText(text)
            view.showCancellationText(true)
        }
    }

    override fun createTagsString(tags: List<VehicleTags>, resources: Resources, shortVersion: Boolean): Spannable {
        val tagsText = SpannableStringBuilder("")

        tags.forEachIndexed { index, tagType ->
            val span: Spannable = SpannableString("  ${tagType.tag.capitalize(Locale.ROOT)} ")
            val icon = tagType.getTagIcon(resources)
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

    companion object {
        private const val INTRINSIC_BOUND_FACTOR = 1.5
    }
}
