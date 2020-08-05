package com.karhoo.uisdk.notification.rides.past

import android.app.PendingIntent
import android.content.Context
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import com.karhoo.sdk.api.model.TripInfo

interface RideNotificationContract {
    fun initWith(context: Context)

    fun notifyRideEnded(context: Context, tripInfo: TripInfo)

    interface View {
        fun init(context: Context, tripInfo: TripInfo)

        fun normalContentView(): RemoteViews

        fun extendedContentView(): RemoteViews

        fun notificationIntent(): PendingIntent

        @DrawableRes
        fun smallIcon(): Int

        fun priority(): Int

        fun channelId(): String

        fun notificationId(): Int
    }
}