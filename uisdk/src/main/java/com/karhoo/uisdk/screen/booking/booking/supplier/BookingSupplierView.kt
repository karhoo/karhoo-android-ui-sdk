package com.karhoo.uisdk.screen.booking.booking.supplier

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.widget.TextViewCompat
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.LogoTransformation
import com.karhoo.uisdk.util.extension.convertDpToPixels
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import kotlinx.android.synthetic.main.uisdk_view_booking_supplier.view.categoryText
import kotlinx.android.synthetic.main.uisdk_view_booking_supplier.view.logoImage
import kotlinx.android.synthetic.main.uisdk_view_booking_supplier.view.supplierNameText
import kotlinx.android.synthetic.main.uisdk_view_supplier_item.view.capacityWidget

class BookingSupplierView @JvmOverloads constructor(context: Context,
                                                    attrs: AttributeSet? = null,
                                                    defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), BookingSupplierMVP.View {

    private var headerTextStyle: Int = R.style.Text_Black_Medium_Bold
    private var detailsTextStyle: Int = R.style.Text_Alternative_XSmall

    init {
        inflate(context, R.layout.uisdk_view_booking_supplier, this)
        getCustomisationParameters(context, attrs, defStyleAttr)
    }

    private fun getCustomisationParameters(context: Context, attr: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.BookingSupplierView,
                                                        defStyleAttr, R.style.KhBookingSupplierView)
        headerTextStyle = typedArray.getResourceId(R.styleable
                                                           .BookingSupplierView_headerText, R
                                                           .style
                                                           .Text_Black_Medium_Bold)
        detailsTextStyle = typedArray.getResourceId(R.styleable.BookingSupplierView_detailsText, R
                .style
                .Text_Alternative_XSmall)
        TextViewCompat.setTextAppearance(supplierNameText, headerTextStyle)
        TextViewCompat.setTextAppearance(categoryText, detailsTextStyle)
    }

    fun bindViews(url: String?, supplierName: String, category: String) {
        url?.let { loadImage(it) }
        supplierNameText.text = supplierName
        categoryText.text = String.format("%s%s",
                                          category.substring(0, 1).toUpperCase(),
                                          category.substring(1))
    }

    private fun loadImage(url: String) {
        val logoSize = resources.getDimension(R.dimen.logo_size).convertDpToPixels()

        val picasso = Picasso.with(context)
        val creator: RequestCreator

        creator = if (url.isNotBlank()) {
            picasso.load(url)
        } else {
            picasso.load(R.drawable.uisdk_ic_supplier_logo_empty)
        }

        creator.placeholder(R.drawable.uisdk_ic_supplier_logo_empty)
                .resize(logoSize, logoSize)
                .transform(LogoTransformation(resources.getInteger(R.integer.logo_radius)))
                .into(logoImage)
    }

    override fun setCapacity(luggage: Int, people: Int) {
        capacityWidget.setCapacity(luggage, people)
    }
}