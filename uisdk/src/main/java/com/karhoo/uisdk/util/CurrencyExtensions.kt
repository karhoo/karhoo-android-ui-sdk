package com.karhoo.uisdk.util

import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale

fun Currency.intToRangedPrice(lowPrice: Int, highPrice: Int, locale: Locale = Locale.getDefault()): String {
    val lowCostString = this.formatted(lowPrice, locale, includeCurrencySymbol = true)
    val highCostString = this.formatted(highPrice, locale, includeCurrencySymbol = false)
    return "$lowCostString - $highCostString"
}

fun Currency.intToPriceNoSymbol(price: Int, locale: Locale = Locale.getDefault()): String {
    val cost = Integer.toString(price)
    val low = defaultFractionDigits

    val costHiEndIndex = if (cost.length - low > 0) cost.length - low else 0
    val costHi = if (costHiEndIndex == 0) "0" else cost.substring(0, costHiEndIndex)
    val costLow = cost.substring(costHiEndIndex)

    return String.format("%s.%s", costHi, costLow)
}

fun Currency.formatted(price: Int,
                       locale: Locale = Locale.getDefault(),
                       includeCurrencySymbol: Boolean = true): String {
    val currencyFormat: DecimalFormat = DecimalFormat.getCurrencyInstance(locale) as DecimalFormat
    currencyFormat.currency = this
    if (!includeCurrencySymbol) {
        val decimalFormatSymbols = currencyFormat.decimalFormatSymbols
        decimalFormatSymbols.currencySymbol = ""
        currencyFormat.decimalFormatSymbols = decimalFormatSymbols
    }
    currencyFormat.setMinimumFractionDigits(this.defaultFractionDigits)
    currencyFormat.setMaximumFractionDigits(this.defaultFractionDigits)
    val value = BigDecimal.valueOf(price.toLong(), this.defaultFractionDigits)
    return currencyFormat.format(value)
}
