package com.karhoo.uisdk.screen.booking.quotes.filterview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_filter_numbered.view.*

class NumberedFilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var filter: NumberedFilter? = null
        set(value) {
            field = value
            filterViewItemValue.text = filter?.currentNumber.toString()
        }

    var delegate: (() -> Unit)? = null
        set(value) {
            field = value
            value?.invoke()
        }

    var icon: Int? = null
        set(value) {
            field = value
            if (value != null) {
                filterViewItemImage.setImageResource(value)
            }
        }

    init {
        View.inflate(context, R.layout.uisdk_view_filter_numbered, this)

        val filterViewItemPlusImage = findViewById<ImageView>(R.id.filterViewItemPlusImage)
        val filterViewItemMinusImage = findViewById<ImageView>(R.id.filterViewItemMinusImage)
        val filterViewItemValue = findViewById<TextView>(R.id.filterViewItemValue)

        filterViewItemPlusImage.setOnClickListener {
            if (filter?.increment() == true) {
                delegate?.invoke()
                filterViewItemValue.text = filter?.currentNumber.toString()

                if (filter?.canFurtherDecrement() == true) {
                    filterViewItemMinusImage.setImageResource(R.drawable.kh_uisdk_ic_minus_colored)
                } else {
                    filterViewItemMinusImage.setImageResource(R.drawable.kh_uisdk_ic_minus_colored_disabled)
                }
                if (filter?.canFurtherIncrement() == true) {
                    filterViewItemPlusImage.setImageResource(R.drawable.kh_uisdk_ic_plus_colored)
                } else {
                    filterViewItemPlusImage.setImageResource(R.drawable.kh_uisdk_ic_plus_colored_disabled)
                }
            }
        }

        filterViewItemMinusImage.setOnClickListener {
            if (filter?.decrement() == true) {
                delegate?.invoke()
                filterViewItemValue.text = filter?.currentNumber.toString()

                if (filter?.canFurtherDecrement() == true) {
                    filterViewItemMinusImage.setImageResource(R.drawable.kh_uisdk_ic_minus_colored)
                } else {
                    filterViewItemMinusImage.setImageResource(R.drawable.kh_uisdk_ic_minus_colored_disabled)
                }

                if (filter?.canFurtherIncrement() == true) {
                    filterViewItemPlusImage.setImageResource(R.drawable.kh_uisdk_ic_plus_colored)
                } else {
                    filterViewItemPlusImage.setImageResource(R.drawable.kh_uisdk_ic_plus_colored_disabled)
                }
            }
        }
    }

    fun setTitle(title: String) {
        filterViewItemTitle.text = title
    }
}
