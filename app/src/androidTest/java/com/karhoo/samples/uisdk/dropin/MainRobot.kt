package com.karhoo.samples.uisdk.dropin

import com.karhoo.samples.uisdk.dropin.common.BaseTestRobot
import com.karhoo.samples.uisdk.dropin.common.Launch

fun main(func: MainRobot.() -> Unit) = MainRobot().apply {
    func()
}

fun main(launch: Launch, func: MainRobot.() -> Unit) = MainRobot().apply {
    launch.launch()
    func()
}

class MainRobot : BaseTestRobot() {

    fun userClicksOnBookATripGuest() {
        clickButton(R.id.bookTripButtonGuest)
    }

    fun userClicksOnBookATripToken() {
        clickButton(R.id.bookTripButtonTokenExchange)
    }

}