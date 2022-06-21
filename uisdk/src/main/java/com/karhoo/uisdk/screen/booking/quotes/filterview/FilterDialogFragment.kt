package com.karhoo.uisdk.screen.booking.quotes.filterview

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.view.LoadingButtonView

const val passengerFilterPosition = 0
const val luggageFilterPosition = 1
const val quoteTypesFilterPosition = 2
const val serviceAgreementsFilterPosition = 3
const val vehicleTypeFilterPosition = 4
const val vehicleClassFilterPosition = 5
const val vehicleExtrasFilterPosition = 6
const val vehicleEcoFilterPosition = 7
const val fleetCapabilitiesFilterPosition = 8

class FilterDialogFragment : BottomSheetDialogFragment(), FilterDialogContract.View {

    private lateinit var quotesFilterSave: LoadingButtonView
    var presenter = FilterDialogPresenter(this)

    private lateinit var filterViewResetFilters: TextView
    private lateinit var filterViewTitleExit: ImageView
    private lateinit var filterViewPassengerNumberedFilter: NumberedFilterView
    private lateinit var filterViewLuggageNumberedFilter: NumberedFilterView

    private lateinit var filterViewVehicleTypeMultiSelectChipsFilter: MultiSelectChipsFilterView
    private lateinit var filterViewVehicleClassMultiSelectChipsFilter: MultiSelectChipsFilterView
    private lateinit var filterViewVehicleExtrasMultiSelectChipsFilter: MultiSelectChipsFilterView
    private lateinit var filterViewVehicleEcoMultiSelectChipsFilter: MultiSelectChipsFilterView
    private lateinit var filterViewFleetCapabilitiesMultiSelectChipsFilter: MultiSelectChipsFilterView

    private lateinit var filterViewQuoteTypeMultiSelectCheckboxFilter: MultiSelectCheckboxFilterView
    private lateinit var filterViewServiceAgreementsMultiSelectCheckboxFilter: MultiSelectCheckboxFilterView

    private lateinit var passengersFilter: PassengersFilter
    private lateinit var luggageFilter: LuggageFilter
    private lateinit var quoteTypesFilter: QuoteTypesFilter
    private lateinit var serviceAgreementsFilter: ServiceAgreementsFilter
    private lateinit var vehicleTypeFilter : VehicleTypeFilter
    private lateinit var vehicleClassFilter : VehicleClassFilter
    private lateinit var vehicleExtrasFilter : VehicleExtrasFilter
    private lateinit var vehicleEcoFilter : VehicleEcoFilter
    private lateinit var fleetCapabilitiesFilter : FleetCapabilitiesFilter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.uisdk_view_filter, container, false)

        quotesFilterSave = view.findViewById(R.id.quotesFilterSave)
        quotesFilterSave.setOnClickListener {
            presenter.applyFilters()
            dismiss()
        }

        filterViewResetFilters = view.findViewById(R.id.filterViewResetFilters)
        filterViewResetFilters.setOnClickListener {
            presenter.resetFilters()
            dismiss()
        }

        filterViewTitleExit = view.findViewById(R.id.filterViewTitleExit)
        filterViewTitleExit.setOnClickListener {
            dismiss()
        }

        filterViewPassengerNumberedFilter =
            view.findViewById(R.id.filterViewPassengerNumberedFilter)
        filterViewLuggageNumberedFilter =
            view.findViewById(R.id.filterViewLuggageNumberedFilter)
        filterViewVehicleTypeMultiSelectChipsFilter =
            view.findViewById(R.id.filterViewVehicleTypeMultiSelectChipsFilter)
        filterViewVehicleClassMultiSelectChipsFilter =
            view.findViewById(R.id.filterViewVehicleClassMultiSelectChipsFilter)
        filterViewVehicleExtrasMultiSelectChipsFilter =
            view.findViewById(R.id.filterViewVehicleExtrasMultiSelectChipsFilter)
        filterViewVehicleEcoMultiSelectChipsFilter =
            view.findViewById(R.id.filterViewVehicleEcoMultiSelectChipsFilter)
        filterViewFleetCapabilitiesMultiSelectChipsFilter =
            view.findViewById(R.id.filterViewFleetCapabilitiesMultiSelectChipsFilter)
        filterViewQuoteTypeMultiSelectCheckboxFilter =
            view.findViewById(R.id.filterViewQuoteTypeMultiSelectCheckboxFilter)
        filterViewServiceAgreementsMultiSelectCheckboxFilter =
            view.findViewById(R.id.filterViewServiceAgreementsMultiSelectCheckboxFilter)

        presenter.createFilters()
        return view
    }

    fun setListener(filterDelegate: FilterDialogPresenter.FilterDelegate) {
        presenter.setFilterDelegate(filterDelegate)
    }

    fun createFilterChain(filterChain: FilterChain) {
        presenter.filterChain = filterChain

        passengersFilter = PassengersFilter(1)
        luggageFilter = LuggageFilter(0)
        quoteTypesFilter = QuoteTypesFilter(ArrayList())
        serviceAgreementsFilter = ServiceAgreementsFilter(ArrayList())
        vehicleTypeFilter = VehicleTypeFilter(ArrayList())
        vehicleClassFilter = VehicleClassFilter(ArrayList())
        vehicleExtrasFilter = VehicleExtrasFilter(ArrayList())
        vehicleEcoFilter = VehicleEcoFilter(ArrayList())
        fleetCapabilitiesFilter = FleetCapabilitiesFilter(ArrayList())

        filterChain.filters.add(passengersFilter)
        filterChain.filters.add(luggageFilter)
        filterChain.filters.add(quoteTypesFilter)
        filterChain.filters.add(serviceAgreementsFilter)
        filterChain.filters.add(vehicleTypeFilter)
        filterChain.filters.add(vehicleClassFilter)
        filterChain.filters.add(vehicleExtrasFilter)
        filterChain.filters.add(vehicleEcoFilter)
        filterChain.filters.add(fleetCapabilitiesFilter)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { layout ->
                val behaviour = BottomSheetBehavior.from(layout)
                setupFullHeight(layout)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    override fun createFilters(filterChain: FilterChain) {
        createPassengerNumberedFilter()
        createLuggageNumberedFilter()
        createQuoteTypeFilter()
        createServiceAgreementsFilter()
        createVehicleTypeFilter()
        createVehicleClassFilter()
        createVehicleExtrasFilter()
        createVehicleEcoFilter()
        createFleetCapabilitiesFilter()
    }

    private fun createPassengerNumberedFilter(){
        filterViewPassengerNumberedFilter.filter = presenter.filterChain.filters[passengerFilterPosition] as PassengersFilter
        filterViewPassengerNumberedFilter.setTitle(getString(R.string.kh_uisdk_filter_passengers))
        filterViewPassengerNumberedFilter.icon = R.drawable.kh_uisdk_ic_passengers
        filterViewPassengerNumberedFilter.delegate = {
            presenter.callFilterChanged()
        }
    }

    private fun createLuggageNumberedFilter(){
        filterViewLuggageNumberedFilter.filter = presenter.filterChain.filters[luggageFilterPosition] as LuggageFilter
        filterViewLuggageNumberedFilter.setTitle(getString(R.string.kh_uisdk_filter_luggages))
        filterViewLuggageNumberedFilter.icon = R.drawable.kh_uisdk_ic_luggage
        filterViewLuggageNumberedFilter.delegate = {
            presenter.callFilterChanged()
        }
    }

    private fun createQuoteTypeFilter(){
        val quoteTypesFilter = presenter.filterChain.filters[quoteTypesFilterPosition] as QuoteTypesFilter
        quoteTypesFilter.apply {
            typeValues = ArrayList<MultiSelectData>().apply {
                add(MultiSelectData(getString(R.string.kh_uisdk_fixed_fare)).apply { fixedTag = QuoteTypesFilter.FIXED_TAG })
                add(MultiSelectData(getString(R.string.kh_uisdk_estimated_fare)).apply { fixedTag = QuoteTypesFilter.ESTIMATED_TAG })
            }
        }
        filterViewQuoteTypeMultiSelectCheckboxFilter.filter = quoteTypesFilter
        filterViewQuoteTypeMultiSelectCheckboxFilter.choices = quoteTypesFilter.typeValues
        filterViewQuoteTypeMultiSelectCheckboxFilter.setTitle(getString(R.string.kh_uisdk_filter_quote_types))
        filterViewQuoteTypeMultiSelectCheckboxFilter.delegate = {
            presenter.callFilterChanged()
        }
    }

    private fun createServiceAgreementsFilter(){
        val serviceAgreementsFilter = presenter.filterChain.filters[serviceAgreementsFilterPosition] as ServiceAgreementsFilter
        serviceAgreementsFilter.apply {
            typeValues = ArrayList<MultiSelectData>().apply {
                add(MultiSelectData(getString(R.string.kh_uisdk_filter_free_waiting_time)).apply { fixedTag = ServiceAgreementsFilter.FREE_WAITING_TIME_TAG })
                add(MultiSelectData(getString(R.string.kh_uisdk_filter_free_cancellation)).apply { fixedTag = ServiceAgreementsFilter.FREE_CANCELLATION_TAG })
            }
        }
        filterViewServiceAgreementsMultiSelectCheckboxFilter.filter = serviceAgreementsFilter
        filterViewServiceAgreementsMultiSelectCheckboxFilter.choices = serviceAgreementsFilter.typeValues
        filterViewServiceAgreementsMultiSelectCheckboxFilter.setTitle(getString(R.string.kh_uisdk_filter_cancellation_and_waiting_time))
        filterViewServiceAgreementsMultiSelectCheckboxFilter.delegate = {
            presenter.callFilterChanged()
        }
    }

    private fun createVehicleTypeFilter(){
        val vehicleTypeFilter = presenter.filterChain.filters[vehicleTypeFilterPosition] as VehicleTypeFilter

        vehicleTypeFilter.apply {
            typeValues = ArrayList<MultiSelectData>().apply {
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_filter_all)).apply { fixedTag = VehicleTypeFilter.ALL_TAG })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_vehicle_standard)).apply { fixedTag = VehicleTypeFilter.STANDARD })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_saloon)).apply { fixedTag = VehicleTypeFilter.BERLINE })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_filter_van)).apply { fixedTag = VehicleTypeFilter.VAN })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_vehicle_moto)).apply { fixedTag = VehicleTypeFilter.MOTO })
            }
        }
        filterViewVehicleTypeMultiSelectChipsFilter.filter = vehicleTypeFilter
        filterViewVehicleTypeMultiSelectChipsFilter.chips = vehicleTypeFilter.typeValues
        filterViewVehicleTypeMultiSelectChipsFilter.setTitle(resources.getString(R.string.kh_uisdk_filter_vehicle_types))
        filterViewVehicleTypeMultiSelectChipsFilter.delegate = {
            presenter.callFilterChanged()
        }
    }

    private fun createVehicleClassFilter(){
        val vehicleClassFilter = presenter.filterChain.filters[vehicleClassFilterPosition] as VehicleClassFilter

        vehicleClassFilter.apply {
            typeValues = ArrayList<MultiSelectData>().apply {
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_filter_all)).apply { fixedTag = VehicleTypeFilter.ALL_TAG })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_filter_executive)).apply {
                    icon = R.drawable.kh_uisdk_ic_briefcase
                    fixedTag = VehicleClassFilter.EXECUTIVE
                })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_filter_luxury)).apply {
                    icon = R.drawable.kh_uisdk_ic_star_empty
                    fixedTag = VehicleClassFilter.LUXURY
                })
            }
        }
        filterViewVehicleClassMultiSelectChipsFilter.filter = vehicleClassFilter
        filterViewVehicleClassMultiSelectChipsFilter.chips = vehicleClassFilter.typeValues
        filterViewVehicleClassMultiSelectChipsFilter.setTitle(resources.getString(R.string.kh_uisdk_filter_vehicle_class))
        filterViewVehicleClassMultiSelectChipsFilter.delegate = {
            presenter.callFilterChanged()
        }
    }

    private fun createVehicleExtrasFilter(){
        val vehicleExtrasFilter = presenter.filterChain.filters[vehicleExtrasFilterPosition] as VehicleExtrasFilter

        vehicleExtrasFilter.apply {
            typeValues = ArrayList<MultiSelectData>().apply {
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_filter_all)).apply { fixedTag = VehicleTypeFilter.ALL_TAG })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_taxi)).apply {
                    icon = R.drawable.kh_uisdk_ic_car
                    fixedTag = VehicleExtrasFilter.TAXI
                })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_filter_child_seat)).apply {
                    icon = R.drawable.kh_uisdk_ic_tag_child_seat
                    fixedTag = VehicleExtrasFilter.CHILD_SEAT
                })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_wheelchair)).apply {
                    icon = R.drawable.kh_uisdk_ic_wheelchair
                    fixedTag = VehicleExtrasFilter.WHEELCHAIR
                })
            }
        }
        filterViewVehicleExtrasMultiSelectChipsFilter.filter = vehicleExtrasFilter
        filterViewVehicleExtrasMultiSelectChipsFilter.chips = vehicleExtrasFilter.typeValues
        filterViewVehicleExtrasMultiSelectChipsFilter.setTitle(resources.getString(R.string.kh_uisdk_filter_vehicle_extras))
        filterViewVehicleExtrasMultiSelectChipsFilter.delegate = {
            presenter.callFilterChanged()
        }
    }

    private fun createVehicleEcoFilter(){
        val vehicleEcoFilter = presenter.filterChain.filters[vehicleEcoFilterPosition] as VehicleEcoFilter

        vehicleEcoFilter.apply {
            typeValues = ArrayList<MultiSelectData>().apply {
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_filter_all)).apply { fixedTag = VehicleTypeFilter.ALL_TAG })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_electric)).apply {
                    icon = R.drawable.kh_uisdk_ic_zap
                    fixedTag = VehicleEcoFilter.ELECTRIC
                })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_filter_hybrid)).apply {
                    icon = R.drawable.kh_uisdk_ic_feather
                    fixedTag = VehicleEcoFilter.HYBRID
                })
            }
        }
        filterViewVehicleEcoMultiSelectChipsFilter.filter = vehicleEcoFilter
        filterViewVehicleEcoMultiSelectChipsFilter.chips = vehicleEcoFilter.typeValues
        filterViewVehicleEcoMultiSelectChipsFilter.setTitle(resources.getString(R.string.kh_uisdk_filter_eco_friendly))
        filterViewVehicleEcoMultiSelectChipsFilter.delegate = {
            presenter.callFilterChanged()
        }
    }

    private fun createFleetCapabilitiesFilter(){
        val fleetCapabilitiesFilter = presenter.filterChain.filters[fleetCapabilitiesFilterPosition] as FleetCapabilitiesFilter

        fleetCapabilitiesFilter.apply {
            typeValues = ArrayList<MultiSelectData>().apply {
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_filter_all)).apply { fixedTag = VehicleTypeFilter.ALL_TAG })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_flight_tracking)).apply {
                    icon = R.drawable.kh_uisdk_ic_plane
                    fixedTag = FleetCapabilitiesFilter.FLIGHT_TRACKING
                })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_train_tracking)).apply {
                    icon = R.drawable.kh_uisdk_ic_train
                    fixedTag = FleetCapabilitiesFilter.TRAIN_TRACKING
                })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_gps_tracking)).apply {
                    icon = R.drawable.kh_uisdk_ic_location_arrow_alt
                    fixedTag = FleetCapabilitiesFilter.GPS_TRACKING
                })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_filter_driver_details)).apply {
                    icon = R.drawable.kh_uisdk_ic_user
                    fixedTag = FleetCapabilitiesFilter.DRIVER_DETAILS
                })
                add(MultiSelectData(resources.getString(R.string.kh_uisdk_filter_vehicle_details)).apply {
                    icon = R.drawable.kh_uisdk_ic_car
                    fixedTag = FleetCapabilitiesFilter.VEHICLE_DETAILS
                })
            }
        }
        filterViewFleetCapabilitiesMultiSelectChipsFilter.filter = fleetCapabilitiesFilter
        filterViewFleetCapabilitiesMultiSelectChipsFilter.chips = fleetCapabilitiesFilter.typeValues
        filterViewFleetCapabilitiesMultiSelectChipsFilter.setTitle(resources.getString(R.string.kh_uisdk_filter_fleet_capabilities))
        filterViewFleetCapabilitiesMultiSelectChipsFilter.delegate = {
            presenter.callFilterChanged()
        }
    }

    fun updateVehicleNumber(){
        presenter.callFilterChanged()
    }

    override fun setNumberOfResultsAfterFilter(size: Int) {
        if(::quotesFilterSave.isInitialized)
            quotesFilterSave.setText(
                String.format(
                    getString(R.string.kh_uisdk_filter_page_results),
                    size
                )
            )
    }

    companion object {
        const val TAG = "FilterView"
    }
}
