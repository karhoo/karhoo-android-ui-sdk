package com.karhoo.uisdk.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.notification.rides.past.RideNotificationContract

class KarhooNotificationContract(val rideNotificationView: RideNotificationContract.View) : RideNotificationContract {

    override fun initWith(context: Context) {
        createNotificationChannel(context)
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT != Build.VERSION_CODES.N) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(TRIP_INTO_CHANNEL, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun notifyRideEnded(context: Context, tripInfo: TripInfo) {
        rideNotificationView.let { view ->
            view.init(context, tripInfo)
            val builder = NotificationCompat.Builder(context, view.channelId())
                    .setSmallIcon(view.smallIcon())
                    .setCustomContentView(view.normalContentView())
                    .setCustomBigContentView(view.extendedContentView())
                    .setPriority(view.priority())
                    .setContentIntent(view.notificationIntent())
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(view.notificationId(), builder.build())
            }
        }
    }

    companion object {
        const val TRIP_INTO_CHANNEL = "TRIP_INFO_CHANNEL"
        const val TRIP_INTO_ID = 1001
    }
}
