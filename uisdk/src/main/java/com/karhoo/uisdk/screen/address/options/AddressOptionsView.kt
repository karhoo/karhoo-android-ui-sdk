package com.karhoo.uisdk.screen.address.options

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.google.android.gms.common.api.ResolvableApiException
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationProvider
import com.karhoo.uisdk.util.extension.isLocateMeEnabled

class AddressOptionsView @JvmOverloads constructor(context: Context,
                                                   attrs: AttributeSet? = null,
                                                   defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), AddressOptionsMVP.View {

    var actions: AddressOptionsMVP.Actions? = null
    private var presenter: AddressOptionsMVP.Presenter = AddressOptionsPresenter(this, locationProvider = LocationProvider(context))

    private lateinit var addressOptions: LinearLayout
    private lateinit var currentLocation: LinearLayout
    private lateinit var setOnMap: LinearLayout

    init {
        inflate(context, R.layout.uisdk_view_address_options, this)

        addressOptions = findViewById(R.id.addressOptions)
        currentLocation = findViewById(R.id.currentLocation)
        setOnMap = findViewById(R.id.setOnMap)

        if (shouldShowMapSearchOptions()) {

            setOnMap.setOnClickListener {
                actions?.pickFromMap()
            }

            currentLocation.setOnClickListener {
                presenter.getCurrentLocation()
            }
        } else {
            addressOptions.visibility = View.GONE
            currentLocation.visibility = View.GONE
        }
    }

    private fun shouldShowMapSearchOptions(): Boolean {
        return isLocateMeEnabled(context)
    }

    override fun didGetCurrentLocation(location: LocationInfo) {
        actions?.didSelectCurrentLocation(location)
    }

    override fun resolveLocationApiException(resolvableApiException: ResolvableApiException) {
        resolvableApiException.startResolutionForResult((context as Activity), 1)
    }

    override fun showSnackbar(snackbarConfig: SnackbarConfig) {
        actions?.showSnackbar(snackbarConfig)
    }
}
