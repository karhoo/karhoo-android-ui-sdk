package com.karhoo.uisdk.screen.booking.supplier.category

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.karhoo.uisdk.R

internal class CategoryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private val categoryName: TextView
    private var selectedColor: Int = R.color.text_primary
    private var unselectedColor: Int = R.color.off_black
    private var unavailableColor: Int = R.color.text_unavailable

    init {
        inflate(context, R.layout.uisdk_view_unavailable_category, this)
        getCustomisationParameters(context, attrs, defStyleAttr)
        categoryName = findViewById(R.id.nameText)
    }

    private fun getCustomisationParameters(context: Context, attr: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.CategorySelectorView,
                                                        defStyleAttr, R.style.KhVehicleClassTabs)
        selectedColor = typedArray.getResourceId(R.styleable
                                                         .CategorySelectorView_selectedTabTextColor, R.color
                                                         .text_primary)
        unselectedColor = typedArray.getResourceId(R.styleable
                                                           .CategorySelectorView_unselectedTabTextColor, R.color.off_black)
        unavailableColor = typedArray.getResourceId(R.styleable
                                                            .CategorySelectorView_unavailableTabTextColor, R.color
                                                            .text_unavailable)
        typedArray.recycle()
    }

    fun setCategoryName(categoryName: String) {
        if (categoryName != resources.getString(R.string.all_category)) {
            val resId = resources.getIdentifier(categoryName.toLowerCase(), "string", context.packageName)
            if (resId == 0) {
                this.categoryName.text = categoryName
            } else {
                this.categoryName.text = resources.getString(resId)
            }
        } else {
            this.categoryName.text = categoryName
        }
    }

    fun setCategoryAvailable(available: Boolean) {
        val colorResourceId = if (available) unselectedColor else unavailableColor
        categoryName.setTextColor(ContextCompat.getColor(context, colorResourceId))
        this.isClickable = !available
    }

    fun setTabTextColor(isSelected: Boolean) {
        categoryName.setTextColor(ContextCompat.getColor(context, if (isSelected) selectedColor
        else unselectedColor))
    }
}
