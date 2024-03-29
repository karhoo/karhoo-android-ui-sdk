package com.karhoo.uisdk.screen.rides.past

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.AttrRes
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

class PastRidesView @JvmOverloads constructor(context: Context,
                                              attrs: AttributeSet? = null,
                                              @AttrRes defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), PastRidesMVP.View,
      LayoutArrayPagerAdapter.Refreshable {

    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var errorStateWidget: ErrorStateView
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var presenter: PastRidesMVP.Presenter = PastRidesPresenter(this,
                                                                       KarhooApi.tripService)

    private val pastRidesAdapter: PastRidesAdapter = PastRidesAdapter()
    private var ridesLoading: RidesLoading? = null

    init {
        View.inflate(context, R.layout.uisdk_view_past_rides, this)

        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        errorStateWidget = findViewById(R.id.errorStateWidget)
        recyclerView = findViewById(R.id.recyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener { presenter.getPastRides() }
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(context, R.color.kh_uisdk_primary))
        errorStateWidget.setRetryButtonClickListener(OnClickListener { refresh() })

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(getContext())
            adapter = pastRidesAdapter
        }

        if (!isInEditMode) {
            presenter.getPastRides()
        }
    }

    override fun showEmptyState() {
        ridesLoading?.hideLoading()
        swipeRefreshLayout.isRefreshing = false
        recyclerView.visibility = View.INVISIBLE
        errorStateWidget.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
    }

    override fun showPastRides(pastRides: List<TripInfo>) {
        ridesLoading?.hideLoading()
        swipeRefreshLayout.isRefreshing = false
        emptyStateLayout.visibility = View.INVISIBLE
        errorStateWidget.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        pastRidesAdapter.items = pastRides
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
        presenter.getPastRides()
    }

    override fun loader(ridesLoading: RidesLoading) {
        this.ridesLoading = ridesLoading
    }

}
