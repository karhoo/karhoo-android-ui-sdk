package com.karhoo.uisdk.screen.address.recents

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.address.adapter.RecentAddressAdapter
import kotlinx.android.synthetic.main.uisdk_view_simple_recycler.view.emptyText
import kotlinx.android.synthetic.main.uisdk_view_simple_recycler.view.recycler

class RecentsListView @JvmOverloads constructor(context: Context,
                                                attrs: AttributeSet? = null,
                                                defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), RecentsMVP.View {

    private var adapter: RecentAddressAdapter = RecentAddressAdapter(context)
    private var presenter: RecentsMVP.Presenter = RecentsPresenter(this, SharedPreferencesLocationStore(context))

    var actions: Actions? = null

    init {
        inflate(context, R.layout.uisdk_view_simple_recycler, this)

        if (!isInEditMode) {
            emptyText.setText(R.string.recents_empty)

            adapter.setItemClickListener { _, position, locationInfo -> saveLocationAndNotifyActions(locationInfo, position) }

            recycler.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                setHasFixedSize(true)
                adapter = this@RecentsListView.adapter
            }

            presenter.loadLocations()
        }
    }

    override fun showLocations(locations: List<LocationInfo>) {
        emptyText.visibility = View.GONE
        recycler.visibility = View.VISIBLE
        adapter.items = locations
    }

    override fun showEmptyState() {
        emptyText.visibility = View.VISIBLE
        recycler.visibility = View.INVISIBLE
    }

    private fun saveLocationAndNotifyActions(locationDetails: LocationInfo, addressPositionInList: Int) {
        saveLocationToRecents(locationDetails)
        actions?.recentAddressSelected(locationDetails, addressPositionInList)
    }

    fun saveLocationToRecents(location: LocationInfo) = presenter.save(location)

    interface Actions {

        fun recentAddressSelected(location: LocationInfo, addressPositionInList: Int)

    }

}
