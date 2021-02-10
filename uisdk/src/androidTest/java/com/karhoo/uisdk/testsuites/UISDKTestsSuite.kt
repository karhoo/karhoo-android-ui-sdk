package com.karhoo.uisdk.testsuites

import com.karhoo.uisdk.address.AddressTests
import com.karhoo.uisdk.booking.GuestBookingTests
import com.karhoo.uisdk.booking.adyen.AdyenBookingFlowTests
import com.karhoo.uisdk.booking.adyen.AdyenBookingTests
import com.karhoo.uisdk.booking.braintree.BraintreeBookingFlowTests
import com.karhoo.uisdk.booking.braintree.BraintreeBookingTests
import com.karhoo.uisdk.ridedetail.RideDetailTests
import com.karhoo.uisdk.rides.RidesFlowTests
import com.karhoo.uisdk.rides.RidesTests
import com.karhoo.uisdk.trip.TripFlowTests
import com.karhoo.uisdk.trip.TripTests
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        AddressTests::class,
        AdyenBookingTests::class,
        AdyenBookingFlowTests::class,
        BraintreeBookingFlowTests::class,
        BraintreeBookingTests::class,
        RideDetailTests::class,
        RidesFlowTests::class,
        RidesTests::class,
        TripFlowTests::class,
        TripTests::class,
        GuestBookingTests::class
                   )

class UISDKTestsSuite : TestSuite()