package com.karhoo.uisdk.screen.address.search

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.widget.LinearLayout
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.listener.SimpleTextWatcher
import com.karhoo.uisdk.screen.address.domain.AddressSearchProvider
import kotlinx.android.synthetic.main.uisdk_view_address_search.view.clearSearchButtonIcon
import kotlinx.android.synthetic.main.uisdk_view_address_search.view.searchInput

class AddressSearchView @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet? = null,
                                                  defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), AddressSearchMVP.View {

    private var presenter: AddressSearchPresenter? = null
    var actions: Actions? = null

    init {
        inflate(context, R.layout.uisdk_view_address_search, this)
        initialiseListeners()
    }

    override fun setHint(hint: String) {
        searchInput.hint = hint
    }

    override fun setAddressSearchProvider(addressProvider: AddressSearchProvider) {
        presenter = AddressSearchPresenter(this, addressProvider)
    }

    override fun clearSearch() {
        searchInput.text.clear()
    }

    override fun showRecents() {
        actions?.showRecents()
    }

    override fun showResults() {
        actions?.showResults()
    }

    private fun initialiseListeners() {
        clearSearchButtonIcon.setOnClickListener { presenter?.onClearSearch() }
        toggleClearButton(false)
        searchInput.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(editable: Editable) {
                val searchTerm = editable.toString()
                toggleClearButton(!searchTerm.isNullOrEmpty())
                presenter?.searchUpdated(searchTerm)
            }
        })
    }

    private fun toggleClearButton(enabled: Boolean) {
        clearSearchButtonIcon.alpha = if (enabled) 1.0f else 0.2f
        clearSearchButtonIcon.isClickable = enabled
    }

    interface Actions {
        fun showRecents()

        fun showResults()
    }

}
