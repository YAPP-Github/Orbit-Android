import com.yapp.convention.setNamespace

plugins {
    id("orbit.android.feature")
}

android {
    setNamespace("feature.navigator")
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.analytics)
    implementation(libs.orbit.core)
    implementation(libs.orbit.compose)
    implementation(libs.orbit.viewmodel)
    implementation(libs.kotlin.reflect)
    implementation(projects.feature.home)
    implementation(projects.feature.alarmInteraction)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.mission)
    implementation(projects.feature.fortune)
    implementation(projects.feature.setting)
    implementation(projects.feature.splash)
    implementation(projects.feature.webview)
}
