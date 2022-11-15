package com.karhoo.uisdk.screen.booking.quotes.sortview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.radiobutton.MaterialRadioButton
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.bottomSheet.MasterBottomSheetFragment
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod

class QuotesSortView : MasterBottomSheetFragment() {

    private var listener: Listener? = null

    var selectedSortMethod = MutableLiveData<SortMethod>()
    private var quotesSortByPrice : MaterialRadioButton? = null
    private var quotesSortByDriverArrival : MaterialRadioButton? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.uisdk_view_quotes_sort, container, false)

        quotesSortByDriverArrival = view?.findViewById(R.id.quotesSortByDriverArrival)
        quotesSortByPrice = view?.findViewById(R.id.quotesSortByPrice)

        val nameObserver = Observer<SortMethod> { sort ->
            if(sort == SortMethod.PRICE)
                quotesSortByPrice?.isChecked = true
            else
                quotesSortByDriverArrival?.isChecked = true
        }
        selectedSortMethod.observe(this, nameObserver)

        quotesSortByPrice?.apply { setOnClickListener { selectedSortMethod.value = SortMethod.PRICE } }
        quotesSortByDriverArrival?.apply { setOnClickListener { selectedSortMethod.value = SortMethod.ETA } }

        setupHeader(view = view, title = getString(R.string.kh_uisdk_sort_by))
        setupButton(view = view, buttonId = R.id.quotesSortBySave, text = getString(R.string.kh_uisdk_save)) {
            selectedSortMethod.value?.let { listener?.onUserChangedSortMethod(it) }
        }

        return view
    }

    companion object {
        const val TAG = "QuotesSortView"
    }

    interface Listener {

        fun onUserChangedSortMethod(sortMethod: SortMethod)

    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }
}
