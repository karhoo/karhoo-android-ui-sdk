package com.karhoo.uisdk.screen.booking.supplier

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.supplier.SortMethod
import kotlinx.android.synthetic.main.uisdk_view_supplier_sort.view.etaLabel
import kotlinx.android.synthetic.main.uisdk_view_supplier_sort.view.etaLayout
import kotlinx.android.synthetic.main.uisdk_view_supplier_sort.view.priceLabel
import kotlinx.android.synthetic.main.uisdk_view_supplier_sort.view.priceLayout
import kotlinx.android.synthetic.main.uisdk_view_supplier_sort.view.supplierSortTabLayout

class SupplierSortView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private var unselectedColor: Int = R.color.off_black
    private var selectedColor: Int = R.color.off_white
    private var leftBackgroundDisabled: Int = R.drawable.uisdk_ic_sort_left_disabled
    private var leftBackground: Int = R.drawable.uisdk_sort_left_background

    private var listener: Listener? = null

    private var selectedSortMethod: SortMethod = SortMethod.ETA
    private var hasDestination = false
    private var isPrebook = false

    init {
        inflate(context, R.layout.uisdk_view_supplier_sort, this)

        getCustomisationParameters(context, attrs, defStyleAttr)

        etaLayout.apply {
            isActivated = true
            setOnClickListener { etaClicked() }
        }

        priceLayout.apply {
            isActivated = false
            setOnClickListener { priceClicked() }
        }

        supplierSortTabLayout.apply {
            addTab(supplierSortTabLayout.newTab())
            addTab(supplierSortTabLayout.newTab())
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    if (tab.position == 0) {
                        etaClicked()
                    } else {
                        priceClicked()
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }

    private fun getCustomisationParameters(context: Context, attr: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.SupplierSortView,
                                                        defStyleAttr, R.style.KhSupplierSortView)
        val rightBackground = typedArray.getResourceId(R.styleable
                                                               .SupplierSortView_rightBackground, R
                                                               .drawable.uisdk_sort_right_background)
        leftBackground = typedArray.getResourceId(R.styleable.SupplierSortView_leftBackground, R
                .drawable
                .uisdk_sort_left_background)
        selectedColor = typedArray.getResourceId(R.styleable.SupplierSortView_selectedTextColor, R.color.off_white)
        unselectedColor = typedArray.getResourceId(R.styleable.SupplierSortView_unselectedTextColor, R.color.off_black)
        typedArray.recycle()

        etaLayout.background = ContextCompat.getDrawable(context, leftBackground)
        priceLayout.background = ContextCompat.getDrawable(context, rightBackground)
    }

    private fun etaClicked() {
        if (!isPrebook) {
            setSelectedSortMethod(SortMethod.ETA)
            listener?.onUserChangedSortMethod(selectedSortMethod)
        }
    }

    private fun priceClicked() {
        if (hasDestination) {
            setSelectedSortMethod(SortMethod.PRICE)
            listener?.onUserChangedSortMethod(selectedSortMethod)
        } else {
            listener?.sortChoiceRequiresDestination()
        }
    }

    private fun setSelectedSortMethod(selectedSortMethod: SortMethod) {
        this.selectedSortMethod = selectedSortMethod
        when (selectedSortMethod) {
            SortMethod.ETA -> activateEtaButton()
            SortMethod.PRICE -> if (isPrebook) {
                activatePriceButtonForPrebook()
            } else {
                activatePriceButton()
            }
        }
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

    private fun activatePriceButtonForPrebook() {
        // deactivate eta button
        etaLayout.isActivated = false
        etaLabel.setTextColor(ContextCompat.getColor(context, unselectedColor))
        // activate highPrice button
        priceLayout.isActivated = true
        priceLabel.setTextColor(ContextCompat.getColor(context, selectedColor))
    }

    fun destinationChanged(bookingStatus: BookingStatus?) {
        hasDestination = bookingStatus?.destination != null
        isPrebook = bookingStatus?.date != null
        if (hasDestination && isPrebook) {
            setSelectedSortMethod(SortMethod.PRICE)
        } else {
            setSelectedSortMethod(SortMethod.ETA)
        }
        listener?.onUserChangedSortMethod(selectedSortMethod)
    }

    fun prebookChanged(isPrebook: Boolean) {
        this.isPrebook = isPrebook
        if (isPrebook) {
            disableETA()
        } else {
            enableETA()
        }
    }

    private fun enableETA() {
        setSelectedSortMethod(SortMethod.ETA)
        etaLayout.background = ContextCompat.getDrawable(context, leftBackground)
    }

    private fun disableETA() {
        etaLayout.isActivated = false
        etaLayout.background = ContextCompat.getDrawable(context, leftBackgroundDisabled)
    }

    interface Listener {

        fun onUserChangedSortMethod(sortMethod: SortMethod)

        fun sortChoiceRequiresDestination()

    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }
}
