package com.karhoo.karhootraveller.testsuites

import com.karhoo.karhootraveller.testsuites.individualSuites.AppLossOfConnectionSuite
import com.karhoo.uisdk.testsuites.UISDKLossOfConnectionSuite
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        AppLossOfConnectionSuite::class,
        UISDKLossOfConnectionSuite::class
                   )
class CombinedLOCTestSuites : TestSuite()