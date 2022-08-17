package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

class ServiceAgreementsFilter(selectedTypes: ArrayList<MultiSelectData>) :
    MultiSelectFilter(selectedTypes) {

    override fun meetsCriteria(quote: Quote): Boolean {
        if (selectedTypes.size == 0) {
            return true
        }
        return if (selectedTypes.size == 1)
            quoteContainsFreeCancellation(quote)
                    ||
            quoteContainsFreeWaitingTime(quote)
        else
            quoteContainsFreeCancellation(quote)
                    &&
            quoteContainsFreeWaitingTime(quote)
    }

    private fun quoteContainsFreeCancellation(quote: Quote): Boolean {
        return (quote.serviceAgreements?.freeCancellation != null && selectedTypes.any {
            it.fixedTag?.contains(
                FREE_CANCELLATION_TAG
            ) == true
        })
    }

    private fun quoteContainsFreeWaitingTime(quote: Quote): Boolean {
        return (quote.serviceAgreements?.freeWaitingTime != null && selectedTypes.any {
            it.fixedTag?.contains(
                FREE_WAITING_TIME_TAG
            ) == true
        })
    }

    companion object {
        const val FREE_CANCELLATION_TAG = "free_cancellation"
        const val FREE_WAITING_TIME_TAG = "free_waiting_time"
    }
}
