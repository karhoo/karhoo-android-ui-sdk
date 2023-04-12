package com.karhoo.uisdk.screen.rideplanning

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import com.google.android.gms.maps.model.LatLng
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutViewContract
import com.karhoo.uisdk.screen.booking.map.BookingMapMVP
import org.joda.time.DateTime

interface RidePlanningContract {
    interface View : Common {
        var checkoutObserver: Observer<in CheckoutViewContract.Action>
        var handleNavigationVisiblityCallback: (toolbarVisibility: Int, navigationMode: Int) -> Unit

        fun setup(presenter: Presenter?,
                  savedInstanceState: Bundle?,
                  viewModelStoreOwner: ViewModelStoreOwner
        )
        fun saveInstanceState(bundle: Bundle)
        fun setMapLocation(latLng: LatLng)
        fun getViewModelStoreOwner(): ViewModelStoreOwner
        fun bindViews(context: Context)
        fun validateCoverage(hasAddresses: Boolean, hasCoverage: Boolean)
        fun startTripAllocation(tripInfo: TripInfo)
        fun onBookingCancelledOrFinished()
        fun initializeListeners(mapActions: BookingMapMVP.Actions)

        //TODO Unify these 2
        fun setAddresses(requestCode: Int, resultCode: Int, data: Intent?)
        fun setAddresses(pickup: LocationInfo?, destination: LocationInfo?, date: DateTime?)
    }

    interface Presenter : Common {
        fun setup(view: View, bundle: Bundle?, ridePlanningCoordinator: RidePlanningCoordinator)
        fun saveInstanceState(bundle: Bundle)
        fun hasLocationPermissions(context: Context): Boolean
        fun requestLocationPermissions(context: Context)
        fun onBookingModeSelected(isPrebook: Boolean)
        fun initializeListeners()
    }

    interface Common {
        fun parseExtrasBundle(extras: Bundle?)
        fun onReceivedPermissionsResult(context: Context, requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean
        fun parseDataFromActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }
}
