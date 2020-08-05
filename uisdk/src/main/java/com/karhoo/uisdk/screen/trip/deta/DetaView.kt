package com.karhoo.uisdk.screen.trip.deta

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.DateUtil
import kotlinx.android.synthetic.main.uisdk_view_deta.view.detaText
import org.joda.time.DateTime
import java.util.Date
import java.util.TimeZone

class DetaView @JvmOverloads constructor(context: Context,
                                         attrs: AttributeSet? = null,
                                         defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), DetaMVP.View, LifecycleObserver {

    private val presenter = DetaPresenter(this, KarhooApi.driverTrackingService,
                                          KarhooApi.tripService, KarhooUISDK.analytics)

    companion object {
        private const val MILLISECONDS_IN_ONE_MINUTE = 60L * 1000L
    }

    init {
        inflate(context, R.layout.uisdk_view_deta, this)
    }

    fun monitorDeta(tripIdentifier: String, timeZone: String) {
        presenter.monitorDeta(tripIdentifier, timeZone)
    }

    override fun showDeta(deta: Int, offsetMilliseconds: Int) {
        visibility = View.VISIBLE
        val now = with(Date()) {
            val deviceOffset = TimeZone.getDefault().getOffset(time)
            time + (offsetMilliseconds - deviceOffset)
        }
        val millisecondsTimeOfArrival = now + (deta * MILLISECONDS_IN_ONE_MINUTE)
        val dateOfArrival = Date(millisecondsTimeOfArrival)
        detaText.text = DateUtil.getTimeFormat(context, DateTime(dateOfArrival.time))
    }

    override fun hideDeta() {
        visibility = View.GONE
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        presenter.onStop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onPause() {
        presenter.onDestroy()
    }

}