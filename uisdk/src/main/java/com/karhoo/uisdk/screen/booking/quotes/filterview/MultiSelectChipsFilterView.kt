package com.karhoo.uisdk.screen.booking.quotes.filterview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
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

    private val chipViews = ArrayList<Chip>()

    var chips: ArrayList<MultiSelectData> = ArrayList()
        set(value) {
            field = value
            for(item in chips){
                val chip = Chip(context)
                chip.text = item.text
                val chipDrawable = ChipDrawable.createFromAttributes(
                    context,
                    null,
                    0,
                    R.style.KhFilterChip
                )
                chip.setTextColor(ContextCompat.getColorStateList(context, R.color.kh_uisdk_quote_list_filter_chips_text))
                chip.setChipDrawable(chipDrawable)
                item.icon?.let {
                    chip.chipIcon = ContextCompat.getDrawable(context, it)
                    chip.checkedIcon = ContextCompat.getDrawable(context, it)
                    chip.chipIconTint = ContextCompat.getColorStateList(context, R.color.kh_uisdk_quote_list_filter_chips_text)
                    chip.checkedIconTint = ContextCompat.getColorStateList(context, R.color.kh_uisdk_quote_list_filter_chips_text)
                    chip.chipIconSize = resources.getDimension(R.dimen.kh_uisdk_text_size_large)
                    chip.isChipIconVisible = true
                }?: kotlin.run {
                    chip.chipIconSize = resources.getDimension(R.dimen.kh_uisdk_spacing_none)
                    chip.isChipIconVisible = false
                }
                chip.minimumWidth = 0
                chip.setEnsureMinTouchTargetSize(false)

                chip.setOnClickListener {
                    if(chip.isChecked){
                        filter?.addSelected(item)
                    }
                    else{
                        filter?.removeSelected(item)
                    }

                    delegate?.invoke()
                }
                filterViewItemChipGroup.addView(chip)
                chipViews.add(chip)
                if(filter?.selectedTypes?.map { it.fixedTag }?.contains(item.fixedTag) == true){
                    chip.performClick()
                }
            }
        }

    init {
        View.inflate(context, R.layout.uisdk_view_filter_multi_select_chips, this)
    }

    fun setTitle(title: String){
        filterViewItemTitle.text = title
    }
}
