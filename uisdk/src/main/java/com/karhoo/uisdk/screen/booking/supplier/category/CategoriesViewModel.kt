package com.karhoo.uisdk.screen.booking.supplier.category

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class CategoriesViewModel(private val defaultCategories: MutableLiveData<List<Category>> = MutableLiveData())
    : ViewModel() {

    var categories: MutableLiveData<List<Category>>
        set(value) {
            defaultCategories.postValue(value.value)
        }
        get() {
            return defaultCategories
        }
}
