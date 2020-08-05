package com.karhoo.karhootraveller.common.matcher

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import org.junit.Assert.assertEquals

class RecyclerMatcher constructor(private val expectedCount: Int = 0) : ViewAssertion {

    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let {
            throw noViewFoundException
        } ?: run {
            val recyclerView = view as RecyclerView
            val adapter = recyclerView.adapter
            assertEquals(adapter?.itemCount, expectedCount)
        }
    }

}