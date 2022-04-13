package com.karhoo.uisdk.screen.booking.quotes.filterview

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_filter_multi_select_checkbox.view.filterViewItemTitle
import kotlinx.android.synthetic.main.uisdk_view_filter_multi_select_checkbox.view.filterViewItemCheckboxGroup

class MultiSelectCheckboxFilterView  @JvmOverloads constructor(context: Context,
                                                               attrs: AttributeSet? = null,
                                                               defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    var filter: MultiSelectFilter? = null

    var delegate: (() -> Unit)? = null
        set(value) {
            field = value
            value?.invoke()
        }

    var choices: HashMap<String, String> = HashMap()
        set(value) {
            field = value
            for((key, mapValue) in choices){
                val box = CheckBox(ContextThemeWrapper(context, R.style.KhFilterViewCheckBox))
                box.text = key
                if(filter?.selectedTypes?.contains(mapValue) == true)
                    box.isChecked = true
                box.setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked)
                        filter?.addSelected(mapValue.lowercase())
                    else
                        filter?.removeSelected(mapValue.lowercase())
                    delegate?.invoke()
                }
                filterViewItemCheckboxGroup.addView(box)
            }
        }

    init {
        View.inflate(context, R.layout.uisdk_view_filter_multi_select_checkbox, this)
    }

    fun setTitle(title: String){
        filterViewItemTitle.text = title
    }
}
