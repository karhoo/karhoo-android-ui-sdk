package com.karhoo.uisdk.screen.booking.quotes.category

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.tabs.TabLayout
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.listener.SimpleTabListener
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityProvider
import com.karhoo.uisdk.util.extension.toLocalizedString
import kotlinx.android.synthetic.main.uisdk_view_category_selector.view.vehicleCategoryTabLayout

class CategorySelectorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                                     defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr), CategorySelectorMVP.View {

    private var presenter: CategorySelectorPresenter? = null

    init {
        View.inflate(context, R.layout.uisdk_view_category_selector, this)

        vehicleCategoryTabLayout.addOnTabSelectedListener(object : SimpleTabListener() {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val category = tab.tag as Category
                presenter?.setVehicleCategory(category.categoryName)
                val unavailableCategoryView = tab.customView as CategoryView?
                unavailableCategoryView?.setTabTextColor(true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                super.onTabUnselected(tab)
                val unavailableCategoryView = tab.customView as CategoryView?
                unavailableCategoryView?.setTabTextColor(false)
            }
        })
    }

    override fun setCategories(categories: List<Category>) {
        var differentTab = false
        for (i in categories.indices){
            val tab = vehicleCategoryTabLayout.getTabAt(i)
            val tag = tab?.tag
            if(categories[i] != tag){
                differentTab = true
                break
            }
        }

        if (vehicleCategoryTabLayout.tabCount <= 0 || vehicleCategoryTabLayout.tabCount !=
            categories.size || differentTab) {
            vehicleCategoryTabLayout.removeAllTabs()
            categories
                    .map {
                        vehicleCategoryTabLayout.newTab().apply {
                            customView = CategoryView(context).apply {
                                setCategoryName(it.toLocalizedString(context.applicationContext))
                                setCategoryAvailable(it.isAvailable)
                            }
                            tag = it
                        }
                    }
                    .forEach {
                        vehicleCategoryTabLayout.addTab(it)
                    }
            vehicleCategoryTabLayout.getTabAt(vehicleCategoryTabLayout.tabCount - 1)?.select()
        } else {
            updateCategories(categories)
        }
    }

    private fun updateCategories(categories: List<Category>) {

        for (i in categories.indices) {
            val tab = vehicleCategoryTabLayout.getTabAt(i)

            val unavailableCategoryView = tab?.customView as CategoryView?
            unavailableCategoryView?.apply {
                setCategoryName(categories[i].toLocalizedString(context.applicationContext))
                setCategoryAvailable(categories[i].isAvailable)
            }

            if (tab?.isSelected == true) {
                presenter?.setVehicleCategory(categories[i].categoryName)
                unavailableCategoryView?.setTabTextColor(true)
            }
        }
    }

    override fun bindViewToData(lifecycleOwner: LifecycleOwner, categoriesViewModel: CategoriesViewModel,
                                journeyDetailsStateViewModel: JourneyDetailsStateViewModel) {
        presenter = CategorySelectorPresenter(this)
        presenter?.let {
            categoriesViewModel.categories.observe(lifecycleOwner, it.subscribeToAvailableCategories())
            journeyDetailsStateViewModel.viewStates().observe(lifecycleOwner, it.subscribeToJourneyDetails())
        }
    }

    override fun bindAvailability(availabilityProvider: AvailabilityProvider) {
        presenter?.availabilityProvider = availabilityProvider
    }

    override fun hideCategories() {
        this.visibility = View.GONE
    }

    override fun showCategories() {
        this.visibility = View.VISIBLE
    }

}
