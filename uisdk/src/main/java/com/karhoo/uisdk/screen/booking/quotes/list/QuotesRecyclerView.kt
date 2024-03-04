package com.karhoo.uisdk.screen.booking.quotes.list

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.checkout.quotes.BookingQuotesViewContract
import com.karhoo.uisdk.screen.booking.checkout.quotes.BookingQuotesViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import com.karhoo.uisdk.screen.booking.domain.support.KarhooFeedbackEmailComposer
import com.karhoo.uisdk.screen.booking.quotes.errorview.ErrorViewGenericReason
import com.karhoo.uisdk.screen.booking.quotes.errorview.QuotesErrorView
import com.karhoo.uisdk.screen.booking.quotes.errorview.QuotesErrorViewContract

class QuotesRecyclerView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attr, defStyleAttr), QuotesRecyclerContract.View {

    private val quotesAdapter =
        QuotesAdapter(context)

    private var bookingQuotesViewModel: BookingQuotesViewModel? = null

    private lateinit var quotesListRecycler: androidx.recyclerview.widget.RecyclerView
    private lateinit var quotesErrorView: QuotesErrorView
    private lateinit var quotesLoadingProgressBar: ProgressBar
    private lateinit var quotesLoadingLabel: TextView

    init {
        inflate(context, R.layout.uisdk_view_quotes_recycler, this)

        quotesListRecycler = findViewById(R.id.quotesListRecycler)
        quotesErrorView = findViewById(R.id.quotesErrorView)
        quotesLoadingProgressBar = findViewById(R.id.quotesLoadingProgressBar)
        quotesLoadingLabel = findViewById(R.id.quotesLoadingLabel)

        if (!isInEditMode) {
            quotesAdapter.setItemClickListener { _, _, item ->
                bookingQuotesViewModel?.process(
                    BookingQuotesViewContract.BookingQuotesEvent.QuotesItemClicked(
                        item
                    )
                )
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

    @Suppress("NestedBlockDepth")
    override fun updateList(quoteList: List<Quote>, refreshAll: Boolean) {
        if(refreshAll) {
            quotesAdapter.items = quoteList
        } else {
            var hasNewQuotes = quoteList.size != quotesAdapter.itemCount
            val newQuoteIds: ArrayList<String?> = arrayListOf()

            quoteList.forEach { newQuote ->
                if (quotesAdapter.items.find { it.id == newQuote.id } == null) {
                    hasNewQuotes = true

                    newQuote.id?.let { newQuoteIds.add(it) }
                }
            }

            if (hasNewQuotes) {
                quotesAdapter.refreshItemsAndAnimateNewOnes(quoteList, newQuoteIds)
                quotesListRecycler.scrollToPosition(0)
            }
        }

        if (quotesAdapter.itemCount > 0) {
            setQuotesLoaderVisibility(View.GONE)
        }
    }

    override fun setQuotesLoaderVisibility(visible: Int) {
        if (quotesErrorView.visibility == View.VISIBLE) {
            quotesLoadingProgressBar.visibility = View.GONE
            quotesLoadingLabel.visibility = View.GONE
        } else {
            quotesLoadingProgressBar.visibility = visible
            quotesLoadingLabel.visibility = visible
        }
    }

    override fun prebook(isPrebook: Boolean) {
        quotesListRecycler.adapter = null;
        quotesListRecycler.adapter = quotesAdapter;
        quotesAdapter.prebook(isPrebook)
    }

    override fun setListVisibility(visible: Boolean) {
        if (visible) {
            quotesListRecycler.visibility = View.VISIBLE
            quotesErrorView.visibility = View.GONE
        } else {
            quotesListRecycler.visibility = View.GONE
        }
        setQuotesLoaderVisibility(if (visible && quotesAdapter.itemCount <= 0) View.VISIBLE else View.GONE)
    }

    private fun showErrorView(show: Boolean, reason: ErrorViewGenericReason) {
        if (show) {
            quotesErrorView.setup(
                reason,
                object : QuotesErrorViewContract.QuotesErrorViewDelegate {
                    override fun onClicked() {
                        //do nothing
                    }

                    override fun onSubtitleClicked() {
                        //do nothing
                    }
                })
        }

        setListVisibility(!show)
        setQuotesLoaderVisibility(if (show) View.GONE else View.VISIBLE)
        quotesErrorView.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showFilterErrorView(show: Boolean, reason: ErrorViewGenericReason) {
        if (show) {
            quotesErrorView.setup(
                reason,
                object : QuotesErrorViewContract.QuotesErrorViewDelegate {
                    override fun onClicked() {
                        //do nothing
                    }

                    override fun onSubtitleClicked() {
                        //do nothing
                    }
                })
        }

        if (!show) {
            quotesListRecycler.visibility = View.VISIBLE
        } else {
            setQuotesLoaderVisibility(View.GONE)
            quotesListRecycler.visibility = View.GONE
        }
        quotesErrorView.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showSameAddressesError(show: Boolean) {
        showErrorView(
            show, ErrorViewGenericReason(
                context.resources.getString(R.string.kh_uisdk_quotes_error_similar_addresses_title),
                context.resources.getString(R.string.kh_uisdk_quotes_error_similar_addresses_subtitle),
                R.drawable.kh_uisdk_similar_pickup_dropoff
            )
        )
    }

    override fun showNoAddressesError(show: Boolean) {
        showErrorView(
            show, ErrorViewGenericReason(
                context.resources.getString(R.string.kh_uisdk_quotes_error_missing_addresses_title),
                context.resources.getString(R.string.kh_uisdk_quotes_error_missing_addresses_subtitle),
                R.drawable.kh_uisdk_similar_pickup_dropoff
            )
        )
    }

    override fun showNoResultsAfterFilterError(show: Boolean) {
        showFilterErrorView(
            show, ErrorViewGenericReason(
                context.resources.getString(R.string.kh_uisdk_quotes_error_no_results_after_filter_title),
                context.resources.getString(R.string.kh_uisdk_quotes_error_no_results_after_filter_subtitle),
                R.drawable.kh_uisdk_ic_filter_no_result
            )
        )
    }

    override fun showNoFleetsError(show: Boolean, isPrebook: Boolean) {
        showErrorView(
            show, ErrorViewGenericReason(
                if (isPrebook) context.resources.getString(R.string.kh_uisdk_quotes_no_availability_title) else "",
                if (isPrebook) context.resources.getString(R.string.kh_uisdk_quotes_no_availability_subtitle) else context.resources.getString(
                    R.string.kh_uisdk_quotes_error_no_results_found
                ),
                R.drawable.kh_uisdk_ic_no_available_quotes
            )
        )
    }

    override fun showNoCoverageError(show: Boolean) {
        if (show) {
            quotesErrorView.setupWithSpan(
                ErrorViewGenericReason(
                    context.resources.getString(R.string.kh_uisdk_quotes_error_no_coverage_title),
                    String.format(
                        context.resources.getString(R.string.kh_uisdk_quotes_error_no_coverage_subtitle),
                        context.resources.getString(R.string.kh_uisdk_contact_us)
                    ),
                    R.drawable.kh_uisdk_ic_no_available_quotes
                ),
                object : QuotesErrorViewContract.QuotesErrorViewDelegate {
                    override fun onClicked() {
                        // Do nothing
                    }

                    override fun onSubtitleClicked() {
                        val emailComposer = KarhooFeedbackEmailComposer(context)
                        val noCoverageFeedbackIntent = emailComposer.showNoCoverageEmail()
                        noCoverageFeedbackIntent?.let { intent ->
                            context.startActivity(intent)
                        }
                    }
                })
        }

        setListVisibility(!show)
        setQuotesLoaderVisibility(if (show) View.GONE else View.VISIBLE)
        quotesErrorView.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun watchQuoteListStatus(
        lifecycleOwner: LifecycleOwner, bookingQuotesViewModel:
        BookingQuotesViewModel
    ) {
        this.bookingQuotesViewModel = bookingQuotesViewModel
        bookingQuotesViewModel.viewStates().observe(lifecycleOwner, Observer { quoteListStatus ->
            quoteListStatus?.let {
                it.selectedQuote
            }
        })
    }

}
