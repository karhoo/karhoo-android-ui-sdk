package com.karhoo.uisdk.screen.address.domain

import com.karhoo.uisdk.base.listener.ErrorView

interface AddressSearchProvider {

    fun getSessionToken(): String

    fun setSearchQuery(searchQuery: String)

    fun setCurrentLatLong(latitude: Double, longitude: Double)

    fun addAddressesObserver(addressChangedListener: OnAddressesChangedListener)

    fun setErrorView(errorMessageView: ErrorView)

    interface OnAddressesChangedListener {

        fun onAddressesChanged(addresses: Addresses?)

    }

}
