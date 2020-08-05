package com.karhoo.uisdk.screen.address.search

import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.address.domain.AddressSearchProvider

class AddressSearchPresenter(view: AddressSearchMVP.View,
                             private val addressProvider: AddressSearchProvider) : BasePresenter<AddressSearchMVP.View>(), AddressSearchMVP.Presenter {

    private var lastSearchQuery: String = ""

    init {
        attachView(view)
    }

    override fun searchUpdated(query: String) {
        addressProvider.setSearchQuery(query)
        if (lastSearchQuery.isNotBlank() && query.isBlank()) {
            view?.showRecents()
        } else if (lastSearchQuery.isBlank()) {
            view?.showResults()
        }
        this.lastSearchQuery = query
    }

    override fun onClearSearch() {
        searchUpdated("")
        view?.clearSearch()
    }
}
