package com.karhoo.uisdk.screen.booking.quotes.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteStatus
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
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.LUGGAGE
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.PASSENGER_NUMBER
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_DROPOFF_ADDRESS
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_PICKUP_ADDRESS
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_RESULT_OK
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_SELECTED_DATE
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_SELECTED_QUOTE_KEY
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP
import com.karhoo.uisdk.screen.booking.quotes.filterview.FilterDialogPresenter
import com.karhoo.uisdk.screen.booking.quotes.filterview.FilterDialogFragment
import com.karhoo.uisdk.screen.booking.quotes.filterview.FilterChain
import com.karhoo.uisdk.screen.booking.quotes.filterview.PassengersFilter
import com.karhoo.uisdk.screen.booking.quotes.filterview.LuggageFilter
import com.karhoo.uisdk.screen.booking.quotes.list.QuotesRecyclerView
import com.karhoo.uisdk.screen.booking.quotes.sortview.QuotesSortView
import java.util.Locale
import com.karhoo.uisdk.screen.booking.domain.quotes.KarhooAvailability.setAnalytics
import com.karhoo.uisdk.screen.booking.domain.quotes.KarhooAvailability.setAvailabilityHandler
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_RESTORE_PREVIOUS_DATA_KEY
import java.util.*
import java.util.concurrent.TimeUnit

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
    private val liveFleetsViewModel: LiveFleetsViewModel = LiveFleetsViewModel()
    private lateinit var quotesSortWidget: QuotesSortView
    private lateinit var progressBarWidget: ProgressBar
    private lateinit var quotesFilterWidget: FilterDialogFragment
    private lateinit var addressBarWidget: AddressBarView
    private lateinit var quotesRecyclerView: QuotesRecyclerView
    private lateinit var quotesTaxesAndFeesLabel: TextView
    private var currentValidityDeadlineTimestamp: Long? = null
    private lateinit var quotesSortByButton: MaterialButton
    private lateinit var quotesFilterByButton: MaterialButton
    private var isPrebook = false
    private var restorePreviousData = false

    var nrOfResults: MutableLiveData<Int> = MutableLiveData(0)
    var filterChain = FilterChain()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.uisdk_quotes_fragment, container, false)

        progressBarWidget = view.findViewById(R.id.progressBar)
        addressBarWidget = view.findViewById(R.id.addressBarWidget)
        quotesRecyclerView = view.findViewById(R.id.quotesRecyclerView)
        quotesTaxesAndFeesLabel = view.findViewById(R.id.quotesTaxesAndFeesLabel)
        initializeSortView()
        initializeFilterView()

        journeyDetailsStateViewModel.viewActions().observe(this, bindToAddressBarOutputs())
        addressBarWidget.watchJourneyDetailsState(this, journeyDetailsStateViewModel)

        journeyDetailsStateViewModel.viewStates().apply {
            observe(viewLifecycleOwner, subscribeToJourneyDetails())
        }
        quotesRecyclerView.watchQuoteListStatus(this.viewLifecycleOwner, bookingQuotesViewModel)
        bookingQuotesViewModel.viewStates()
            .observe(this.viewLifecycleOwner, watchBookingQuotesStatus())
        liveFleetsViewModel.liveFleets.observe(this.viewLifecycleOwner, presenter.watchQuotes())

        parseArguments(arguments)

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

        initAvailability()
        initProgressBar()

        showFilteringWidgets(true)

        return view
    }


    private fun parseArguments(bundle: Bundle?) {
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

        currentValidityDeadlineTimestamp =
            if (bundle?.getLong(QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP) != 0L) {
                bundle?.getLong(QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP)
            } else {
                null
            }

        restorePreviousData =
            bundle?.getBoolean(QUOTES_RESTORE_PREVIOUS_DATA_KEY) == true && !shouldRefreshQuoteList()
    }

    fun initializeSortView() {
        quotesSortWidget = QuotesSortView()
        quotesSortWidget.setListener(this)
    }

    fun initializeFilterView() {
        quotesFilterWidget = FilterDialogFragment()
        quotesFilterWidget.setListener(this)
        quotesFilterWidget.createFilterChain(filterChain)
    }

    override fun onResume() {
        super.onResume()

        if (availabilityProvider?.shouldRunInBackground == true || shouldRefreshQuoteList()) {
            availabilityProvider?.resumeUpdates()
        } else {
            availabilityProvider?.restoreData()
        }
        onFiltersApplied()
    }

    fun subscribeToJourneyDetails(): Observer<JourneyDetails> {
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

    fun getJourneyDetails(): JourneyDetails? {
        return dataModel?.journeyDetails
    }

    private fun showSortBy() {
        activity?.supportFragmentManager?.let {
            quotesSortWidget.show(it, QuotesSortView.TAG)
        }
    }

    private fun showFilters() {
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

    private fun changeVisibilityOfQuotesSortByButton(show: Boolean) {
        this::quotesSortByButton.isInitialized.let {
            if (it)
                quotesSortByButton.visibility = if (show && !isPrebook) VISIBLE else GONE
        }
    }

    private fun changeVisibilityOfQuotesFilterByButton(show: Boolean) {
        this::quotesFilterByButton.isInitialized.let {
            if (it)
                quotesFilterByButton.visibility = if (show) VISIBLE else GONE
        }
    }

    override fun updateList(quoteList: List<Quote>) {
        if (quotesFilterWidget.isVisible)
            quotesFilterWidget.updateVehicleNumber()
        quotesRecyclerView.updateList(quoteList)
        nrOfResults.postValue(quoteList.size)
    }

    override fun setListVisibility(pickup: LocationInfo?, destination: LocationInfo?) {
        quotesRecyclerView.setListVisibility(pickup != null && destination != null)
    }

    override fun prebook(isPrebook: Boolean) {
        quotesRecyclerView.prebook(isPrebook)
        this.isPrebook = isPrebook
        changeVisibilityOfQuotesSortByButton(true)
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

    override fun showNoResultsAfterFilterError() {
        if (dataModel?.quotes?.size == 0 && filterChain.filters.any { it.isFilterApplied == true }) {
            showFilteringWidgets(true)
            quotesRecyclerView.showNoResultsAfterFilterError(true)
        } else {
            quotesRecyclerView.showNoResultsAfterFilterError(false)
        }
    }

    private fun showFilteringWidgets(show: Boolean) {
        changeVisibilityOfQuotesSortByButton(show)
        changeVisibilityOfQuotesFilterByButton(show)
        quotesTaxesAndFeesLabel.visibility = if (show) VISIBLE else GONE
    }

    override fun showSnackbarError(snackbarConfig: SnackbarConfig) {
        quoteListViewDelegate?.onError(snackbarConfig)
    }

    override fun showList(show: Boolean) {
        changeVisibilityOfQuotesSortByButton(show)
        changeVisibilityOfQuotesFilterByButton(show)
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
                bundle.putInt(
                    PASSENGER_NUMBER,
                    (filterChain.filters[0] as PassengersFilter).currentNumber
                )
                bundle.putInt(LUGGAGE, (filterChain.filters[1] as LuggageFilter).currentNumber)

                /** Tell the availability provider to run in background when going to the
                 * Checkout Screen after selecting a quote
                 */
                availabilityProvider?.shouldRunInBackground = true

                val intent = Intent()
                intent.putExtras(bundle)
                activity?.setResult(QUOTES_RESULT_OK, intent)
                activity?.finish()
            }
        }
    }

    private fun initAvailability() {
        val locale: Locale? = resources.configuration.locale
        journeyDetailsStateViewModel.let {
            availabilityProvider = KarhooAvailability
            /** If there is a new availability request, we should cleanup the previous data */
            if (!restorePreviousData) {
                availabilityProvider?.cleanup()
            } else {
                availabilityProvider?.getExistingFilterChain()?.let { chain ->
                    filterChain = chain
                    quotesFilterWidget.presenter.filterChain = filterChain
                }
            }

            if (shouldRefreshQuoteList()) {
                availabilityProvider?.shouldRunInBackground = false
            }

            availabilityProvider?.setup(
                KarhooApi.quotesService,
                liveFleetsViewModel,
                it, this.viewLifecycleOwner, locale, restorePreviousData
            ).apply {
                setAvailabilityHandler(presenter)
                setAnalytics(KarhooUISDK.analytics)
            }
            availabilityProvider?.shouldRunInBackground = false
            (availabilityProvider as KarhooAvailability).quoteListValidityListener =
                object : QuotesFragmentContract
                .QuoteValidityListener {
                    override fun isValidUntil(timestamp: Long) {
                        currentValidityDeadlineTimestamp = timestamp
                    }
                }
        }

        if ((dataModel?.quotes?.size?.compareTo(0) ?: 1) <= 0)
            showFilteringWidgets(false)
        else
            showFilteringWidgets(true)
    }

    private fun initProgressBar() {
        (availabilityProvider as KarhooAvailability).quoteListPoolingStatusListener =
            object : QuotesFragmentContract
            .QuotePoolingStatusListener {
                override fun changedStatus(status: QuoteStatus?) {
                    if (status == QuoteStatus.COMPLETED) {
                        progressBarWidget.visibility = View.INVISIBLE
                    } else {
                        progressBarWidget.visibility = VISIBLE
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
        private const val MINIMUM_REFRESH_DURATION_LEFT_SECONDS = 120

        fun newInstance(bundle: Bundle?): Fragment {
            val fragment = QuotesFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onUserChangedFilter(): Int {
        return availabilityProvider?.getNonFilteredVehicles()
            ?.let { filterChain.applyFilters(it).size } ?: 0
    }

    override fun onFiltersApplied() {
        availabilityProvider?.filterVehicleListByFilterChain(filterChain)!!
        val nrOfFiltersApplied = filterChain.filters.filter { it.isFilterApplied == true }.size
        if (nrOfFiltersApplied != 0) {
            quotesFilterByButton.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.kh_uisdk_accent
                )
            )
            quotesFilterByButton.iconTint = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.kh_uisdk_accent
                )
            )
            val textToShow = "${resources.getString(R.string.kh_uisdk_filter)}($nrOfFiltersApplied)"
            quotesFilterByButton.text = textToShow
            quotesFilterByButton.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.kh_uisdk_quote_list_filter_by_applied_button
            )
            showNoResultsAfterFilterError()
        } else {
            showNoResultsAfterFilterError()
            quotesFilterByButton.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.textColor
                )
            )
            quotesFilterByButton.iconTint =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.textColor))
            quotesFilterByButton.text = resources.getString(R.string.kh_uisdk_filter)
            quotesFilterByButton.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.kh_uisdk_quote_list_sort_by_button
            )
        }
    }

    private fun shouldRefreshQuoteList(): Boolean {
        return TimeUnit.MILLISECONDS.toSeconds(
            ((currentValidityDeadlineTimestamp ?: Long.MAX_VALUE) - Date().time)
        ) < MINIMUM_REFRESH_DURATION_LEFT_SECONDS
    }
}
