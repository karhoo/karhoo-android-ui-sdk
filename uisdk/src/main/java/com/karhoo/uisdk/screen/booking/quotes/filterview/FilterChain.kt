package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.screen.booking.quotes.fragment.QuotesFragmentPresenter

class FilterChain {
    var filters: MutableList<IFilter> = mutableListOf()

    fun add(filter: IFilter){
        filters.add(filter)
    }

    fun applyFilters(quotes: List<Quote>): List<Quote> {
        val filteredList: MutableList<Quote> = mutableListOf()

        for(quote in quotes){
            var eligible: Boolean = true
            for(filter in filters) {
                if(!filter.meetsCriteria(quote)){
                    eligible = false
                    break
                }
            }
            if(eligible){
                filteredList.add(quote)
            }
        }
        return filteredList.filter {
            it.vehicle.vehicleQta.highMinutes <= QuotesFragmentPresenter.MAX_ACCEPTABLE_QTA
        }
    }
}
