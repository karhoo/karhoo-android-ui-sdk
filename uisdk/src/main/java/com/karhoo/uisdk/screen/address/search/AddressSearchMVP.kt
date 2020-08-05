package com.karhoo.uisdk.screen.address.search

import com.karhoo.uisdk.screen.address.domain.AddressSearchProvider

interface AddressSearchMVP {

    interface View {

        fun setHint(hint: String)

        fun setAddressSearchProvider(addressProvider: AddressSearchProvider)

        fun clearSearch()

        fun showRecents()

        fun showResults()

    }

    interface Presenter {

        fun searchUpdated(query: String)

        fun onClearSearch()

    }

}
