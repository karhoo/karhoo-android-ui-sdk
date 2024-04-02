package com.karhoo.uisdk.featureFlags

import android.content.Context
import com.karhoo.uisdk.base.FeatureFlags
import com.karhoo.uisdk.base.FeatureFlagsModel
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FeatureFlagsTest {

    private val context: Context = mock()

    @Test
    fun testVersionFromList() {
        performTestForVersions(expected = "1.12.0")
    }

    @Test
    fun testVersionMajorBetweenOther() {
        makeSureTheCorrectVersionComes(expected = "4.0.0")
    }

    @Test
    fun testVersionMinorBetweenOther() {
        makeSureTheCorrectVersionComes(expected = "2.0.1")
    }

    @Test
    fun testVersionGreaterThanEverything() {
        makeSureTheCorrectVersionComes(expected = "11.1.0")
    }

    @Test
    fun testVersionLowerThanEverything() {
        makeSureTheCorrectVersionComes(expected = null)
    }

    private fun getFeatureFlagsSet(): List<FeatureFlagsModel> {
        return listOf(
            getFeatureFlagsModel(withVersion = "1.12.0"),
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

    private fun performTestForVersions(expected: String?) {
//        val mockFeatureFlagsStore: FeatureFlagsStore = MockFeatureFlagsStore()
//        val featureFlagsService = FeatureFlagsService(
//            context = context,
//            featureFlagsStore = mockFeatureFlagsStore
//        )
//        featureFlagsService.handleFlagSets(getFeatureFlagsSet())
//        assertEquals(expected, mockFeatureFlagsStore.get()?.version)
    }

    private fun makeSureTheCorrectVersionComes(expected: String?) {
//        val mockFeatureFlagsStore: FeatureFlagsStore = MockFeatureFlagsStore()
//        val featureFlagsService = FeatureFlagsService(
//            context = context,
//            featureFlagsStore = mockFeatureFlagsStore
//        )
//        featureFlagsService.handleFlagSets(getFeatureFlagsSet())
//        assertNotEquals(expected, mockFeatureFlagsStore.get()?.version)
    }
}
