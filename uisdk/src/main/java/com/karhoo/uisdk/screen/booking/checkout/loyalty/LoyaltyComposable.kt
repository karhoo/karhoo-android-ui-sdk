package com.karhoo.uisdk.screen.booking.checkout.loyalty

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoyaltyComposable(
    balance: String,
    earnTitle: String,
    earnSubtitle: String,
    burnTitle: String,
    burnSubtitle: String,
    infoText: String,
    isInfoVisible: Boolean,
    isSwitchChecked: Boolean,
    onSwitchCheckedChange: (Boolean) -> Unit
) {
    Column {
        Text(text = balance, style = MaterialTheme.typography.h6)
        LoyaltyActionsContainer(
            earnTitle = earnTitle,
            earnSubtitle = earnSubtitle,
            burnTitle = burnTitle,
            burnSubtitle = burnSubtitle,
            isSwitchChecked = isSwitchChecked,
            onSwitchCheckedChange = onSwitchCheckedChange
        )
        if (isInfoVisible) {
            LoyaltyInfoLayout(infoText = infoText)
        }
    }
}

@Composable
fun LoyaltyActionsContainer(
    earnTitle: String,
    earnSubtitle: String,
    burnTitle: String,
    burnSubtitle: String,
    isSwitchChecked: Boolean,
    onSwitchCheckedChange: (Boolean) -> Unit
) {
    Column {
        LoyaltyActionLayout(title = earnTitle, subtitle = earnSubtitle)
        Divider()
        LoyaltyActionLayout(title = burnTitle, subtitle = burnSubtitle) {
            Switch(checked = isSwitchChecked, onCheckedChange = onSwitchCheckedChange)
        }
    }
}

@Composable
fun LoyaltyActionLayout(title: String, subtitle: String, trailing: @Composable (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = title, style = MaterialTheme.typography.body1)
            Text(text = subtitle, style = MaterialTheme.typography.body2)
        }
        Spacer(modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
fun LoyaltyInfoLayout(infoText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = infoText,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}