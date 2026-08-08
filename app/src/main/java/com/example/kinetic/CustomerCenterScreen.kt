package com.example.kinetic

import android.util.Log
import androidx.compose.runtime.Composable
import com.revenuecat.purchases.ui.revenuecatui.customercenter.CustomerCenter

/**
 * RevenueCat Customer Center — lets users manage subscriptions, cancel, change plans,
 * request refunds, etc.
 *
 * Enable in: RevenueCat Dashboard -> Project Settings -> Customer Center.
 *
 * @param onDismiss Called when the user closes the Customer Center.
 */
@Composable
fun KineticCustomerCenter(
    onDismiss: () -> Unit
) {
    CustomerCenter(
        onDismiss = { onDismiss() }
    )
}
