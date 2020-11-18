package com.karhoo.uisdk.screen.booking.domain.quotes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.karhoo.sdk.api.model.Quote

data class LiveFleetsViewModel(private val defaultList: MutableLiveData<List<Quote>> = MutableLiveData())
    : ViewModel() {

    var liveFleets: MutableLiveData<List<Quote>>
        set(value) {
            defaultList.postValue(value.value)
        }
        get() {
            return defaultList
        }
}
