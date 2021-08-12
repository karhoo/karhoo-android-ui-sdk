package com.karhoo.uisdk.screen.booking.checkout.quotes

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseRecyclerAdapter
import com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities.Capability
import com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities.CapabilityAdapter
import com.karhoo.uisdk.util.PicassoLoader
import com.karhoo.uisdk.util.TagType
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.bookingQuoteCancellationText
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.capacityWidget
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.categoryText
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.expandedCapacityList
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.fleetDescription
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.logoImage
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.quoteLearnMoreContainer
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.quoteLearnMoreIcon
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.quoteNameText
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.vehicleTags

class BookingQuotesView @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet? = null,
                                                  defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), BookingQuotesMVP.View {

    private val presenter: BookingQuotesMVP.Presenter = BookingQuotesPresenter(this)
    private var isExpandedSectionShown = false

    init {
        inflate(context, R.layout.uisdk_view_booking_quotes, this)
    }

    fun bindViews(url: String?,
                  quoteName: String,
                  category: String,
                  serviceCancellation: ServiceCancellation?,
                  tags: List<TagType>,
                  description: String?,
                  isPrebook: Boolean) {
        quoteNameText.text = quoteName
        presenter.capitalizeCategory(category)
        presenter.checkCancellationSLAMinutes(context, serviceCancellation, isPrebook)
        loadImage(url)

        if (tags.isNotEmpty()) {
            vehicleTags.text = presenter.createTagsString(tags, resources, !isExpandedSectionShown)
        }

        if (!description.isNullOrBlank()) {
            fleetDescription.text = description
        }

        expandedCapacityList.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, COLUMN_TOTAL_NO)
            adapter = CapabilityAdapter(context)
        }

        quoteLearnMoreContainer.setOnClickListener {
            isExpandedSectionShown = !isExpandedSectionShown

            vehicleTags.text = presenter.createTagsString(tags, resources, !isExpandedSectionShown)

            val arrowIcon = if (isExpandedSectionShown)
                getDrawableResource(R.drawable.kh_uisdk_ic_keyboard_arrow_up_small)
            else
                getDrawableResource(R.drawable.kh_uisdk_ic_arrow_down_small)

            quoteLearnMoreIcon.setImageDrawable(arrowIcon)

            expandLearnMoreSection()
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
        PicassoLoader.loadImage(context,
                                logoImage,
                                url,
                                R.drawable.uisdk_ic_quotes_logo_empty,
                                R.dimen.logo_size,
                                R.integer.logo_radius)
    }

    private fun expandLearnMoreSection() {
        setAnimation(isExpandedSectionShown, expandedCapacityList, isExpandedSectionShown)
        setAnimation(!isExpandedSectionShown, capacityWidget, !isExpandedSectionShown)
        setAnimation(isExpandedSectionShown, fleetDescription, isExpandedSectionShown)
    }

    private fun setAnimation(fadeIn: Boolean,
                             view: View,
                             showView: Boolean) {
        val animationType = if (fadeIn) {
            R.anim.kh_uisdk_fade_in
        } else {
            R.anim.kh_uisdk_fade_out
        }

        val animation = AnimationUtils.loadAnimation(context, animationType)

        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                if (showView) {
                    view.visibility = VISIBLE
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {
                //Do nothing
            }

            override fun onAnimationEnd(animation: Animation?) {
                view.clearAnimation()
                if (!showView) {
                    view.visibility = GONE
                }
            }
        })

        view.startAnimation(animation)
    }

    override fun setCapacity(luggage: Int, people: Int, otherCapabilities: Int) {
        capacityWidget.setCapacity(luggage, people, otherCapabilities)
    }

    override fun setCapabilities(capabilities: List<Capability>) {
        (expandedCapacityList.adapter as BaseRecyclerAdapter<*, *>).items = capabilities
    }

    override fun getDrawableResource(id: Int): Drawable? {
        return ResourcesCompat.getDrawable(resources, id, null)
    }

    companion object {
        private const val COLUMN_TOTAL_NO: Int = 2
    }
}
