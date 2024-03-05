package com.karhoo.uisdk.screen.trip.eta

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.ViewsConstants.ETA_ELEVATION

class EtaView @JvmOverloads constructor(context: Context,
                                        attrs: AttributeSet? = null,
                                        defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), EtaMVP.View, LifecycleObserver {

        private lateinit var etaLayout: FloatingActionButton
        private lateinit var minsText: TextView

    private val presenter = EtaPresenter(this, KarhooApi.driverTrackingService, KarhooApi.tripService)

    init {
        inflate(context, R.layout.uisdk_view_eta, this)

        etaLayout = findViewById(R.id.etaLayout)
        minsText = findViewById(R.id.minsText)

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
