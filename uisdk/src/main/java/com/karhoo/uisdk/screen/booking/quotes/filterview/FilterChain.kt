package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

class FilterChain {
    var filters: MutableList<IFilter> = mutableListOf()

    fun add(filter: IFilter){
        filters.add(filter)
    }

    fun applyFilters(quotes: List<Quote>): List<Quote> {
        val filteredList: MutableList<Quote> = mutableListOf()

        for(quote in quotes){
            var stay: Boolean = true
            for(filter in filters) {
                if(!filter.meetsCriteria(quote)){
                    stay = false
                    break
                }
            }
            if(stay){
                filteredList.add(quote)
            }
        }
        return filteredList
    }
}
