package com.karhoo.uisdk.base.featureFlags

import java.util.Comparator

class VersionStringComparator : Comparator<String> {

    // Compares two Strings
    private fun check(a: String, b: String): Int {
        val al = a.length
        val bl = b.length

        var i = 0
        var j = 0
        while (i < al && j < bl) {
            if (a[i] == b[j]) {
                i++
                j++
            } else if (a[i] > b[j]) {
                return 1
            } else {
                return -1
            }
        }

        if (i == al && j == bl) {
            return 0
        }
        if (i == al) {
            return -1
        }
        return 1
    }

    // Function to split Strings based on dots
    private fun getTokens(a: String): Array<String> {
        return a.split(".").toTypedArray()
    }

    // Comparator to sort the array of Strings
    override fun compare(a: String, b: String): Int {

        // Stores the numerical subStrings
        val va = getTokens(a)
        val vb = getTokens(b)

        // Iterate up to length of minimum
        // of the two Strings
        for (i in 0 until Math.min(va.size, vb.size)) {

            // Compare each numerical subString
            // of the two Strings
            val countCheck = check(va[i], vb[i])

            if (countCheck == -1) {
                return -1
            } else if (countCheck == 1) {
                return 1
            }
        }

        return when {
            va.size < vb.size -> -1
            else -> 1
        }
    }
}


