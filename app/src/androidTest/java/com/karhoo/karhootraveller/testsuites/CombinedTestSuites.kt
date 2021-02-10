package com.karhoo.karhootraveller.testsuites

import com.karhoo.karhootraveller.testsuites.individualSuites.AppTestsSuite
import com.karhoo.uisdk.testsuites.UISDKTestsSuite
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        AppTestsSuite::class,
        UISDKTestsSuite::class
                   )
class CombinedTestSuites : TestSuite()