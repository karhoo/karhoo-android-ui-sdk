package com.karhoo.uisdk.util

import java.util.Currency

object CurrencyUtils {

    fun intToPrice(currency: Currency, price: Int): String {

        val cost = Integer.toString(price)
        val low = currency.defaultFractionDigits

        val (costHi, costLow) = getDisplayPrice(cost, low)

        return String.format("%s%s.%s", currency.symbol, costHi, costLow)
    }

    fun intToRangedPrice(currency: Currency, lowPrice: Int, highPrice: Int): String {
        val lowCost = lowPrice.toString()
        val highCost = highPrice.toString()
        val low = currency.defaultFractionDigits

        val (costHiPrice, costHighFraction) = getDisplayPrice(highCost, low)
        val (costLowPrice, costLowFraction) = getDisplayPrice(lowCost, low)

        return String.format("%s%s.%s - %s.%s",
                             currency.symbol,
                             costLowPrice,
                             costLowFraction,
                             costHiPrice,
                             costHighFraction)
    }

    fun intToPriceNoSymbol(currency: Currency, price: Int): String {
        val cost = Integer.toString(price)
        val low = currency.defaultFractionDigits

        val costHiEndIndex = if (cost.length - low > 0) cost.length - low else 0
        val costHi = if (costHiEndIndex == 0) "0" else cost.substring(0, costHiEndIndex)
        val costLow = cost.substring(costHiEndIndex)

        return String.format("%s.%s", costHi, costLow)
    }

    private fun getDisplayPrice(cost: String, low: Int): Pair<String, String> {
        val costHiEndIndex = if (cost.length - low > 0) cost.length - low else 0
        val costHi = if (costHiEndIndex == 0) "0" else cost.substring(0, costHiEndIndex)
        val costLow = cost.substring(costHiEndIndex, cost.length)
        return Pair(costHi, costLow)
    }

}
