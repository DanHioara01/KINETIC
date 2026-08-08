package com.example.kinetic

import android.app.Activity
import android.content.Context
import android.util.Log
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PackageType
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.logInWith
import com.revenuecat.purchases.logOutWith
import com.revenuecat.purchases.purchaseWith
import com.revenuecat.purchases.restorePurchasesWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Modern wrapper around the RevenueCat SDK (v10+), including Paywall and Customer Center support.
 *
 * Configure in RevenueCat Dashboard:
 *   - App: com.example.kinetic  (Android / Google Play)
 *   - Entitlement: "Kinetic Pro"  (name must match BillingProducts.ENTITLEMENT_KINETIC_PRO)
 *   - Products:  monthly, yearly, lifetime  (Google Play Console product IDs)
 *   - Offering: "default" (contains the packages above)
 *   - Paywall: design in RevenueCat Paywalls editor (optional; custom UI also supported)
 *   - Customer Center: enable in RevenueCat → Project Settings → Customer Center
 */
class RevenueCatManager(private val appContext: Context) {

    private val _customerInfo = MutableStateFlow<CustomerInfo?>(null)
    val customerInfo: StateFlow<CustomerInfo?> = _customerInfo.asStateFlow()

    private val _offerings = MutableStateFlow<Offerings?>(null)
    val offerings: StateFlow<Offerings?> = _offerings.asStateFlow()

    @Volatile private var configured = false

    // ----------------------------------------------------------------
    //  Initialization
    // ----------------------------------------------------------------

    fun initialize(apiKey: String) {
        if (configured || apiKey.isBlank()) return

        Purchases.logLevel = LogLevel.DEBUG  // use WARN in release
        Purchases.configure(
            PurchasesConfiguration.Builder(appContext, apiKey).build()
        )
        configured = true

        // Listen for real-time customer info updates (e.g. renewal, cancellation)
        Purchases.sharedInstance.updatedCustomerInfoListener =
            UpdatedCustomerInfoListener { info -> _customerInfo.value = info }

        refreshOfferings()
        refreshCustomerInfo()
    }

    // ----------------------------------------------------------------
    //  User management
    // ----------------------------------------------------------------

    /** Associate the RevenueCat anonymous id with the app's logged-in user id. */
    fun logIn(userId: String) {
        if (!configured) return
        Purchases.sharedInstance.logInWith(
            appUserID = userId,
            onError = { Log.w(TAG, "logIn error: ${it.message}") },
            onSuccess = { info, created ->
                _customerInfo.value = info
                Log.d(TAG, "logIn ok, new user=$created")
            }
        )
    }

    /**
     * Suspending variant of [logIn]. Awaits the RevenueCat login callback so the caller
     * can safely read the NEW user's entitlement afterwards — without risking the previous
     * user's cached CustomerInfo leaking onto the wrong account.
     * Returns the new [CustomerInfo], or null if RevenueCat is not configured / login failed.
     */
    suspend fun logInSuspend(userId: String): CustomerInfo? {
        if (!configured) return null
        return suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.logInWith(
                appUserID = userId,
                onError = { Log.w(TAG, "logIn error: ${it.message}"); cont.resume(null) },
                onSuccess = { info, _ ->
                    _customerInfo.value = info
                    cont.resume(info)
                }
            )
        }
    }

    fun logOut() {
        if (!configured) return
        Purchases.sharedInstance.logOutWith(
            onError = { Log.w(TAG, "logOut error: ${it.message}") },
            onSuccess = { info -> _customerInfo.value = info }
        )
    }

    // ----------------------------------------------------------------
    //  Customer Info
    // ----------------------------------------------------------------

    fun refreshCustomerInfo() {
        if (!configured) return
        Purchases.sharedInstance.getCustomerInfoWith(
            onError = { Log.w(TAG, "refreshCustomerInfo error: ${it.message}") },
            onSuccess = { info -> _customerInfo.value = info }
        )
    }

    /** Check if the current user has an active "Kinetic Pro" entitlement. */
    fun isKineticProActive(): Boolean {
        val info = _customerInfo.value ?: return false
        return info.entitlements[BillingProducts.ENTITLEMENT_KINETIC_PRO]?.isActive == true
    }

    // ----------------------------------------------------------------
    //  Offerings & Products
    // ----------------------------------------------------------------

    fun refreshOfferings() {
        if (!configured) return
        Purchases.sharedInstance.getOfferingsWith(
            onError = { Log.w(TAG, "offerings error: ${it.message}"); _offerings.value = null },
            onSuccess = { offerings ->
                _offerings.value = offerings
                Log.d(TAG, "offerings loaded: ${offerings.current?.availablePackages?.size} packages")
            }
        )
    }

    /** Build display options for the custom pricing screen from the current offering. */
    fun buildPricingOptions(): List<PricingOption> {
        val offering = _offerings.value?.current ?: return emptyList()
        return offering.availablePackages.mapNotNull { pkg ->
            val tier = when (pkg.packageType) {
                PackageType.MONTHLY -> SubscriptionTier.PREMIUM_MONTHLY
                PackageType.ANNUAL -> SubscriptionTier.PREMIUM_ANNUAL
                PackageType.LIFETIME -> SubscriptionTier.PRO_LIFETIME
                else -> when {
                    pkg.product.id.contains("annual", true) || pkg.product.id.contains("yearly", true) -> SubscriptionTier.PREMIUM_ANNUAL
                    pkg.product.id.contains("lifetime", true) -> SubscriptionTier.PRO_LIFETIME
                    pkg.product.id.contains("monthly", true) -> SubscriptionTier.PREMIUM_MONTHLY
                    else -> return@mapNotNull null
                }
            }
            PricingOption(
                tier = tier,
                priceText = pkg.product.price.formatted,
                rcPackage = pkg
            )
        }
    }

    // ----------------------------------------------------------------
    //  Purchase
    // ----------------------------------------------------------------

    suspend fun purchase(activity: Activity, pkg: Package): PurchaseResult {
        if (!configured) return PurchaseResult.Error("Billing not configured")
        return suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.purchaseWith(
                PurchaseParams.Builder(activity, pkg).build(),
                onError = { error, userCancelled ->
                    Log.w(TAG, "purchase error: ${error.message}, cancelled=$userCancelled")
                    if (userCancelled) cont.resume(PurchaseResult.Cancelled)
                    else cont.resume(PurchaseResult.Error(error.message))
                },
                onSuccess = { _, info ->
                    _customerInfo.value = info
                    Log.d(TAG, "purchase success, entitlements: ${info.entitlements.active}")
                    cont.resume(PurchaseResult.Success(info))
                }
            )
        }
    }

    // ----------------------------------------------------------------
    //  Restore
    // ----------------------------------------------------------------

    suspend fun restore(): PurchaseResult {
        if (!configured) return PurchaseResult.Error("Billing not configured")
        return suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.restorePurchasesWith(
                onError = { error ->
                    Log.w(TAG, "restore error: ${error.message}")
                    cont.resume(PurchaseResult.Error(error.message))
                },
                onSuccess = { info ->
                    _customerInfo.value = info
                    Log.d(TAG, "restore success, entitlements: ${info.entitlements.active}")
                    cont.resume(PurchaseResult.Success(info))
                }
            )
        }
    }

    // ----------------------------------------------------------------
    //  Mapping helpers
    // ----------------------------------------------------------------

    companion object {
        private const val TAG = "RevenueCat"

        /**
         * Maps a RevenueCat [CustomerInfo] to a local [UserSubscriptionEntity].
         * Used to reconcile Room right after a successful purchase/restore, before the
         * server webhook has propagated to Firestore.
         */
        fun mapToEntity(userId: String, info: CustomerInfo): UserSubscriptionEntity {
            val pro = info.entitlements[BillingProducts.ENTITLEMENT_KINETIC_PRO]

            return if (pro != null && pro.isActive) {
                val productId = pro.productIdentifier.lowercase()
                val tier = when {
                    productId.contains("lifetime") -> SubscriptionTier.PRO_LIFETIME
                    productId.contains("annual") || productId.contains("yearly") -> SubscriptionTier.PREMIUM_ANNUAL
                    else -> SubscriptionTier.PREMIUM_MONTHLY
                }
                val expiryDate = when (tier) {
                    SubscriptionTier.PRO_LIFETIME -> null
                    SubscriptionTier.PREMIUM_MONTHLY -> (pro.expirationDate?.time ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000))
                    SubscriptionTier.PREMIUM_ANNUAL -> (pro.expirationDate?.time ?: (System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000))
                    SubscriptionTier.FREE -> null
                }
                UserSubscriptionEntity(
                    userId = userId,
                    subscriptionType = tier.id,
                    subscriptionStatus = "ACTIVE",
                    expiryDate = expiryDate,
                    isLifetime = tier == SubscriptionTier.PRO_LIFETIME,
                    revenueCatId = info.originalAppUserId,
                    lastSyncedAt = System.currentTimeMillis()
                )
            } else {
                UserSubscriptionEntity(
                    userId = userId,
                    subscriptionType = SubscriptionTier.FREE.id,
                    subscriptionStatus = "ACTIVE",
                    expiryDate = null,
                    isLifetime = false,
                    revenueCatId = info.originalAppUserId,
                    lastSyncedAt = System.currentTimeMillis()
                )
            }
        }
    }
}

sealed class PurchaseResult {
    data class Success(val customerInfo: CustomerInfo) : PurchaseResult()
    data class Error(val message: String?) : PurchaseResult()
    data object Cancelled : PurchaseResult()
}
