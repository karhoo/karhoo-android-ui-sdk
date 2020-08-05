package com.karhoo.karhootraveller.testsuites.individualSuites

import com.karhoo.karhootraveller.login.LoginLOCTest
import com.karhoo.karhootraveller.profile.user.ProfileLOCTest
import com.karhoo.karhootraveller.registration.RegistrationLOCTest
import com.karhoo.karhootraveller.splash.SplashLOCTest
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        LoginLOCTest::class,
        SplashLOCTest::class,
        ProfileLOCTest::class,
        RegistrationLOCTest::class
)
class AppLossOfConnectionSuite : TestSuite()