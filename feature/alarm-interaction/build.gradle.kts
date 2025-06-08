import com.yapp.convention.setNamespace

plugins {
    id("orbit.android.feature")
}

android {
    setNamespace("feature.alarm.interaction")
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.common)
    implementation(projects.core.analytics)
    implementation(projects.core.alarm)
    implementation(projects.core.media)
    implementation(projects.domain)
    implementation(projects.core.datastore)
    implementation(libs.orbit.core)
    implementation(libs.orbit.compose)
    implementation(libs.orbit.viewmodel)
    implementation(libs.androidx.material.android)
    implementation(libs.androidx.annotation)
    implementation(libs.play.services.ads)
    implementation(libs.kotlinx.serialization.json)
}
