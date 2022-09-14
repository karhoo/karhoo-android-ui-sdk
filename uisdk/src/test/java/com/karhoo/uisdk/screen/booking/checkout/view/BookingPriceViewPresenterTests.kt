package com.karhoo.uisdk.screen.booking.checkout.view

import android.content.Context
import com.karhoo.sdk.api.model.*
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.checkout.component.views.BookingPriceViewContract
import com.karhoo.uisdk.screen.booking.checkout.component.views.BookingPriceViewPresenter
import com.karhoo.uisdk.util.extension.toLocalisedString
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class BookingPriceViewPresenterTests {
    private lateinit var priceViewPresenter: BookingPriceViewPresenter
    private var context: Context = mock()
    private var view: BookingPriceViewContract.View = mock()
    private val quote: Quote = mock()
    private lateinit var quoteSource: QuoteSource
    private val price: QuotePrice = QuotePrice(highPrice = 10, lowPrice = 3, currencyCode = "GBP")
    private val currency: Currency = Currency.getInstance(price.currencyCode)

    @Before
    fun setUp() {
        priceViewPresenter = BookingPriceViewPresenter()
        priceViewPresenter.attachView(view)
    }

    @Test
    fun `When formatting the price in case of a FLEET quote, the price is formatted correctly `() {
        whenever(quote.quoteSource).thenReturn(QuoteSource.FLEET)
        whenever(quote.price).thenReturn(price)
        priceViewPresenter.formatPriceText(quote, currency, Locale.UK)
        verify(view).setPriceText(FLEET_PRICE_RESULT)
    }

    @Test
    fun `When formatting the price in case of a fleet quote, the price is formatted correctly `() {
        whenever(quote.quoteSource).thenReturn(QuoteSource.MARKET)
        whenever(quote.price).thenReturn(price)
        priceViewPresenter.formatPriceText(quote, currency, Locale.UK)
        verify(view).setPriceText(MARKET_PRICE_RESULT)
    }

    @Test
    fun `When formatting the pickup type in case of MEET_AND_GREET, the meet and greet string is passed to the view succesfully`() {
        whenever(quote.pickupType).thenReturn(PickupType.MEET_AND_GREET)
        whenever(view.getString(R.string.kh_uisdk_price_meet_and_greet)).thenReturn(MEET_AND_GREET)
        priceViewPresenter.formatPickUpType(quote)

        verify(view).setPickUpType(quote.pickupType)
    }

    @Test
    fun `When formatting the pickup type in case of CURBSIDE, the pickup type string is null`() {
        whenever(quote.pickupType).thenReturn(PickupType.CURBSIDE)
        whenever(view.getString(any())).thenReturn(null)
        priceViewPresenter.formatPickUpType(quote)

        verify(view).setPickUpType(quote.pickupType)
    }

    @Test
    fun `When formatting the pickup type in case of DEFAULT, the pickup type string is null`() {
        whenever(quote.pickupType).thenReturn(PickupType.DEFAULT)
        whenever(context.getString(any())).thenReturn("")
        priceViewPresenter.formatPickUpType(quote)

        verify(view).setPickUpType(quote.pickupType)
        Assert.assertEquals(quote.pickupType?.toLocalisedString(context = context), "")
    }

    @Test
    fun `When formatting the pickup type in case of NOT_SET, the pickup type string is null`() {
        whenever(quote.pickupType).thenReturn(PickupType.NOT_SET)
        whenever(context.getString(any())).thenReturn("")
        priceViewPresenter.formatPickUpType(quote)

        verify(view).setPickUpType(quote.pickupType)
        Assert.assertEquals(quote.pickupType?.toLocalisedString(context = context), "")
    }

    @Test
    fun `When formatting the pickup type in case of STANDBY, the pickup type string is null`() {
        whenever(quote.pickupType).thenReturn(PickupType.STANDBY)
        whenever(context.getString(R.string.kh_uisdk_pickup_type_standby)).thenReturn(STANDBY)
        priceViewPresenter.formatPickUpType(quote)

        verify(view).setPickUpType(quote.pickupType)
        Assert.assertEquals(quote.pickupType?.toLocalisedString(context = context), STANDBY)
    }

    @Test
    fun `When formatting the price type, if the quote type is ESTIMATED then the pricing type and info text will be correct`() {
        whenever(quote.quoteType).thenReturn(QuoteType.ESTIMATED)
        whenever(view.getString(R.string.kh_uisdk_estimated_fare)).thenReturn(ESTIMATED)
        whenever(view.getString(R.string.kh_uisdk_price_info_text_estimated)).thenReturn(ESTIMATED_INFO_TEXT)
        priceViewPresenter.formatQuoteType(quote)

        verify(view).setQuoteTypeDetails(quote.quoteType)
    }

    @Test
    fun `When formatting the price type, if the quote type is METERED then the pricing type and info text will be correct`() {
        whenever(quote.quoteType).thenReturn(QuoteType.METERED)
        whenever(view.getString(R.string.kh_uisdk_metered)).thenReturn(METERED)
        whenever(view.getString(R.string.kh_uisdk_price_info_text_metered)).thenReturn(METERED_INFO_TEXT)
        priceViewPresenter.formatQuoteType(quote)

        verify(view).setQuoteTypeDetails(quote.quoteType)
    }

    @Test
    fun `When formatting the price type, if the quote type is FIXED then the pricing type and info text will be correct`() {
        whenever(quote.quoteType).thenReturn(QuoteType.FIXED)
        whenever(view.getString(R.string.kh_uisdk_fixed_fare)).thenReturn(FIXED)
        whenever(view.getString(R.string.kh_uisdk_price_info_text_fixed)).thenReturn(FIXED_INFO_TEXT)
        priceViewPresenter.formatQuoteType(quote)

        verify(view).setQuoteTypeDetails(quote.quoteType)
    }

    companion object {
        private const val FLEET_PRICE_RESULT = "£0.10"
        private const val MARKET_PRICE_RESULT = "£0.03 - 0.10"
        private const val MEET_AND_GREET = "Meet and greet"
        private const val STANDBY = "STANDBY"
        private const val ESTIMATED = "ESTIMATED"
        private const val METERED = "METERED"
        private const val FIXED = "FIXED"
        private const val ESTIMATED_INFO_TEXT = "ESTIMATED_INFO_TEXT"
        private const val METERED_INFO_TEXT = "METERED_INFO_TEXT"
        private const val FIXED_INFO_TEXT = "FIXED_INFO_TEXT"
    }
}
