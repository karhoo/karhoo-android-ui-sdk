package com.karhoo.uisdk.screen.address.addresslist

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Place
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.address.adapter.AddressAdapter
import com.karhoo.uisdk.screen.address.domain.AddressSearchProvider
import com.karhoo.uisdk.screen.address.domain.AddressSearchProvider.OnAddressesChangedListener
import com.karhoo.uisdk.screen.address.domain.Addresses
import com.karhoo.uisdk.util.extension.hideSoftKeyboard
import kotlinx.android.synthetic.main.uisdk_view_simple_recycler.view.emptyText
import kotlinx.android.synthetic.main.uisdk_view_simple_recycler.view.recycler

class AddressResultListView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), AddressResultListMVP.View, LifecycleObserver {

    private var presenter: AddressResultListMVP.Presenter? = null
    private val addressAdapter: AddressAdapter
    var actions: AddressResultListMVP.Actions? = null

    init {
        inflate(context, R.layout.uisdk_view_simple_recycler, this)

        addressAdapter = AddressAdapter(context, KarhooUISDK.analytics).apply {
            setItemClickListener { _, position, item -> handleSelectedAddress(item, position) }
        }

        recycler.apply {
            layoutManager = GridLayoutManager(context, 1)
            adapter = addressAdapter
        }
    }

    private fun handleSelectedAddress(item: Place, addressPositionInList: Int) {
        focusedChild.hideSoftKeyboard()
        addressAdapter.addressSelected()
        presenter?.onAddressSelected(item, addressPositionInList)
    }

    override fun bindViewToAddresses(addressProvider: AddressSearchProvider) {

        presenter = AddressResultListPresenter(this, KarhooApi.addressService, addressProvider)

        addressProvider.addAddressesObserver(object : OnAddressesChangedListener {
            override fun onAddressesChanged(addresses: Addresses?) {
                addresses?.let {
                    addressAdapter.apply {
                        emptyText.visibility = if (it.locations.locations.isEmpty()) View.VISIBLE else View.GONE
                        setItems(it)
                        addressAdapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    override fun setAddress(location: LocationInfo, addressPositionInList: Int) {
        actions?.addressSelected(location, addressPositionInList)
    }

    override fun showError(@StringRes errorMessage: Int, karhooError: KarhooError?) {
        actions?.showSnackbar(SnackbarConfig(text = resources.getString(errorMessage), karhooError = karhooError))
    }

}
