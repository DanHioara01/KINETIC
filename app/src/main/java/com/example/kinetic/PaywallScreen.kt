package com.example.kinetic

import android.util.Log
import androidx.compose.runtime.Composable
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions

/**
 * RevenueCat Paywall composable — displays the configured paywall from RevenueCat Dashboard.
 *
 * This uses the built-in Paywall from the RevenueCat UI SDK v10.
 * Design your paywall in RevenueCat Dashboard -> Paywalls.
 *
 * @param onPurchaseCompleted Called when the user successfully purchases or restores.
 * @param onDismiss Called when the user taps the close/back button.
 */
@Composable
fun KineticPaywallScreen(
    revenueCatManager: RevenueCatManager,
    onPurchaseCompleted: (CustomerInfo) -> Unit,
    onDismiss: () -> Unit
) {
    Paywall(
        options = PaywallOptions.Builder(
            dismissRequest = { onDismiss() }
        ).setListener(
            object : PaywallListener {
                override fun onPurchaseCompleted(customerInfo: CustomerInfo, storeTransaction: com.revenuecat.purchases.models.StoreTransaction) {
                    Log.d("Paywall", "Purchase completed: ${customerInfo.entitlements.active}")
                    onPurchaseCompleted(customerInfo)
                }
                override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                    Log.d("Paywall", "Restore completed: ${customerInfo.entitlements.active}")
                    onPurchaseCompleted(customerInfo)
                }
                override fun onPurchaseError(error: com.revenuecat.purchases.PurchasesError) {
                    Log.w("Paywall", "Purchase error: ${error.message}")
                }
                override fun onPurchaseCancelled() {
                    Log.d("Paywall", "Purchase cancelled")
                }
            }
        ).build()
    )
}
