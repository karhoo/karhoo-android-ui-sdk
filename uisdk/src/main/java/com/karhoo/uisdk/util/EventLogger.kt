package com.karhoo.uisdk.util

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.LoggingEvent
import com.karhoo.sdk.api.network.request.LoggingRequest
import java.util.*
import kotlin.collections.ArrayList

object EventLogger {
    private const val MAX_NUMBER_OF_EVENTS = 10
    private const val TIMER_SECONDS_BETWEEN_FLUSHES = 60L
    private const val TAG = "EventLogger"
    private var timer: Timer? = null
    val eventsList: ArrayList<LoggingEvent> = arrayListOf()

    fun addLog(event: LoggingEvent) {
        eventsList.add(event)

        if (eventsList.size > MAX_NUMBER_OF_EVENTS) {
            flushLogs()
        }
    }

    fun flushLogs() {
        try {
            KarhooApi.loggingService.sendLogs(LoggingRequest(eventsList)).execute {
                eventsList.clear()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, e.message ?: "Error when flushing logs")
        }
    }

    fun startTimer() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                flushLogs()
            }
        }, TIMER_SECONDS_BETWEEN_FLUSHES)
    }

    fun stopTimer() {
        timer?.cancel()
        timer?.purge()
        timer = null
    }

}
