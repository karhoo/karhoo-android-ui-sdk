package com.karhoo.uisdk.notification.rides.past

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.uisdk.R
import com.karhoo.uisdk.notification.KarhooNotificationContract
import com.karhoo.uisdk.screen.rides.detail.RideDetailActivity
import com.karhoo.uisdk.util.extension.categoryToLocalisedString
import com.karhoo.uisdk.util.extension.toLocalisedString
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.lang.Exception

class RideNotificationView : RideNotificationContract.View {
    lateinit var context: Context
    lateinit var trip: TripInfo

    override fun init(context: Context, tripInfo: TripInfo) {
        this.context = context
        this.trip = tripInfo
    }

    override fun normalContentView(): RemoteViews {
        val contentView = RemoteViews(context.packageName, R.layout.uisdk_view_past_ride_notification)

        contentView.setTextViewText(R.id.khTermsAndConditionsText, trip.fleetInfo?.name)

        bindState(contentView)
        loadFleetLogo(contentView)

        return contentView
    }

    override fun extendedContentView(): RemoteViews {
        val contentView = RemoteViews(context.packageName, R.layout.uisdk_view_past_ride_notification_big)

        contentView.apply {
            setImageViewResource(R.id.pickupBallIcon, R.drawable.uisdk_ic_pickup)
            setImageViewResource(R.id.dropoffBallIcon, R.drawable.uisdk_ic_destination)
            setTextViewText(R.id.khTermsAndConditionsText, trip.fleetInfo?.name)
            setTextViewText(R.id.pickupLabel, trip.origin?.displayAddress)
            setTextViewText(R.id.dropOffLabel, trip.destination?.displayAddress)

            handleCarVisibility(this)
            bindPickupType(this, trip.meetingPoint?.pickupType)
            bindState(this)
            loadFleetLogo(this)
        }

        return contentView
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun notificationIntent(): PendingIntent {
        val intent = RideDetailActivity.Builder.newBuilder().trip(trip).build(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(context, 0, intent, 0)
        }
    }

    @DrawableRes
    override fun smallIcon() = R.drawable.uisdk_karhoo_wordmark_small

    override fun priority() = NotificationCompat.PRIORITY_DEFAULT

    override fun channelId() = KarhooNotificationContract.TRIP_INTO_CHANNEL

    override fun notificationId(): Int {
        return KarhooNotificationContract.TRIP_INTO_ID
    }

    private fun loadFleetLogo(contentView: RemoteViews) {
        if (trip.fleetInfo?.logoUrl.isNullOrBlank()) {
            contentView.setImageViewResource(R.id.logoImage, R.drawable.uisdk_ic_quotes_logo_empty)
        } else {
            Picasso.get().load(trip.fleetInfo?.logoUrl).into(object : Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    // Do nothing
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    contentView.setImageViewResource(R.id.logoImage, R.drawable.uisdk_ic_quotes_logo_empty)
                }

                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    contentView.setImageViewBitmap(R.id.logoImage, bitmap)
                }
            })
        }
    }

    fun bindState(contentView: RemoteViews) {
        when (trip.tripState) {
            TripStatus.COMPLETED ->
                displayState(contentView, R.drawable.uisdk_ic_trip_completed, R.string.kh_uisdk_ride_state_completed, R.color.kh_uisdk_text)
            TripStatus.CANCELLED_BY_USER,
            TripStatus.CANCELLED_BY_DISPATCH,
            TripStatus.NO_DRIVERS,
            TripStatus.CANCELLED_BY_KARHOO ->
                displayState(contentView, R.drawable.uisdk_ic_trip_cancelled, R.string.kh_uisdk_ride_state_cancelled, R.color.kh_uisdk_text)

            else -> {}
        }
    }

    private fun displayState(contentView: RemoteViews, @DrawableRes icon: Int, @StringRes state: Int, @ColorRes color: Int) {
        contentView.setImageViewResource(R.id.stateIcon, icon)
        contentView.setTextColor(R.id.stateText, ContextCompat.getColor(context, color))
        contentView.setTextViewText(R.id.stateText, context.getString(state))
    }

    private fun handleCarVisibility(contentView: RemoteViews) =
            if (trip.vehicle?.vehicleLicencePlate.isNullOrBlank()) {
                contentView.setViewVisibility(R.id.carText, View.GONE)
            } else {
                contentView.setViewVisibility(R.id.carText, View.VISIBLE)
                contentView.setTextViewText(R.id.carText, "${trip.vehicle?.categoryToLocalisedString(this.context)}${trip.vehicle?.vehicleLicencePlate}")
            }

    private fun bindPickupType(contentView: RemoteViews, pickupType: PickupType?) {
        when (pickupType) {
            PickupType.DEFAULT,
            PickupType.NOT_SET -> {
                contentView.setViewVisibility(R.id.pickupTypeText, View.GONE)
            }
            else -> {
                contentView.setViewVisibility(R.id.pickupTypeText, View.VISIBLE)
                contentView.setTextViewText(R.id.pickupTypeText, pickupType?.toLocalisedString(context.applicationContext))
            }
        }
    }
}
