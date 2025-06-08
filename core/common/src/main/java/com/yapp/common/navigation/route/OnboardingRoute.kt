package com.yapp.common.navigation.route

import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
data object OnboardingBaseRoute

sealed class OnboardingDestination {
    @Serializable
    data object Explain : OnboardingDestination()

    @Serializable
    data object AlarmTimeSelection : OnboardingDestination()

    @Serializable
    data object Birthday : OnboardingDestination()

    @Serializable
    data object TimeOfBirth : OnboardingDestination()

    @Serializable
    data object Name : OnboardingDestination()

    @Serializable
    data object Gender : OnboardingDestination()

    @Serializable
    data object Access : OnboardingDestination()

    @Serializable
    data object Complete1 : OnboardingDestination()

    @Serializable
    data object Complete2 : OnboardingDestination()

    companion object {
        val routes: List<KClass<out OnboardingDestination>> = listOf(
            Explain::class,
            AlarmTimeSelection::class,
            Birthday::class,
            TimeOfBirth::class,
            Name::class,
            Gender::class,
            Access::class,
            Complete1::class,
            Complete2::class,
        )

        fun getNextRouteForStep(currentStep: Int): KClass<out OnboardingDestination>? {
            val nextRoute = routes.getOrNull(currentStep + 1)
            return nextRoute
        }
    }
}
