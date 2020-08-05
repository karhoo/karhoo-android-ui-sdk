package com.karhoo.uisdk.screen.booking.drawer

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R

class BookingDrawerView @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet? = null,
                                                  defStyle: Int = 0) : DrawerLayout(context, attrs, defStyle), NavigationView.OnNavigationItemSelectedListener {

    private var drawerToggle: ActionBarDrawerToggle? = null

    init {
        checkIfDrawerIsOpenOnStart()
    }

    private fun checkIfDrawerIsOpenOnStart() {
        if (isDrawerOpen(GravityCompat.START)) {
            closeDrawer(GravityCompat.START)
        }
    }

    fun setToggleToolbar(toolbar: Toolbar, actionBar: ActionBar) {
        drawerToggle = ActionBarDrawerToggle(context as Activity,
                                             this,
                                             toolbar,
                                             R.string.drawer_open,
                                             R.string.drawer_closed)

        addDrawerListener(drawerToggle!!)
        actionBar.setDisplayHomeAsUpEnabled(true)
        drawerToggle?.isDrawerIndicatorEnabled = true
        drawerToggle?.syncState()
        actionBar.title = ""
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val menuHandler = KarhooUISDK.menuHandler
        menuHandler?.onNavigationItemSelected(context, item)

        closeDrawer(GravityCompat.START)

        return true
    }

    fun closeIfOpen(): Boolean {
        if (isDrawerOpen(GravityCompat.START)) {
            closeDrawer(GravityCompat.START)
            return false
        }
        return true
    }

}
