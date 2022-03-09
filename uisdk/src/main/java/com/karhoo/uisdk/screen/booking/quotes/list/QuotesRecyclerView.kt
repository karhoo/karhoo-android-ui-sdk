package com.karhoo.uisdk.screen.booking.quotes.list

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.checkout.quotes.BookingQuotesViewContract
import com.karhoo.uisdk.screen.booking.checkout.quotes.BookingQuotesViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import com.karhoo.uisdk.screen.booking.quotes.category.CategoriesViewModel
import kotlinx.android.synthetic.main.uisdk_view_quotes_recycler.view.quotesErrorSubtitle
import kotlinx.android.synthetic.main.uisdk_view_quotes_recycler.view.quotesErrorTitle
import kotlinx.android.synthetic.main.uisdk_view_quotes_recycler.view.quotesListRecycler
import kotlinx.android.synthetic.main.uisdk_view_quotes_recycler.view.quotesLoadingLabel
import kotlinx.android.synthetic.main.uisdk_view_quotes_recycler.view.quotesLoadingProgressBar

class QuotesRecyclerView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attr, defStyleAttr), QuotesRecyclerMVP.View {

    private val quotesAdapter =
        QuotesAdapter(context)
    private val presenter: QuotesRecyclerMVP.Presenter = QuotesRecyclerPresenter(this)

    private var bookingQuotesViewModel: BookingQuotesViewModel? = null

    init {
        inflate(context, R.layout.uisdk_view_quotes_recycler, this)

        if (!isInEditMode) {
            quotesAdapter.setItemClickListener { _, _, item ->
                bookingQuotesViewModel?.process(BookingQuotesViewContract.BookingQuotesEvent.QuotesItemClicked(item))
            }
            quotesListRecycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                itemAnimator = DefaultItemAnimator()
                adapter = quotesAdapter
            }
        }
    }

    override fun setSortMethod(sortMethod: SortMethod) {
        quotesAdapter.setSelectedSortMethod(sortMethod)
    }

    override fun updateList(quoteList: List<Quote>) {
        quotesAdapter.items = quoteList
        if (quotesAdapter.itemCount > 0) {
            setQuotesLoaderVisibility(View.GONE)
        }
    }

    override fun setQuotesLoaderVisibility(visible: Int) {
        if (quotesErrorTitle.visibility == View.VISIBLE) {
            quotesLoadingProgressBar.visibility = View.GONE
            quotesLoadingLabel.visibility = View.GONE
        } else {
            quotesLoadingProgressBar.visibility = visible
            quotesLoadingLabel.visibility = visible
        }
    }

    override fun prebook(isPrebook: Boolean) {
        quotesAdapter.prebook(isPrebook)
    }

    override fun setListVisibility(visible: Boolean) {
        if (visible) {
            quotesListRecycler.visibility = View.VISIBLE
            quotesErrorTitle.visibility = View.GONE
            quotesErrorSubtitle?.visibility = View.GONE
        } else {
            quotesListRecycler.visibility = View.GONE

            quotesErrorTitle.visibility = View.VISIBLE
            quotesErrorTitle?.text = context.resources.getString(R.string.kh_uisdk_no_availability)

            quotesErrorSubtitle?.text = context.resources.getString(R.string.kh_uisdk_enter_destination_for_vehicles)
            quotesErrorSubtitle?.visibility = View.VISIBLE

        }
        setQuotesLoaderVisibility(if (visible) View.VISIBLE else View.GONE)
    }

    override fun showNoResultsText(show: Boolean) {
        if (show) {
            quotesErrorSubtitle?.text = context.resources.getString(R.string.kh_uisdk_no_results_found)
            quotesErrorTitle?.text = context.resources.getString(R.string.kh_uisdk_no_results_label)

            quotesErrorSubtitle?.visibility = View.VISIBLE
            quotesErrorTitle?.visibility = View.VISIBLE
        } else {
            quotesErrorSubtitle?.visibility = View.GONE
            quotesErrorTitle?.visibility = View.GONE
        }

        setQuotesLoaderVisibility(if (show) View.GONE else View.VISIBLE)
    }

    override fun watchCategories(lifecycleOwner: LifecycleOwner, categoriesViewModel: CategoriesViewModel) {
        categoriesViewModel.categories.observe(lifecycleOwner, presenter.watchCategories())
    }

    override fun watchQuoteListStatus(lifecycleOwner: LifecycleOwner, bookingQuotesViewModel:
    BookingQuotesViewModel) {
        this.bookingQuotesViewModel = bookingQuotesViewModel
        bookingQuotesViewModel.viewStates().observe(lifecycleOwner, Observer { quoteListStatus ->
            quoteListStatus?.let {
                it.selectedQuote
            }
        })
    }

}
