package com.karhoo.uisdk.screen.booking.quotes.filterview

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.view.LoadingButtonView


class FilterDialogFragment : BottomSheetDialogFragment(), FilterDialogContract.View {

    var quotesFilterSave: LoadingButtonView? = null
    var presenter = FilterDialogPresenter(this)

    private lateinit var filterViewResetFilters: TextView
    private lateinit var filterViewPassengerNumberedFilter: NumberedFilterView
    private lateinit var filterViewLuggageNumberedFilter: NumberedFilterView
    private lateinit var filterViewVehicleTypeMultiSelectChipsFilter: MultiSelectChipsFilterView

    private lateinit var passengersFilter: PassengersFilter
    private lateinit var luggageFilter: LuggageFilter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.uisdk_view_filter, container, false)

        quotesFilterSave = view.findViewById(R.id.quotesFilterSave)
        quotesFilterSave!!.setOnClickListener {
            presenter.applyFilters()
            dismiss()
        }

        filterViewResetFilters = view.findViewById(R.id.filterViewResetFilters)
        filterViewResetFilters.setOnClickListener {
            presenter.resetFilters()
            dismiss()
        }

        filterViewPassengerNumberedFilter =
            view.findViewById<NumberedFilterView>(R.id.filterViewPassengerNumberedFilter)
        filterViewLuggageNumberedFilter =
            view.findViewById<NumberedFilterView>(R.id.filterViewLuggageNumberedFilter)
        filterViewVehicleTypeMultiSelectChipsFilter =
            view.findViewById<MultiSelectChipsFilterView>(R.id.filterViewVehicleTypeMultiSelectChipsFilter)

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

        filterChain.filters.add(passengersFilter)
        filterChain.filters.add(luggageFilter)
    }

    companion object {
        const val TAG = "FilterView"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { it ->
                val behaviour = BottomSheetBehavior.from(it)
                setupFullHeight(it)
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
        filterViewPassengerNumberedFilter.filter = passengersFilter
        filterViewPassengerNumberedFilter.setTitle(getString(R.string.kh_uisdk_filter_passengers))
        filterViewPassengerNumberedFilter.icon = R.drawable.kh_uisdk_ic_passengers
        filterViewPassengerNumberedFilter.delegate = {
            presenter.callFilterChanged()
        }

        filterViewLuggageNumberedFilter.filter = luggageFilter
        filterViewLuggageNumberedFilter.setTitle(getString(R.string.kh_uisdk_filter_luggages))
        filterViewLuggageNumberedFilter.icon = R.drawable.kh_uisdk_ic_luggage
        filterViewLuggageNumberedFilter.delegate = {
            presenter.callFilterChanged()
        }
//
//        val vehicleTypeFilter = VehicleTypeFilter(ArrayList()).apply {
//            typeValues = ArrayList<String>().apply {
//                add("All")
//                add("Standard")
//                add("Berline")
//                add("Van")
//                add("Moto")
//                add("Bike")
//            }
//        }
//
//        filterViewVehicleTypeMultiSelectChipsFilter.filter = vehicleTypeFilter
//        filterViewVehicleTypeMultiSelectChipsFilter.chips = vehicleTypeFilter.typeValues
//        filterViewVehicleTypeMultiSelectChipsFilter.setTitle("Vehicle Types")
//        filterViewVehicleTypeMultiSelectChipsFilter.delegate = {
//            presenter.callFilterChanged()
//        }
//
//        filterChain.filters.add(filterViewPassengerNumberedFilter.filter as PassengersFilter)
//        filterChain.filters.add(filterViewLuggageNumberedFilter.filter as LuggageFilter)
//        filterChain.filters.add(filterViewVehicleTypeMultiSelectChipsFilter.filter as VehicleTypeFilter)
    }

    override fun setNumberOfResultsAfterFilter(size: Int) {
        quotesFilterSave?.setText(
            String.format(
                getString(R.string.kh_uisdk_filter_page_results),
                size
            )
        )
    }
}
