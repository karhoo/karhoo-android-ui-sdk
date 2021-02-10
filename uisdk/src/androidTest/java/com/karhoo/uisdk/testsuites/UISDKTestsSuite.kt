package com.karhoo.uisdk.testsuites

import com.karhoo.uisdk.rides.RidesTests
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
//        AddressTests::class,
//        AdyenBookingTests::class,
//        AdyenBookingFlowTests::class,
//        BraintreeBookingFlowTests::class,
//        BraintreeBookingTests::class,
//        RideDetailTests::class,
//        RidesFlowTests::class,
        RidesTests::class
//        TripFlowTests::class,
//        TripTests::class,
//        GuestBookingTests::class
                   )

class UISDKTestsSuite : TestSuite()