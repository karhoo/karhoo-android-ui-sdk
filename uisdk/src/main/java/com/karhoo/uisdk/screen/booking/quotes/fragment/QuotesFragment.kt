package com.karhoo.uisdk.screen.booking.quotes.fragment

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.address.AddressCodes
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarView
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.checkout.quotes.BookingQuotesViewModel
import com.karhoo.uisdk.screen.booking.checkout.quotes.QuoteListStatus
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityProvider
import com.karhoo.uisdk.screen.booking.domain.quotes.KarhooAvailability
import com.karhoo.uisdk.screen.booking.domain.quotes.LiveFleetsViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_DROPOFF_ADDRESS
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_PICKUP_ADDRESS
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_RESULT_OK
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_SELECTED_DATE
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_SELECTED_QUOTE_KEY
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP
import com.karhoo.uisdk.screen.booking.quotes.category.CategoriesViewModel
import com.karhoo.uisdk.screen.booking.quotes.category.CategorySelectorView
import com.karhoo.uisdk.screen.booking.quotes.filterview.FilterDialogPresenter
import com.karhoo.uisdk.screen.booking.quotes.filterview.FilterDialogFragment
import com.karhoo.uisdk.screen.booking.quotes.filterview.FilterChain
import com.karhoo.uisdk.screen.booking.quotes.list.QuotesRecyclerView
import com.karhoo.uisdk.screen.booking.quotes.sortview.QuotesSortView
import java.util.Locale

class QuotesFragment : Fragment(), QuotesSortView.Listener,
    QuotesFragmentContract.View, LifecycleObserver, FilterDialogPresenter.FilterDelegate {

    private val journeyDetailsStateViewModel: JourneyDetailsStateViewModel by lazy {
        ViewModelProvider(this).get(
            JourneyDetailsStateViewModel::class.java
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
    private lateinit var quotesFilterWidget: FilterDialogFragment
    private lateinit var addressBarWidget: AddressBarView
    private lateinit var categorySelectorWidget: CategorySelectorView
    private lateinit var quotesRecyclerView: QuotesRecyclerView
    private lateinit var quotesTaxesAndFeesLabel: TextView
    private var currentValidityDeadlineTimestamp: Long? = null
    private lateinit var quotesSortByButton: MaterialButton
    private lateinit var quotesFilterByButton: MaterialButton
    private var isPrebook = false

    var filterChain = FilterChain()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.uisdk_quotes_fragment, container, false)

        addressBarWidget = view.findViewById(R.id.addressBarWidget)
        categorySelectorWidget = view.findViewById(R.id.categorySelectorWidget)
        quotesRecyclerView = view.findViewById(R.id.quotesRecyclerView)
        quotesTaxesAndFeesLabel = view.findViewById(R.id.quotesTaxesAndFeesLabel)
        initializeSortView()
        initializeFilterView()

        journeyDetailsStateViewModel.viewActions().observe(this, bindToAddressBarOutputs())
        addressBarWidget.watchJourneyDetailsState(this, journeyDetailsStateViewModel)

        journeyDetailsStateViewModel.viewStates().apply {
            observe(viewLifecycleOwner, subscribeToJourneyDetails(journeyDetailsStateViewModel))
        }
        categorySelectorWidget.bindViewToData(
            this.viewLifecycleOwner,
            categoriesViewModel,
            journeyDetailsStateViewModel
        )
        quotesRecyclerView.watchCategories(this.viewLifecycleOwner, categoriesViewModel)
        quotesRecyclerView.watchQuoteListStatus(this.viewLifecycleOwner, bookingQuotesViewModel)
        bookingQuotesViewModel.viewStates()
            .observe(this.viewLifecycleOwner, watchBookingQuotesStatus())
        liveFleetsViewModel.liveFleets.observe(this.viewLifecycleOwner, presenter.watchQuotes())
        val bundle = arguments

        if (bundle?.containsKey(QuotesActivity.QUOTES_BOOKING_INFO_KEY) == true) {
            dataModel = QuoteListViewDataModel(
                quotes = null,
                vehicles = null,
                journeyDetails = bundle.getParcelable(QuotesActivity.QUOTES_BOOKING_INFO_KEY)
            )

            presenter.setData(dataModel!!)

            dataModel?.journeyDetails?.let { bookingInfo ->
                bookingInfo.pickup?.let {
                    addressBarWidget.setPickup(it, -1)
                }
                bookingInfo.destination?.let {
                    addressBarWidget.setDestination(it, -1)
                }
                bookingInfo.date?.let {
                    addressBarWidget.setPrebookTime(it)
                }
            }
        }

        quotesSortByButton = view.findViewById(R.id.quotesSortByButton)
        quotesSortByButton.apply {
            visibility = if (isPrebook) GONE else VISIBLE
            setOnClickListener { showSortBy() }
        }

        quotesFilterByButton = view.findViewById(R.id.quotesFilterByButton)
        quotesFilterByButton.apply {
            visibility = VISIBLE
            setOnClickListener { showFilters() }
        }

        initAvailability();

        showFilteringWidgets(false)

        return view
    }

    fun initializeSortView(){
        quotesSortWidget = QuotesSortView()
        quotesSortWidget.setListener(this)
    }

    fun initializeFilterView(){
        quotesFilterWidget = FilterDialogFragment()
        quotesFilterWidget.setListener(this)
        quotesFilterWidget.createFilterChain(filterChain)
    }

    override fun onPause() {
        super.onPause()
        availabilityProvider?.cleanup()
    }

    fun subscribeToJourneyDetails(journeyDetailsStateViewModel: JourneyDetailsStateViewModel): Observer<JourneyDetails> {
        return Observer {
            it?.let {
                val sortMethod = SortMethod.PRICE
                quotesSortWidget.selectedSortMethod.postValue(sortMethod)

                dataModel = QuoteListViewDataModel(
                    quotes = null,
                    vehicles = null,
                    journeyDetails = it
                )
                presenter.setData(dataModel!!)
            }
        }
    }

    private fun showSortBy(){
        activity?.supportFragmentManager?.let {
            quotesSortWidget.show(it, QuotesSortView.TAG)
        }
    }

    private fun showFilters(){
        activity?.supportFragmentManager?.let {
            quotesFilterWidget.show(it, FilterDialogFragment.TAG)
        }
    }

    override fun setViewDelegate(quoteListDelegate: QuotesFragmentContract.QuoteListDelegate) {
        this.quoteListViewDelegate = quoteListDelegate
    }

    override fun provideResources(): Resources {
        return resources
    }

    override fun setSortMethod(sortMethod: SortMethod) {
        quotesRecyclerView.setSortMethod(sortMethod)
    }

    override fun onUserChangedSortMethod(sortMethod: SortMethod) {
        presenter.sortMethodChanged(sortMethod)

    }

    override fun setup(data: QuoteListViewDataModel) {
        presenter.setData(data)
    }

    override fun destinationChanged(journeyDetails: JourneyDetails) {
        val sortMethod = SortMethod.PRICE
        quotesSortWidget.selectedSortMethod.postValue(sortMethod)
        presenter.sortMethodChanged(sortMethod)
    }

    private fun changeVisibilityOfQuotesSortByButton(show: Boolean){
        this::quotesSortByButton.isInitialized.let {
            if(it)
                quotesSortByButton.visibility = if (show && !isPrebook) VISIBLE else GONE
        }
    }

    private fun changeVisibilityOfQuotesFilterByButton(show: Boolean){
        this::quotesFilterByButton.isInitialized.let {
            if(it)
                quotesFilterByButton.visibility = if (show) VISIBLE else GONE
        }
    }

    override fun updateList(quoteList: List<Quote>) {
        showFilteringWidgets(quoteList.isNotEmpty())
        quotesRecyclerView.updateList(quoteList)
    }

    override fun setListVisibility(pickup: LocationInfo?, destination: LocationInfo?) {
        quotesRecyclerView.setListVisibility(pickup != null && destination != null)
    }

    override fun prebook(isPrebook: Boolean) {
        quotesRecyclerView.prebook(isPrebook)
        this.isPrebook = isPrebook
        changeVisibilityOfQuotesSortByButton(isPrebook)
    }

    override fun showNoCoverageError(show: Boolean) {
        showFilteringWidgets(!show)
        quotesRecyclerView.showNoCoverageError(show)
    }

    override fun showNoFleetsError(show: Boolean) {
        showFilteringWidgets(!show)
        quotesRecyclerView.showNoFleetsError(show)
    }

    override fun showSameAddressesError(show: Boolean) {
        showFilteringWidgets(!show)
        quotesRecyclerView.showSameAddressesError(show)
    }

    override fun showNoAddressesError(show: Boolean) {
        showFilteringWidgets(!show)
        quotesRecyclerView.showNoAddressesError(show)
    }

    private fun showFilteringWidgets(show: Boolean) {
        changeVisibilityOfQuotesSortByButton(show)
        changeVisibilityOfQuotesFilterByButton(show)
        categorySelectorWidget.visibility = if (show) VISIBLE else GONE
        quotesTaxesAndFeesLabel.visibility = if (show) VISIBLE else GONE
    }

    override fun showSnackbarError(snackbarConfig: SnackbarConfig) {
        quoteListViewDelegate?.onError(snackbarConfig)
    }

    override fun showList(show: Boolean) {
        changeVisibilityOfQuotesSortByButton(show)
        changeVisibilityOfQuotesFilterByButton(show)
        categorySelectorWidget.visibility = if (show) VISIBLE else GONE
        quotesTaxesAndFeesLabel.visibility = if (show) VISIBLE else GONE
        // will be modified later
    }

    private fun watchBookingQuotesStatus(): Observer<in QuoteListStatus> {
        return Observer { quoteListStatus ->
            quoteListStatus.selectedQuote?.let { quote ->
                val bundle = Bundle();
                bundle.putParcelable(QUOTES_SELECTED_QUOTE_KEY, quote)
                bundle.putParcelable(QUOTES_PICKUP_ADDRESS, dataModel?.journeyDetails?.pickup)
                bundle.putParcelable(QUOTES_DROPOFF_ADDRESS, dataModel?.journeyDetails?.destination)
                bundle.putSerializable(
                    QUOTES_SELECTED_DATE,
                    journeyDetailsStateViewModel.currentState.date
                )
                bundle.putLong(
                    QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP,
                    currentValidityDeadlineTimestamp ?: 0
                )

                val intent = Intent()
                intent.putExtras(bundle)
                activity?.setResult(QUOTES_RESULT_OK, intent)
                activity?.finish()
            }
        }
    }

    private fun initAvailability() {
        availabilityProvider?.cleanup()
        val locale: Locale? = resources.configuration.locale
        journeyDetailsStateViewModel?.let {
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
                is AddressBarViewContract.AddressBarActions.AddressChanged -> {
                    when (actions.addressCode) {
                        AddressCodes.PICKUP -> {
                            dataModel?.journeyDetails?.pickup = actions.address
                        }
                        AddressCodes.DESTINATION -> {
                            dataModel?.journeyDetails?.destination = actions.address
                        }
                    }

                    dataModel?.let {
                        presenter.setData(it)
                    }
                }
                is AddressBarViewContract.AddressBarActions.ShowAddressActivity ->
                    startActivityForResult(actions.intent, actions.addressCode)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            when (requestCode) {
                AddressCodes.PICKUP -> {
                    dataModel?.journeyDetails?.pickup =
                        data.getParcelableExtra(AddressCodes.DATA_ADDRESS)
                    addressBarWidget.onActivityResult(requestCode, resultCode, data)
                }
                AddressCodes.DESTINATION -> {
                    dataModel?.journeyDetails?.destination =
                        data.getParcelableExtra(AddressCodes.DATA_ADDRESS)
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

    override fun onUserChangedFilter(): Int {
        return availabilityProvider?.getNonFilteredVehicles()
            ?.let { filterChain.applyFilters(it).size }?: 0
    }

    override fun onFiltersApplied() {
        availabilityProvider?.filterVehicleListByFilterChain(filterChain)
    }
}
