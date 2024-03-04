package com.karhoo.uisdk.screen.rides.upcoming

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.view.ErrorStateView
import com.karhoo.uisdk.screen.rides.LayoutArrayPagerAdapter
import com.karhoo.uisdk.screen.rides.RidesLoading

class UpcomingRidesView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0)
    : FrameLayout(context, attrs, defStyle), UpcomingRidesMVP.View, LayoutArrayPagerAdapter.Refreshable {

        private lateinit var emptyStateLayout: LinearLayout
        private lateinit var errorStateWidget: ErrorStateView
        private lateinit var recyclerView: RecyclerView
        private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val presenter: UpcomingRidesMVP.Presenter = UpcomingRidesPresenter(this, KarhooApi.tripService)

    private val upcomingRidesAdapter: UpcomingRidesAdapter = UpcomingRidesAdapter()
    private var ridesLoading: RidesLoading? = null

    init {
        inflate(context, R.layout.uisdk_view_upcoming_rides, this)

        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        errorStateWidget = findViewById(R.id.errorStateWidget)
        recyclerView = findViewById(R.id.recyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        errorStateWidget.setRetryButtonClickListener(OnClickListener { refresh() })
        swipeRefreshLayout.setOnRefreshListener { presenter.getUpcomingRides() }
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(context, R.color.kh_uisdk_primary))

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(getContext())
            adapter = upcomingRidesAdapter
        }

        if (!isInEditMode) {
            presenter.getUpcomingRides()
        }
    }

    override fun showEmptyState() {
        ridesLoading?.hideLoading()
        swipeRefreshLayout.isRefreshing = false
        recyclerView.visibility = View.INVISIBLE
        emptyStateLayout.visibility = View.VISIBLE
        errorStateWidget.visibility = View.GONE
    }

    override fun showUpcomingRides(upcomingRides: List<TripInfo>) {
        ridesLoading?.hideLoading()
        swipeRefreshLayout.isRefreshing = false
        recyclerView.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.INVISIBLE
        errorStateWidget.visibility = View.GONE
        upcomingRidesAdapter.items = upcomingRides
    }

    override fun showError(@StringRes errorMessage: Int, karhooError: KarhooError?) {
        ridesLoading?.hideLoading()
        swipeRefreshLayout.isRefreshing = false
        recyclerView.visibility = View.INVISIBLE
        emptyStateLayout.visibility = View.INVISIBLE
        errorStateWidget.visibility = View.VISIBLE
        errorStateWidget.setErrorMessage(resources.getString(errorMessage), karhooError)
    }

    override fun refresh() {
        ridesLoading?.showLoading()
        presenter.getUpcomingRides()
    }

    override fun loader(ridesLoading: RidesLoading) {
        this.ridesLoading = ridesLoading
    }

}
