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
import com.karhoo.uisdk.screen.booking.domain.support.KarhooFeedbackEmailComposer
import com.karhoo.uisdk.screen.booking.quotes.category.CategoriesViewModel
import com.karhoo.uisdk.screen.booking.quotes.errorview.ErrorViewGenericReason
import com.karhoo.uisdk.screen.booking.quotes.errorview.ErrorViewLinkedReason
import com.karhoo.uisdk.screen.booking.quotes.errorview.QuotesErrorViewContract
import kotlinx.android.synthetic.main.uisdk_view_quotes_recycler.view.*

class QuotesRecyclerView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attr, defStyleAttr), QuotesRecyclerContract.View {

    private val quotesAdapter =
        QuotesAdapter(context)
    private val presenter: QuotesRecyclerContract.Presenter = QuotesRecyclerPresenter(this)

    private var bookingQuotesViewModel: BookingQuotesViewModel? = null

    init {
        inflate(context, R.layout.uisdk_view_quotes_recycler, this)

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

    override fun updateList(quoteList: List<Quote>) {
        quotesAdapter.items = quoteList
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
        quotesAdapter.prebook(isPrebook)
    }

    override fun setListVisibility(visible: Boolean) {
        if (visible) {
            quotesListRecycler.visibility = View.VISIBLE
            quotesErrorView.visibility = View.GONE
        } else {
            quotesListRecycler.visibility = View.GONE
        }
        setQuotesLoaderVisibility(if (visible) View.VISIBLE else View.GONE)
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

    override fun showSameAddressesError(show: Boolean) {
        showErrorView(show, ErrorViewGenericReason(
            context.resources.getString(R.string.kh_uisdk_quotes_error_similar_addresses_title),
            context.resources.getString(R.string.kh_uisdk_quotes_error_similar_addresses_subtitle),
            R.drawable.kh_uisdk_similar_pickup_dropoff
        ))
    }

    override fun showNoAddressesError(show: Boolean) {
        showErrorView(show, ErrorViewGenericReason(
            context.resources.getString(R.string.kh_uisdk_quotes_error_missing_addresses_title),
            context.resources.getString(R.string.kh_uisdk_quotes_error_missing_addresses_subtitle),
            R.drawable.kh_uisdk_similar_pickup_dropoff
        ))
    }

    override fun showNoFleetsError(show: Boolean) {
        showErrorView(show, ErrorViewGenericReason(
            context.resources.getString(R.string.kh_uisdk_quotes_error_no_availability_title),
            context.resources.getString(R.string.kh_uisdk_quotes_error_no_availability_subtitle),
            R.drawable.kh_uisdk_ic_no_available_quotes
        ))
    }

    override fun showNoCoverageError(show: Boolean) {
        //TODO will be changed when the proper error is implemented
        if (show) {
            quotesErrorView.visibility = View.VISIBLE
            quotesErrorView.setup(
                ErrorViewLinkedReason(
                    context.resources.getString(R.string.kh_uisdk_no_coverage_title),
                    context.resources.getString(R.string.kh_uisdk_no_coverage_subtitle),
                        context.resources.getString(R.string.kh_uisdk_contact_us),
                                context.resources.getString(R.string.kh_uisdk_contact_us),
                    R.drawable.kh_uisdk_ic_no_coverage_quotes
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
        } else {
            quotesErrorView.visibility = View.GONE
        }
    }

    override fun watchCategories(
        lifecycleOwner: LifecycleOwner,
        categoriesViewModel: CategoriesViewModel
    ) {
        categoriesViewModel.categories.observe(lifecycleOwner, presenter.watchCategories())
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
