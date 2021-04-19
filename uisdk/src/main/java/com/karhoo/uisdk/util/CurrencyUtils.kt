package com.karhoo.uisdk.util

import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {

    fun getFormattedPrice(currency: String?, price: Int): String {
        return if (currency.isNullOrEmpty()) "" else {
            val currency = Currency.getInstance(currency)
            currencyAwareFormatting(price.toLong(), currency)
        }
    }

    fun intToPrice(currency: Currency, price: Int): String {
        return currencyAwareFormatting(price.toLong(), currency)
    }

    fun intToRangedPrice(currency: Currency, lowPrice: Int, highPrice: Int): String {
        val lowCostString = currencyAwareFormatting(lowPrice.toLong(), currency, includeCurrencySymbol = true)
        val highCostString = currencyAwareFormatting(highPrice.toLong(), currency, includeCurrencySymbol = false)
        return String.format("%s - %s", lowCostString, highCostString)
    }

    fun intToPriceNoSymbol(currency: Currency, price: Int): String {
        return currencyAwareFormatting(price.toLong(), currency, includeCurrencySymbol = false)
    }

    private fun currencyAwareFormatting(amount: Long,
                                        currency: Currency,
                                        includeCurrencySymbol: Boolean = true,
                                        locale: Locale = Locale.getDefault()): String {
        val currencyFormat: DecimalFormat = DecimalFormat.getCurrencyInstance(locale) as DecimalFormat
        currencyFormat.currency = currency
        if (!includeCurrencySymbol) {
            val decimalFormatSymbols = currencyFormat.decimalFormatSymbols
            decimalFormatSymbols.currencySymbol = ""
            currencyFormat.decimalFormatSymbols = decimalFormatSymbols
        }
        currencyFormat.setMinimumFractionDigits(currency.defaultFractionDigits)
        currencyFormat.setMaximumFractionDigits(currency.defaultFractionDigits)
        val value = BigDecimal.valueOf(amount, currency.defaultFractionDigits)
        return currencyFormat.format(value)
    }
}
