package com.karhoo.uisdk.screen.address

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.model.LatLng
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.base.address.AddressCodes
import com.karhoo.uisdk.base.address.AddressType
import com.karhoo.uisdk.screen.address.addresslist.AddressResultListMVP
import com.karhoo.uisdk.screen.address.domain.AddressSearchProvider
import com.karhoo.uisdk.screen.address.domain.KarhooAddressProvider
import com.karhoo.uisdk.screen.address.map.AddressMapMVP
import com.karhoo.uisdk.screen.address.options.AddressOptionsMVP
import com.karhoo.uisdk.screen.address.recents.RecentsListView
import com.karhoo.uisdk.screen.address.search.AddressSearchView
import com.karhoo.uisdk.util.extension.orZero
import kotlinx.android.synthetic.main.uisdk_activity_address.addressMapView
import kotlinx.android.synthetic.main.uisdk_activity_address.addressOptionsWidget
import kotlinx.android.synthetic.main.uisdk_activity_address.addressResultListWidget
import kotlinx.android.synthetic.main.uisdk_activity_address.addressSearchWidget
import kotlinx.android.synthetic.main.uisdk_activity_address.recentsListWidget
import kotlinx.android.synthetic.main.uisdk_activity_address.toolbar

class AddressActivity : BaseActivity(), AddressResultListMVP.Actions, AddressSearchView.Actions,
                        RecentsListView.Actions, AddressMapMVP.Actions, AddressOptionsMVP.Actions {

    private var addressType: AddressType? = null
    private val addressSearchProvider: AddressSearchProvider =
            KarhooAddressProvider(KarhooUISDK.analytics, KarhooApi.addressService)

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override val layout: Int = R.layout.uisdk_activity_address

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        addressMapView.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        addressMapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        addressMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        addressMapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        addressMapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        addressMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        addressMapView.onLowMemory()
    }

    override fun handleExtras() {
        addressType = extras?.getSerializable(Builder.EXTRA_ADDRESS_TYPE) as AddressType
        latitude = extras?.getDouble(Builder.EXTRA_PICKUP_LATITUDE).orZero()
        longitude = extras?.getDouble(Builder.EXTRA_PICKUP_LONGITUDE).orZero()
        addressMapView.initialLocation = LatLng(latitude, longitude)
    }

    override fun bindViews() {

        addressSearchProvider.apply {
            setCurrentLatLong(latitude, longitude)
            setErrorView(this@AddressActivity)
        }

        addressSearchWidget.apply {
            setHint(if (addressType == AddressType.PICKUP) getString(R.string.kh_uisdk_enter_pickup) else getString(R.string.kh_uisdk_enter_destination))
            setAddressSearchProvider(addressSearchProvider)
            actions = this@AddressActivity
        }

        addressResultListWidget.apply {
            bindViewToAddresses(addressSearchProvider)
            actions = this@AddressActivity
        }

        addressMapView.apply {
            actions = this@AddressActivity
            setAddressType(addressType)
        }

        addressOptionsWidget.apply {
            actions = this@AddressActivity
        }

        recentsListWidget.actions = this
        lifecycle.addObserver(addressResultListWidget)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun addressSelected(location: LocationInfo, addressPositionInList: Int) {
        recentsListWidget.saveLocationToRecents(location)
        setAddressResultAndFinish(location, addressPositionInList)
    }

    override fun recentAddressSelected(location: LocationInfo, addressPositionInList: Int) {
        setAddressResultAndFinish(location, addressPositionInList)
    }

    private fun setAddressResultAndFinish(location: LocationInfo, addressPositionInList: Int) {
        val data = Intent()
        data.putExtra(AddressCodes.DATA_ADDRESS, location)
        data.putExtra(AddressCodes.DATA_POSITION_IN_LIST, addressPositionInList)
        setResult(RESULT_OK, data)
        finish()
    }

    override fun showRecents() {
        addressResultListWidget.visibility = View.GONE
        recentsListWidget.visibility = View.VISIBLE
    }

    override fun showResults() {
        recentsListWidget.visibility = View.GONE
        addressResultListWidget.visibility = View.VISIBLE
    }

    override fun pickFromMap() {
        addressMapView.visibility = View.VISIBLE
    }

    override fun didSelectCurrentLocation(location: LocationInfo) {
        val data = Intent()
        data.putExtra(AddressCodes.DATA_ADDRESS, location)
        setResult(RESULT_OK, data)
        finish()
    }

    class Builder private constructor() {

        private val extras: Bundle = Bundle()

        /**
         *  The activity will use the [addressType] to display in its UI whether the user
         *  is searching for their pickup or destination
         */
        fun addressType(addressType: AddressType): Builder {
            extras.putSerializable(EXTRA_ADDRESS_TYPE, addressType)
            return this
        }

        /**
         *  Passes up the [latitude] and [longitude] with the address
         *  search to help locate the address
         */
        fun locationBias(latitude: Double, longitude: Double): Builder {
            extras.putDouble(EXTRA_PICKUP_LATITUDE, latitude)
            extras.putDouble(EXTRA_PICKUP_LONGITUDE, longitude)
            return this
        }

        /**
         * Returns a launchable Intent to the configured booking activity with the given
         * builder parameters in the extras bundle
         */
        fun build(context: Context): Intent {
            val intent = Intent(context, KarhooUISDK.Routing.address)
            intent.putExtras(extras)
            return intent
        }

        companion object {

            const val EXTRA_ADDRESS_TYPE = "address::type"
            const val EXTRA_PICKUP_LATITUDE = "latitude::pickup"
            const val EXTRA_PICKUP_LONGITUDE = "longitude::pickup"

            val builder: Builder
                get() = Builder()
        }
    }

}
