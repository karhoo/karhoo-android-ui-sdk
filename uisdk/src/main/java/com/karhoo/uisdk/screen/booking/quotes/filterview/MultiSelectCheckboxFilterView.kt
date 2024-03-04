package com.karhoo.uisdk.screen.booking.quotes.filterview

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.karhoo.uisdk.R

class MultiSelectCheckboxFilterView  @JvmOverloads constructor(context: Context,
                                                               attrs: AttributeSet? = null,
                                                               defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    var filter: MultiSelectFilter? = null

    private lateinit var filterViewItemTitle: TextView
    private lateinit var filterViewItemCheckboxGroup: LinearLayout

    var delegate: (() -> Unit)? = null
        set(value) {
            field = value
            value?.invoke()
        }

    var choices = ArrayList<MultiSelectData>()
        set(value) {
            field = value
            value.forEach { data ->
                val box = CheckBox(ContextThemeWrapper(context, R.style.KhFilterViewCheckBox))
                box.text = data.text
                box.setTextColor(ContextCompat.getColor(context, R.color.kh_uisdk_headline))
                if(filter?.selectedTypes?.map { it.fixedTag }?.contains(data.fixedTag) == true)
                    box.isChecked = true

                box.setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked)
                        filter?.addSelected(data)
                    else
                        filter?.selectedTypes?.remove(filter?.selectedTypes?.firstOrNull { it.fixedTag == data.fixedTag })
                    delegate?.invoke()
                }
                filterViewItemCheckboxGroup.addView(box)
            }
        }

    init {
        View.inflate(context, R.layout.uisdk_view_filter_multi_select_checkbox, this)

        filterViewItemTitle = findViewById(R.id.filterViewItemTitle)
        filterViewItemCheckboxGroup = findViewById(R.id.filterViewItemCheckboxGroup)
    }

    fun setTitle(title: String){
        filterViewItemTitle.text = title
    }
}
