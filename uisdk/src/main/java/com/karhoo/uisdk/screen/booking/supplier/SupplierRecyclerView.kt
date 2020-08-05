package com.karhoo.uisdk.screen.booking.supplier

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.karhoo.sdk.api.model.QuoteV2
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.booking.supplier.BookingSupplierViewContract
import com.karhoo.uisdk.screen.booking.booking.supplier.BookingSupplierViewModel
import com.karhoo.uisdk.screen.booking.domain.supplier.SortMethod
import com.karhoo.uisdk.screen.booking.supplier.category.CategoriesViewModel
import kotlinx.android.synthetic.main.uisdk_view_supplier_recycler.view.noDestinationLabel
import kotlinx.android.synthetic.main.uisdk_view_supplier_recycler.view.noDestinationVehiclesLabel
import kotlinx.android.synthetic.main.uisdk_view_supplier_recycler.view.quotesLoadingLabel
import kotlinx.android.synthetic.main.uisdk_view_supplier_recycler.view.quotesLoadingProgressBar
import kotlinx.android.synthetic.main.uisdk_view_supplier_recycler.view.supplierListRecycler

class SupplierRecyclerView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attr, defStyleAttr), SupplierRecyclerMVP.View {

    private val suppliersAdapter = SuppliersAdapter(context)
    private val presenter: SupplierRecyclerMVP.Presenter = SupplierRecyclerPresenter(this)

    private var bookingSupplierViewModel: BookingSupplierViewModel? = null

    init {
        inflate(context, R.layout.uisdk_view_supplier_recycler, this)

        if (!isInEditMode) {
            suppliersAdapter.setItemClickListener { _, _, item ->
                bookingSupplierViewModel?.process(BookingSupplierViewContract.BookingSupplierEvent.SupplierItemClicked(item))
            }
            supplierListRecycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                itemAnimator = DefaultItemAnimator()
                adapter = suppliersAdapter
            }
        }
    }

    override fun setSortMethod(sortMethod: SortMethod) {
        suppliersAdapter.setSelectedSortMethod(sortMethod)
    }

    override fun updateList(quoteList: List<QuoteV2>) {
        suppliersAdapter.items = quoteList
        if (suppliersAdapter.itemCount > 0) {
            setQuotesLoaderVisibility(View.GONE)
        }
    }

    override fun setQuotesLoaderVisibility(visible: Int) {
        if (noDestinationLabel.visibility == View.VISIBLE) {
            quotesLoadingProgressBar.visibility = View.GONE
            quotesLoadingLabel.visibility = View.GONE
        } else {
            quotesLoadingProgressBar.visibility = visible
            quotesLoadingLabel.visibility = visible
        }
    }

    override fun prebook(isPrebook: Boolean) {
        suppliersAdapter.prebook(isPrebook)
    }

    override fun setListVisibility(visible: Boolean) {
        supplierListRecycler.visibility = if (visible) View.VISIBLE else View.GONE
        noDestinationLabel.visibility = if (visible) View.GONE else View.VISIBLE
        noDestinationVehiclesLabel?.visibility = if (visible) View.GONE else View.VISIBLE
        setQuotesLoaderVisibility(if (visible) View.VISIBLE else View.GONE)
    }

    override fun watchCategories(lifecycleOwner: LifecycleOwner, categoriesViewModel: CategoriesViewModel) {
        categoriesViewModel.categories.observe(lifecycleOwner, presenter.watchCategories())
    }

    override fun watchQuoteListStatus(lifecycleOwner: LifecycleOwner, bookingSupplierViewModel:
    BookingSupplierViewModel) {
        this.bookingSupplierViewModel = bookingSupplierViewModel
        bookingSupplierViewModel.viewStates().observe(lifecycleOwner, Observer { quoteListStatus ->
            quoteListStatus?.let {
                it.selectedQuote
            }
        })
    }

}