package com.karhoo.uisdk.screen.address.recents

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.address.adapter.RecentAddressAdapter

class RecentsListView @JvmOverloads constructor(context: Context,
                                                attrs: AttributeSet? = null,
                                                defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), RecentsMVP.View {

    private var adapter: RecentAddressAdapter = RecentAddressAdapter(context)
    private var presenter: RecentsMVP.Presenter = RecentsPresenter(this, SharedPreferencesLocationStore(context))

    var actions: Actions? = null

    private lateinit var emptyText: TextView
    private lateinit var recycler: RecyclerView

    init {
        inflate(context, R.layout.uisdk_view_simple_recycler, this)

        emptyText = findViewById(R.id.emptyText)
        recycler = findViewById(R.id.recycler)

        if (!isInEditMode) {
            emptyText.setText(R.string.kh_uisdk_recents_empty)

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
