package com.karhoo.uisdk.base.featureFlags

import java.util.Comparator

class VersionStringComparator : Comparator<String> {

    private fun getVersionsComponents(a: String): List<Int> {
        return a.split(".").toTypedArray().map { it.toInt() }
    }

    override fun compare(a: String, b: String): Int {
        // Stores the numerical subStrings
        val va = getVersionsComponents(a)
        val vb = getVersionsComponents(b)

        for (i in 0 until Math.min(va.size, vb.size)) {
            if (va[i] > vb[i]) {
                return 1
            } else if (va[i] < vb[i]) {
                return -1
            }
        }
        return when {
            va.size < vb.size -> -1
            else -> 0
        }
    }
}


