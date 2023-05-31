package com.karhoo.uisdk.base.featureFlags
import com.karhoo.uisdk.base.FeatureFlagsModel
import java.util.Comparator

class FeatureFlagsVersionComparator : Comparator<FeatureFlagsModel> {

    // Comparator to sort the array of Strings
    override fun compare(a: FeatureFlagsModel, b: FeatureFlagsModel): Int {
        return VersionStringComparator().compare(a.version, b.version)
    }
}


