package com.karhoo.uisdk.screen.trip.eta

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.ViewsConstants.ETA_ELEVATION
import kotlinx.android.synthetic.main.uisdk_view_eta.view.etaLayout
import kotlinx.android.synthetic.main.uisdk_view_eta.view.minsText

class EtaView @JvmOverloads constructor(context: Context,
                                        attrs: AttributeSet? = null,
                                        defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), EtaMVP.View, LifecycleObserver {

    private val presenter = EtaPresenter(this, KarhooApi.driverTrackingService,
                                         KarhooApi.tripService, KarhooUISDK.analytics)

    init {
        inflate(context, R.layout.uisdk_view_eta, this)

        etaLayout.apply {
            isEnabled = false
            elevation = ETA_ELEVATION
        }
    }

    fun monitorEta(tripIdentifier: String) {
        presenter.monitorEta(tripIdentifier)
    }

    override fun showEta(eta: Int) {
        if (eta == 0) {
            hideEta()
        } else {
            visibility = VISIBLE
            minsText.text = eta.toString()
        }
    }

    override fun hideEta() {
        visibility = GONE
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
