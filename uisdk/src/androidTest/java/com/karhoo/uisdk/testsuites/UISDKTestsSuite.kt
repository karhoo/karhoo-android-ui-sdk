package com.karhoo.uisdk.testsuites

import com.karhoo.uisdk.address.AddressTests
import com.karhoo.uisdk.booking.adyen.AdyenBookingTests
import com.karhoo.uisdk.booking.braintree.BraintreeBookingFlowTests
import com.karhoo.uisdk.booking.braintree.BraintreeBookingTests
import com.karhoo.uisdk.booking.GuestBookingTests
import com.karhoo.uisdk.booking.adyen.AdyenBookingFlowTests
import com.karhoo.uisdk.ridedetail.RideDetailTests
import com.karhoo.uisdk.rides.RidesFlowTests
import com.karhoo.uisdk.rides.RidesTests
import com.karhoo.uisdk.trip.adyenTrip.AdyenTripFlowTests
import com.karhoo.uisdk.trip.adyenTrip.AdyenTripTests
import com.karhoo.uisdk.trip.braintreeTrip.BraintreeTripFlowTests
import com.karhoo.uisdk.trip.braintreeTrip.BraintreeTripTests
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        AddressTests::class,
        AdyenBookingTests::class,
        AdyenBookingFlowTests::class,
        AdyenTripFlowTests::class,
        AdyenTripTests::class,
        BraintreeBookingFlowTests::class,
        BraintreeBookingTests::class,
        RideDetailTests::class,
        RidesFlowTests::class,
        RidesTests::class,
        BraintreeTripFlowTests::class,
        BraintreeTripTests::class,
        GuestBookingTests::class
                   )

class UISDKTestsSuite : TestSuite()