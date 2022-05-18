package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

class ServiceAgreementsFilter  (selectedTypes: ArrayList<String>) : MultiSelectFilter(selectedTypes) {

    override fun meetsCriteria(quote: Quote): Boolean {
        if (selectedTypes.size == 0) {
            return true
        }
        return (quote.serviceAgreements?.freeCancellation != null && selectedTypes.contains(FREE_CANCELLATION_TAG))
                ||
                (quote.serviceAgreements?.freeWaitingTime != null && selectedTypes.contains(FREE_WAITING_TIME_TAG))
    }

    companion object {
        const val FREE_CANCELLATION_TAG = "free_cancellation"
        const val FREE_WAITING_TIME_TAG = "free_waiting_time"
    }
}
