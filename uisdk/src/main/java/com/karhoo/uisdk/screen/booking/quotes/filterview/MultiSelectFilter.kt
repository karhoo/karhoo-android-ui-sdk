package com.karhoo.uisdk.screen.booking.quotes.filterview

open class MultiSelectFilter(var selectedTypes: ArrayList<String>): IFilter {

    fun addSelected(choice: String){
        selectedTypes.add(choice)
    }

    fun removeSelected(choice: String){
        selectedTypes.remove(choice)
    }

    override fun clearFilter() {
        selectedTypes.clear()
    }
}
