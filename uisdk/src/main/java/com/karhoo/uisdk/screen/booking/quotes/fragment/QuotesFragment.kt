package com.karhoo.uisdk.screen.booking.quotes.fragment

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.address.AddressCodes
import com.karhoo.uisdk.base.snackbar.SnackbarAction
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.base.snackbar.SnackbarPriority
import com.karhoo.uisdk.base.snackbar.SnackbarType
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarView
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.checkout.quotes.BookingQuotesViewModel
import com.karhoo.uisdk.screen.booking.checkout.quotes.QuoteListStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingInfo
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityProvider
import com.karhoo.uisdk.screen.booking.domain.quotes.KarhooAvailability
import com.karhoo.uisdk.screen.booking.domain.quotes.LiveFleetsViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import com.karhoo.uisdk.screen.booking.domain.support.KarhooFeedbackEmailComposer
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_DROPOFF_ADDRESS
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_PICKUP_ADDRESS
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_RESULT_OK
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_SELECTED_DATE
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_SELECTED_QUOTE_KEY
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP
import com.karhoo.uisdk.screen.booking.quotes.category.CategoriesViewModel
import com.karhoo.uisdk.screen.booking.quotes.category.CategorySelectorView
import com.karhoo.uisdk.screen.booking.quotes.list.QuotesRecyclerView
import com.karhoo.uisdk.screen.booking.quotes.sortview.QuotesSortView
import kotlinx.android.synthetic.main.uisdk_view_quotes_list.*
import java.util.Locale

class QuotesFragment : Fragment(), QuotesSortView.Listener,
    QuotesFragmentContract.View, LifecycleObserver {

    private val bookingStatusStateViewModel: BookingStatusStateViewModel by lazy {
        ViewModelProvider(this).get(
            BookingStatusStateViewModel::class.java
        )
    }
    private val bookingQuotesViewModel: BookingQuotesViewModel by lazy {
        ViewModelProvider(this).get(
            BookingQuotesViewModel::class.java
        )
    }

    private var quoteListViewDelegate: QuotesFragmentContract.QuoteListDelegate? = null
    private var presenter = QuotesFragmentPresenter(this, KarhooUISDK.analytics)
    private var availabilityProvider: AvailabilityProvider? = null
    private var dataModel: QuoteListViewDataModel? = null
    private val categoriesViewModel: CategoriesViewModel = CategoriesViewModel()
    private val liveFleetsViewModel: LiveFleetsViewModel = LiveFleetsViewModel()
    private lateinit var quotesSortWidget: QuotesSortView
    private lateinit var addressBarWidget: AddressBarView
    private lateinit var categorySelectorWidget: CategorySelectorView
    private lateinit var quotesRecyclerView: QuotesRecyclerView
    private var currentValidityDeadlineTimestamp: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.uisdk_quotes_fragment, container, false)

        quotesSortWidget = view.findViewById(R.id.quotesSortWidget)
        addressBarWidget = view.findViewById(R.id.addressBarWidget)
        categorySelectorWidget = view.findViewById(R.id.categorySelectorWidget)
        quotesRecyclerView = view.findViewById(R.id.quotesRecyclerView)


        hideListInitially()

        quotesSortWidget.setListener(this)
        bookingStatusStateViewModel.viewActions().observe(this, bindToAddressBarOutputs())
        addressBarWidget.watchBookingStatusState(this, bookingStatusStateViewModel)
        categorySelectorWidget.bindViewToData(
            this.viewLifecycleOwner,
            categoriesViewModel,
            bookingStatusStateViewModel
        )
        quotesRecyclerView.watchCategories(this.viewLifecycleOwner, categoriesViewModel)
        quotesRecyclerView.watchQuoteListStatus(this.viewLifecycleOwner, bookingQuotesViewModel)
        bookingQuotesViewModel.viewStates().observe(this.viewLifecycleOwner, watchBookingQuotesStatus())
        liveFleetsViewModel.liveFleets.observe(this.viewLifecycleOwner, presenter.watchQuotes())
        val bundle = arguments

        if (bundle?.containsKey(QuotesActivity.QUOTES_BOOKING_INFO_KEY) == true) {
            dataModel = QuoteListViewDataModel(
                quotes = null,
                vehicles = null,
                bookingInfo = bundle.getParcelable(QuotesActivity.QUOTES_BOOKING_INFO_KEY)
            )

            presenter.setData(dataModel!!)

            dataModel?.bookingInfo?.let { bookingInfo ->
                bookingInfo.pickup?.let {
                    addressBarWidget.setPickup(it, -1)
                }
                bookingInfo.destination?.let {
                    addressBarWidget.setDestination(it, -1)
                }
            }
        }

        initAvailability();
        return view
    }

    override fun setViewDelegate(quoteListDelegate: QuotesFragmentContract.QuoteListDelegate) {
        this.quoteListViewDelegate = quoteListDelegate
    }

    override fun provideResources(): Resources {
        return resources
    }

    override fun setChevronState(isExpanded: Boolean) {
        val stateSet = intArrayOf(android.R.attr.state_checked * if (isExpanded) 1 else -1)
        chevronIcon.setImageState(stateSet, true)
    }

    override fun setSortMethod(sortMethod: SortMethod) {
        quotesRecyclerView.setSortMethod(sortMethod)
    }

    override fun onUserChangedSortMethod(sortMethod: SortMethod) {
        presenter.sortMethodChanged(sortMethod)

    }

    override fun sortChoiceRequiresDestination() {
        quoteListViewDelegate?.onError(SnackbarConfig(text = resources.getString(R.string.kh_uisdk_destination_price_error)))
    }

    override fun setup(data: QuoteListViewDataModel) {
        presenter.setData(data)
    }

    override fun destinationChanged(bookingInfo: BookingInfo) {
        quotesSortWidget.destinationChanged(bookingInfo)
    }

    override fun updateList(quoteList: List<Quote>) {
        quotesRecyclerView.updateList(quoteList)
    }

    override fun setListVisibility(pickup: LocationInfo?, destination: LocationInfo?) {
        quotesRecyclerView.setListVisibility(pickup != null && destination != null)
    }

    override fun prebook(isPrebook: Boolean) {
        quotesRecyclerView.prebook(isPrebook)
        quotesSortWidget.visibility = if (isPrebook) GONE else VISIBLE
    }

    private fun hideListInitially() {
//        animate().translationY(resources.getDimension(R.dimen.kh_uisdk_quote_list_height)).duration = 0
    }

    override fun showNoAvailability() {
        val activity = context as Activity
        val emailComposer = KarhooFeedbackEmailComposer(activity)

        val snackbarConfig = SnackbarConfig(
            type = SnackbarType.BLOCKING_DISMISSIBLE,
            priority = SnackbarPriority.NORMAL,
            action = SnackbarAction(resources.getString(R.string.kh_uisdk_contact)) {
                val showNoCoverageEmail = emailComposer.showNoCoverageEmail()
                showNoCoverageEmail?.let { intent ->
                    activity.startActivity(intent)
                }
            },
            text = resources.getString(R.string.kh_uisdk_no_availability)
        )

        quoteListViewDelegate?.onError(snackbarConfig)
    }

    override fun showNoResultsText(show: Boolean) {
        quotesRecyclerView.showNoResultsText(show)
    }


    override fun showSnackbarError(snackbarConfig: SnackbarConfig) {
        quoteListViewDelegate?.onError(snackbarConfig)
    }

    override fun showList(show: Boolean) {
        // will be modified later
    }

    private fun watchBookingQuotesStatus(): Observer<in QuoteListStatus> {
        return Observer { quoteListStatus ->
            quoteListStatus.selectedQuote?.let { quote ->
                val bundle = Bundle();
                bundle.putParcelable(QUOTES_SELECTED_QUOTE_KEY, quote)
                bundle.putParcelable(QUOTES_PICKUP_ADDRESS, dataModel?.bookingInfo?.pickup)
                bundle.putParcelable(QUOTES_DROPOFF_ADDRESS, dataModel?.bookingInfo?.destination)
                bundle.putSerializable(QUOTES_SELECTED_DATE, bookingStatusStateViewModel.currentState.date)
                bundle.putLong(QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP, currentValidityDeadlineTimestamp ?: 0)

                val intent = Intent()
                intent.putExtras(bundle)
                activity?.setResult(QUOTES_RESULT_OK, intent)
                activity?.finish()
            }
        }
    }

    override fun initAvailability() {
        availabilityProvider?.cleanup()
        val locale: Locale? = resources.configuration.locale
        bookingStatusStateViewModel?.let {
            availabilityProvider = KarhooAvailability(
                KarhooApi.quotesService,
                categoriesViewModel, liveFleetsViewModel,
                it, this.viewLifecycleOwner, locale
            ).apply {
                setAllCategory(resources.getString(R.string.kh_uisdk_all_category))
                setAvailabilityHandler(presenter)
                setAnalytics(KarhooUISDK.analytics)
                categorySelectorWidget.bindAvailability(this)
            }
            (availabilityProvider as KarhooAvailability).quoteListValidityListener =
                object : QuotesFragmentContract
                .QuoteValidityListener {
                    override fun isValidUntil(timestamp: Long) {
                        currentValidityDeadlineTimestamp = timestamp
                    }
                }
        }
    }

    private fun bindToAddressBarOutputs(): Observer<in AddressBarViewContract.AddressBarActions> {
        return Observer { actions ->
            when (actions) {
                is AddressBarViewContract.AddressBarActions.ShowAddressActivity ->
                    startActivityForResult(actions.intent, actions.addressCode)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            when (requestCode) {
                AddressCodes.PICKUP -> {
                    dataModel?.bookingInfo?.pickup = data.getParcelableExtra(AddressCodes.DATA_ADDRESS)
                    addressBarWidget.onActivityResult(requestCode, resultCode, data)
                }
                AddressCodes.DESTINATION -> {
                    dataModel?.bookingInfo?.destination = data.getParcelableExtra(AddressCodes.DATA_ADDRESS)
                    addressBarWidget.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    companion object {
        fun newInstance(bundle: Bundle?): Fragment {
            val fragment = QuotesFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
