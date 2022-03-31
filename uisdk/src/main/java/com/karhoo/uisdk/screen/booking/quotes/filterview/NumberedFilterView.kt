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

    init {
        View.inflate(context, R.layout.uisdk_view_filter_numbered, this)

        val filterViewItemPlusImage = findViewById<ImageView>(R.id.filterViewItemPlusImage)
        val filterViewItemMinusImage = findViewById<ImageView>(R.id.filterViewItemMinusImage)
        val filterViewItemValue = findViewById<TextView>(R.id.filterViewItemValue)

        filterViewItemPlusImage.setOnClickListener {
            if (filter?.increment() == true) {
                delegate?.invoke()
                filterViewItemValue.text = filter?.currentNumber.toString()
            }
        }

        filterViewItemMinusImage.setOnClickListener {
            if (filter?.decrement() == true) {
                delegate?.invoke()
                filterViewItemValue.text = filter?.currentNumber.toString()
            }
        }
    }

    fun setTitle(title: String) {
        filterViewItemTitle.text = title
    }
}
