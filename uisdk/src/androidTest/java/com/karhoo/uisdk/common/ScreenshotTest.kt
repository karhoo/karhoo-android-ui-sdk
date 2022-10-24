package com.karhoo.uisdk.common

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import com.karhoo.uisdk.base.address.AddressType
import com.karumi.shot.ActivityScenarioUtils.waitForActivity
import com.karumi.shot.ScreenshotTest
import org.junit.After
import org.junit.Before


abstract class ScreenshotTest<T : Activity>(private val clazz: Class<T>) :
    ScreenshotTest {
    private var scenario: ActivityScenario<T>? = null

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        scenario?.close()
        Intents.release()
    }

    fun startActivity(args: Bundle = Bundle()): T {
        val intent = Intent(ApplicationProvider.getApplicationContext(), clazz)
        intent.putExtras(args)
        intent.putExtra("address::type", AddressType.PICKUP)
        scenario = ActivityScenario.launch(intent)
        return scenario!!.waitForActivity()
    }
}