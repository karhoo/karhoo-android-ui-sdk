package com.karhoo.karhootraveller.testsuites

import com.karhoo.karhootraveller.testsuites.individualSuites.AppTestsSuite
import com.karhoo.uisdk.testsuites.UISDKTestsSuiteNoLOCTests
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        AppTestsSuite::class,
        UISDKTestsSuiteNoLOCTests::class
                   )
class CombinedTestSuites : TestSuite()