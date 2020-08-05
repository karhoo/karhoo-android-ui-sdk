package com.karhoo.uisdk.testsuites

import com.karhoo.uisdk.address.AddressLOCTest
import com.karhoo.uisdk.booking.BookingLOCTest
import com.karhoo.uisdk.ridedetail.RideDetailLOCTest
import com.karhoo.uisdk.rides.RidesLOCTest
import com.karhoo.uisdk.trip.TripLOCTest
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        AddressLOCTest::class,
        BookingLOCTest::class,
        RidesLOCTest::class,
        RideDetailLOCTest::class,
        TripLOCTest::class)
class UISDKLossOfConnectionSuite : TestSuite()