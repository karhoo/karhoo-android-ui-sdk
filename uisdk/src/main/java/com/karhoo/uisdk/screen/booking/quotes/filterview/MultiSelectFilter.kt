package com.karhoo.uisdk.screen.booking.quotes.filterview

open class MultiSelectFilter(var selectedTypes: ArrayList<MultiSelectData>): IFilter {

    fun addSelected(choice: MultiSelectData){
        if(!selectedTypes.contains(choice))
            selectedTypes.add(choice)
    }

    fun removeSelected(choice: MultiSelectData){
        selectedTypes.remove(choice)
    }

    override fun clearFilter() {
        selectedTypes.clear()
    }

    override val isFilterApplied: Boolean?
        get() { return selectedTypes.isNotEmpty() }

    var typeValues: ArrayList<MultiSelectData> = ArrayList()
}
