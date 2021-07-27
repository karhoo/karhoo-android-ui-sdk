package com.karhoo.uisdk

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.MenuHandler
import com.karhoo.uisdk.notification.rides.past.RideNotificationContract
import com.karhoo.uisdk.screen.address.AddressActivity
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.booking.booking.bookingcheckout.activity.BookingCheckoutActivity
import com.karhoo.uisdk.screen.rides.RidesActivity
import com.karhoo.uisdk.screen.rides.detail.RideDetailActivity
import com.karhoo.uisdk.screen.rides.feedback.FeedbackActivity
import com.karhoo.uisdk.screen.trip.TripActivity

object KarhooUISDK {

    var analytics: Analytics? = null
    val karhooApi: KarhooApi = KarhooApi
    var karhooNotification: RideNotificationContract? = null

    object Routing {

        var booking: Class<*> = BookingActivity::class.java

        var bookingRequest: Class<*> = BookingCheckoutActivity::class.java

        var address: Class<*> = AddressActivity::class.java

        var trip: Class<*> = TripActivity::class.java

        var rides: Class<*> = RidesActivity::class.java

        var rideDetail: Class<*> = RideDetailActivity::class.java

        var feedback: Class<*> = FeedbackActivity::class.java
    }

    var menuHandler: MenuHandler? = null

    fun setConfiguration(configuration: KarhooUISDKConfiguration) {
        KarhooUISDKConfigurationProvider.setConfig(configuration)
        KarhooApi.setConfiguration(configuration)
    }
}
