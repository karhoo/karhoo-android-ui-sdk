package com.karhoo.uisdk.featureFlags
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.karhoo.uisdk.base.FeatureFlags
import com.karhoo.uisdk.base.FeatureFlagsModel
import com.karhoo.uisdk.base.featureFlags.FeatureFlagsService
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class FeatureFlagsTest {

    private lateinit var instrumentationContext: Context

    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun testVersionFromList() {
        performTestForVersions(current = "2.0.1", expected = "2.0.1")
    }

    @Test
    fun testVersionMajorBetweenOther() {
        performTestForVersions(current = "7.0.1", expected = "4.0.0")
    }

    @Test
    fun testVersionMinorBetweenOther() {
        performTestForVersions(current = "2.1.2", expected = "2.0.1")
    }

    @Test
    fun testVersionGreaterThanEverything() {
        performTestForVersions(current = "100.0.0", expected = "11.1.0")
    }

    @Test
    fun testVersionLowerThanEverything() {
        performTestForVersions(current = "1.0.0", expected = null)
    }

    private fun getFeatureFlagsSet(): List<FeatureFlagsModel> {
        return listOf(
            getFeatureFlagsModel(withVersion = "2.1.4"),
            getFeatureFlagsModel(withVersion = "2.0.0"),
            getFeatureFlagsModel(withVersion = "11.1.0"),
            getFeatureFlagsModel(withVersion = "2.0.1"),
            getFeatureFlagsModel(withVersion = "2.1.10"),
            getFeatureFlagsModel(withVersion = "1.1.0"),
            getFeatureFlagsModel(withVersion = "4.0.0")
        )
        //            CORRECT ORDER:
        //            "1.1.0"
        //            "2.0.0"
        //            "2.0.1"
        //            "2.2.4"
        //            "2.1.10"
        //            "4.0.0"
        //            "11.1.0"
    }

    private fun getFeatureFlagsModel(withVersion: String): FeatureFlagsModel {
        return FeatureFlagsModel(withVersion, FeatureFlags(adyenAvailable = true, newRidePlaningScreen = true))
    }

    private fun performTestForVersions(current: String, expected: String?) {
        val mockFeatureFlagsStore = MockFeatureFlagsStore()
        val featureFlagsService = FeatureFlagsService(
            instrumentationContext,
            currentSdkVersion = current,
            featureFlagsStore = mockFeatureFlagsStore
        )
        featureFlagsService.handleFlagSets(getFeatureFlagsSet())
        assertEquals(expected, mockFeatureFlagsStore.savedModel?.version)
    }
}
