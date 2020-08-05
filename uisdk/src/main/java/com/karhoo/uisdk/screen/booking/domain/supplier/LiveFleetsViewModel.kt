package com.karhoo.uisdk.screen.booking.domain.supplier

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteV2

data class LiveFleetsViewModel(private val defaultList: MutableLiveData<List<QuoteV2>> = MutableLiveData())
    : ViewModel() {

    var liveFleets: MutableLiveData<List<QuoteV2>>
        set(value) {
            defaultList.postValue(value.value)
        }
        get() {
            return defaultList
        }
}