package com.karhoo.uisdk.screen.booking.quotes

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import kotlinx.android.synthetic.main.uisdk_view_quotes_sort.view.etaLabel
import kotlinx.android.synthetic.main.uisdk_view_quotes_sort.view.etaLayout
import kotlinx.android.synthetic.main.uisdk_view_quotes_sort.view.priceLabel
import kotlinx.android.synthetic.main.uisdk_view_quotes_sort.view.priceLayout
import kotlinx.android.synthetic.main.uisdk_view_quotes_sort.view.quotesSortTabLayout

class QuotesSortView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private var unselectedColor: Int = R.color.kh_uisdk_off_black
    private var selectedColor: Int = R.color.kh_uisdk_off_white
    private var leftBackground: Int = R.drawable.uisdk_sort_left_background

    private var listener: Listener? = null

    private var selectedSortMethod: SortMethod = SortMethod.PRICE
    private var hasDestination = false
    private var isPrebook = false

    init {
        inflate(context, R.layout.uisdk_view_quotes_sort, this)

        getCustomisationParameters(context, attrs, defStyleAttr)

        etaLayout.apply {
            isActivated = true
            setOnClickListener { etaClicked() }
        }

        priceLayout.apply {
            isActivated = false
            setOnClickListener { priceClicked() }
        }

        quotesSortTabLayout.apply {
            addTab(quotesSortTabLayout.newTab())
            addTab(quotesSortTabLayout.newTab())
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    setSortingMethodByTabPosition(tab.position)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    // Do nothing
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    setSortingMethodByTabPosition(tab.position)
                }
            })
        }
        setSelectedSortMethod(SortMethod.PRICE)
    }

    private fun getCustomisationParameters(context: Context, attr: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.QuotesSortView,
                defStyleAttr, R.style.KhQuotesSortView)
        val rightBackground = typedArray.getResourceId(R.styleable
                .QuotesSortView_rightBackground, R
                .drawable.uisdk_sort_right_background)
        leftBackground = typedArray.getResourceId(R.styleable.QuotesSortView_leftBackground, R
                .drawable
                .uisdk_sort_left_background)
        selectedColor = typedArray.getResourceId(R.styleable.QuotesSortView_selectedTextColor, R.color.kh_uisdk_off_white)
        unselectedColor = typedArray.getResourceId(R.styleable.QuotesSortView_unselectedTextColor, R.color.kh_uisdk_off_black)
        typedArray.recycle()

        etaLayout.background = ContextCompat.getDrawable(context, leftBackground)
        priceLayout.background = ContextCompat.getDrawable(context, rightBackground)
    }

    private fun etaClicked() {
        if (!isPrebook) {
            setSelectedSortMethod(SortMethod.ETA)
        }
    }

    private fun priceClicked() {
        if (hasDestination) {
            setSelectedSortMethod(SortMethod.PRICE)
        } else {
            listener?.sortChoiceRequiresDestination()
        }
    }

    private fun setSelectedSortMethod(selectedSortMethod: SortMethod) {
        this.selectedSortMethod = selectedSortMethod
        when (selectedSortMethod) {
            SortMethod.ETA -> activateEtaButton()
            SortMethod.PRICE -> activatePriceButton()
        }

        listener?.onUserChangedSortMethod(selectedSortMethod)
    }

    private fun activateEtaButton() {
        // deactivate highPrice button
        priceLayout.isActivated = false
        priceLabel.setTextColor(ContextCompat.getColor(context, unselectedColor))
        // activate eta button
        etaLayout.isActivated = true
        etaLabel.setTextColor(ContextCompat.getColor(context, selectedColor))
    }

    private fun activatePriceButton() {
        // deactivate eta button
        etaLayout.isActivated = false
        etaLabel.setTextColor(ContextCompat.getColor(context, unselectedColor))
        // activate highPrice button
        priceLayout.isActivated = true
        priceLabel.setTextColor(ContextCompat.getColor(context, selectedColor))
    }

    fun destinationChanged(journeyDetails: JourneyDetails?) {
        hasDestination = journeyDetails?.destination != null
    }

    private fun setSortingMethodByTabPosition(position: Int) = if (position == 0) {
        etaClicked()
    } else {
        priceClicked()
    }

    interface Listener {

        fun onUserChangedSortMethod(sortMethod: SortMethod)

        fun sortChoiceRequiresDestination()

    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }
}
