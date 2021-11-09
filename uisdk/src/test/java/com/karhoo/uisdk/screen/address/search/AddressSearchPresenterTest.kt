package com.karhoo.uisdk.screen.address.search

import com.karhoo.uisdk.screen.address.domain.AddressSearchProvider
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AddressSearchPresenterTest {

    internal lateinit var presenter: AddressSearchPresenter

    internal var view: AddressSearchMVP.View = mock()
    internal var searchProvider: AddressSearchProvider = mock()

    @Before
    fun setUp() {
        presenter = AddressSearchPresenter(view = view, addressProvider = searchProvider)
    }

    /**
     * Given    A user types in a address
     * When     A new character is typed
     * Then     The address search provider should be called with the search string
     */
    @Test
    fun newCharacterTypedCallsTheAddressSearchWithTheNewQuery() {
        presenter.searchUpdated("hallo der")
        verify(searchProvider).setSearchQuery("hallo der")
    }

    /**
     * Given    A user presses clear on the search field
     * When     A user is entering a new address
     * Then     The view should be asked to clear the text field
     */
    @Test
    fun onClearSearch() {
        presenter.onClearSearch()
        verify(view, atLeastOnce()).clearSearch()
    }

    /**
     * Given:   no previous calls to searchUpdated
     * When:    search updated called with non empty string
     * Then:    show results called
     */
    @Test
    fun showResultsCalledOnFirstNonEmptySearch() {
        presenter.searchUpdated("H")
        verify(view, atLeastOnce()).showResults()
    }

    /**
     * Given:   previous non empty call to searchUpdated
     * When:    search updated called with non empty string
     * Then:    no additional calls to show results
     */
    @Test
    fun showResultsNotCalledAdditionalTimes() {
        presenter.searchUpdated("H")
        presenter.searchUpdated("Ho")
        verify(view, atLeastOnce()).showResults()
    }

    /**
     * Given:   previous non empty call to searchUpdated
     * When:    search updated called with empty string
     * Then:    show recents
     */
    @Test
    fun showRecentsWhenSearchEmpty() {
        presenter.searchUpdated("Holborn")
        presenter.searchUpdated("")
        verify(view, atLeastOnce()).showRecents()
    }

    /**
     * Given:   previous non empty call to searchUpdated
     * When:    clear search selected
     * Then:    show recents
     */
    @Test
    fun showRecentsWhenClearSearch() {
        presenter.searchUpdated("Holborn")
        presenter.onClearSearch()
        verify(view, atLeastOnce()).showRecents()
    }

    /**
     * Given: previous non empty call to searchUpdated
     * When: search updated called with non empty string
     * Then: session token should be the same
     */
    @Test
    fun showResultsWithSameSessionToken() {
        presenter.searchUpdated("Holborn")
        val sessionToken = searchProvider.getSessionToken()
        presenter.searchUpdated("Holborn 2")
        Assert.assertEquals(searchProvider.getSessionToken(), sessionToken)
    }
}
