package com.karhoo.karhootraveller.testsuites.individualSuites

import com.karhoo.karhootraveller.login.LoginTests
import com.karhoo.karhootraveller.menu.MenuTests
import com.karhoo.karhootraveller.profile.user.UserProfileTests
import com.karhoo.karhootraveller.registration.RegistrationTests
import com.karhoo.karhootraveller.splash.SplashTests
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        LoginTests::class,
        MenuTests::class,
        UserProfileTests::class,
        RegistrationTests::class,
        SplashTests::class
)
class AppTestsSuite : TestSuite()