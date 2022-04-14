package com.karhoo.uisdk.screen.booking.quotes.filterview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_filter_multi_select_chips.view.*


class MultiSelectChipsFilterView @JvmOverloads constructor(context: Context,
                                                           attrs: AttributeSet? = null,
                                                           defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    var filter: MultiSelectFilter? = null

    var delegate: (() -> Unit)? = null
        set(value) {
            field = value
            value?.invoke()
        }

    var chips: ArrayList<String> = ArrayList()
        set(value) {
            field = value
            for(text in chips){
                val chip = Chip(context)
                chip.text = text
            val chipDrawable = ChipDrawable.createFromAttributes(
                context,
                null,
                0,
                R.style.KhFilterChip
            )
            chip.setChipDrawable(chipDrawable)
            chip.setOnClickListener {
                if(chip.isChecked)
                    filter?.addSelected(chip.text.toString().lowercase())
                else
                    filter?.removeSelected(chip.text.toString().lowercase())
                delegate?.invoke()
            }
            filterViewItemChipGroup.addView(chip)
            }
        }

    init {
        View.inflate(context, R.layout.uisdk_view_filter_multi_select_chips, this)
    }

    fun setTitle(title: String){
        filterViewItemTitle.text = title
    }
}
