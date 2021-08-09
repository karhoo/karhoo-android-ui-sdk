package com.karhoo.uisdk.screen.booking.checkout.quotes

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseRecyclerAdapter
import com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities.Capability
import com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities.CapabilityAdapter
import com.karhoo.uisdk.util.PicassoLoader
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.*

class BookingQuotesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), BookingQuotesMVP.View {

    private val presenter: BookingQuotesMVP.Presenter = BookingQuotesPresenter(this)
    private var isExpandedSectionShown = false

    init {
        inflate(context, R.layout.uisdk_view_booking_quotes, this)
    }

    fun bindViews(
        url: String?,
        quoteName: String,
        category: String,
        serviceCancellation: ServiceCancellation?,
        tags: List<String>,
        isPrebook: Boolean
    ) {
        quoteNameText.text = quoteName
        presenter.capitalizeCategory(category)
        presenter.checkCancellationSLAMinutes(context, serviceCancellation, isPrebook)
        loadImage(url)

        if (tags.isNotEmpty()) {
            vehicleTags.text = presenter.createTagsString(tags, !isExpandedSectionShown)
        }

        expandedCapacityList.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, COLUMN_TOTAL_NO)
            adapter = CapabilityAdapter(context)
        }

        quoteLearnMoreContainer.setOnClickListener {
            isExpandedSectionShown = !isExpandedSectionShown

            vehicleTags.text = presenter.createTagsString(tags, !isExpandedSectionShown)

            val arrowIcon = if (isExpandedSectionShown)
                getDrawableResource(R.drawable.kh_uisdk_ic_keyboard_arrow_up_small)
            else
                getDrawableResource(R.drawable.kh_uisdk_ic_arrow_down_small)

            quoteLearnMoreIcon.setImageDrawable(arrowIcon)

            if (isExpandedSectionShown) {
                expandedCapacityList.animate().alpha(VISIBLE_ALPHA).withEndAction {
                    expandedCapacityList.visibility = VISIBLE
                }.duration = SHORT_ANIMATION_DURATION
                capacityWidget.animate().alpha(TRANSPARENT_ALPHA).duration =
                    SHORT_ANIMATION_DURATION
            } else {
                expandedCapacityList.animate().alpha(TRANSPARENT_ALPHA).withEndAction {
                    expandedCapacityList.visibility = GONE
                }.duration = SHORT_ANIMATION_DURATION
                capacityWidget.animate().alpha(VISIBLE_ALPHA).withEndAction {
                    capacityWidget.visibility = VISIBLE
                }.duration = SHORT_ANIMATION_DURATION
            }
        }
    }

    override fun setCancellationText(text: String) {
        bookingQuoteCancellationText.text = text
    }

    override fun showCancellationText(show: Boolean) = if (show) {
        bookingQuoteCancellationText.visibility = VISIBLE
    } else {
        bookingQuoteCancellationText.visibility = GONE
    }

    override fun setCategoryText(text: String) {
        categoryText.text = text
    }

    private fun loadImage(url: String?) {
        PicassoLoader.loadImage(
            context,
            logoImage,
            url,
            R.drawable.uisdk_ic_quotes_logo_empty,
            R.dimen.logo_size,
            R.integer.logo_radius
        )
    }

    override fun setCapacity(luggage: Int, people: Int) {
        capacityWidget.setCapacity(luggage, people, 2)
    }

    override fun setCapabilities(capabilities: List<Capability>) {
        (expandedCapacityList.adapter as BaseRecyclerAdapter<*, *>).items = capabilities
    }

    override fun getDrawableResource(id: Int): Drawable? {
        return ResourcesCompat.getDrawable(resources, id, null)
    }

    companion object {
        const val SHORT_ANIMATION_DURATION: Long = 200
        const val TRANSPARENT_ALPHA: Float = 0.0f
        const val VISIBLE_ALPHA: Float = 1.0F
        private const val COLUMN_TOTAL_NO: Int = 2
    }
}
