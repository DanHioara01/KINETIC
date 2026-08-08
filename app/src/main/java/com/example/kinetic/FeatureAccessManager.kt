package com.example.kinetic

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Access-control interactor. Exposes reactive access checks per [PremiumFeature].
 * Observe from Compose with collectAsState / collectAsStateWithLifecycle.
 */
class FeatureAccessManager(
    private val subscriptionRepository: SubscriptionRepository
) {
    /** Reactive access check for a single feature. Admins get full access. */
    fun hasAccess(userId: String, feature: PremiumFeature, email: String? = null): Flow<Boolean> =
        subscriptionRepository.observeSubscription(userId).map { sub ->
            if (AdminManager.isAdmin(email)) true else sub.hasAccess(feature)
        }

    /** One-shot access check. Admins get full access. */
    suspend fun checkAccess(userId: String, feature: PremiumFeature, email: String? = null): Boolean =
        subscriptionRepository.observeSubscription(userId).first().let { sub ->
            if (AdminManager.isAdmin(email)) true else sub.hasAccess(feature)
        }

    /** Current subscription tier as a flow. Admins see PRO_LIFETIME. */
    fun currentTier(userId: String, email: String? = null): Flow<SubscriptionTier> =
        subscriptionRepository.observeSubscription(userId).map { sub ->
            if (AdminManager.isAdmin(email)) SubscriptionTier.PRO_LIFETIME else sub.tier
        }

    /** Whether the user currently has any active premium entitlement (subscription or lifetime). Admins are premium. */
    fun isPremium(userId: String, email: String? = null): Flow<Boolean> =
        subscriptionRepository.observeSubscription(userId).map { sub ->
            if (AdminManager.isAdmin(email)) true else sub.isPremium
        }

    /** All features the user currently cannot access. Admins have none locked. */
    fun lockedFeatures(userId: String, email: String? = null): Flow<List<PremiumFeature>> =
        subscriptionRepository.observeSubscription(userId).map { sub ->
            if (AdminManager.isAdmin(email)) emptyList() else PremiumFeature.entries.filter { !sub.hasAccess(it) }
        }

    /** Full subscription snapshot flow. Admins see PRO_LIFETIME. */
    fun subscription(userId: String, email: String? = null): Flow<UserSubscription> =
        subscriptionRepository.observeSubscription(userId).map { sub ->
            if (AdminManager.isAdmin(email)) sub.copy(
                tier = SubscriptionTier.PRO_LIFETIME,
                status = "ACTIVE",
                isLifetime = true
            ) else sub
        }
}
