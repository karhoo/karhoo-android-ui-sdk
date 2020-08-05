package com.karhoo.uisdk.base

import android.content.Context
import android.view.MenuItem

interface MenuHandler {

    fun onNavigationItemSelected(context: Context, item: MenuItem)

}