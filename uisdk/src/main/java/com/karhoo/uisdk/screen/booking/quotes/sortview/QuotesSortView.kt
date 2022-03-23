package com.karhoo.uisdk.screen.booking.quotes.sortview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.radiobutton.MaterialRadioButton
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.view.LoadingButtonView
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod

class QuotesSortView : BottomSheetDialogFragment() {

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

        val quotesSortBySave = view.findViewById<LoadingButtonView>(R.id.quotesSortBySave)
        quotesSortBySave.apply {
            setText(R.string.kh_uisdk_save)
            setOnClickListener { saveClicked() }
        }

        quotesSortByDriverArrival = view?.findViewById(R.id.quotesSortByDriverArrival)
        quotesSortByPrice = view?.findViewById(R.id.quotesSortByPrice)

        val nameObserver = Observer<SortMethod> { sort ->
            if(sort == SortMethod.PRICE)
                quotesSortByPrice?.isChecked = true
            else
                quotesSortByDriverArrival?.isChecked = true
        }
        selectedSortMethod.observe(this, nameObserver)

        val quotesSortByCloseDialog = view.findViewById<ImageButton>(R.id.quotesSortByCloseDialog)
        quotesSortByCloseDialog.apply {
            setOnClickListener { dismiss() }
        }

        quotesSortByPrice?.apply { setOnClickListener { selectedSortMethod.value = SortMethod.PRICE } }
        quotesSortByDriverArrival?.apply { setOnClickListener { selectedSortMethod.value = SortMethod.ETA } }

        return view
    }

    companion object {
        const val TAG = "QuotesSortView"
    }

    override fun getTheme(): Int {
        return R.style.KhQuoteListSortBottomSheetDialogTheme
    }

    private fun saveClicked(){
        selectedSortMethod.value?.let { listener?.onUserChangedSortMethod(it) }
        dismiss()
    }

    interface Listener {

        fun onUserChangedSortMethod(sortMethod: SortMethod)

    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }
}
