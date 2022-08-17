package com.karhoo.uisdk.screen.booking.quotes.errorview

data class ErrorViewGenericReason(val title: String, val subtitle: String, val iconId: Int)
data class ErrorViewLinkedReason(
    val title: String,
    val subtitle: String,
    val subtitleLink: String,
    val linkTitle: String,
    val iconId: Int
)
