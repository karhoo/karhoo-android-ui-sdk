package com.karhoo.uisdk.screen.booking.quotes.filterview

open class NumberedFilter(private val defaultNumber: Int, private  val maxNumber: Int = 7): IFilter {

    var currentNumber: Int = defaultNumber

    override fun clearFilter() {
        super.clearFilter()
        currentNumber = defaultNumber
    }

    override val isFilterApplied: Boolean?
        get() { return currentNumber != defaultNumber }

    fun increment(): Boolean {
        if(currentNumber < maxNumber) {
            currentNumber++
            return true
        }
        return false
    }

    fun decrement(): Boolean {
        if(currentNumber > defaultNumber) {
            currentNumber--
            return true
        }
        return false
    }
}
