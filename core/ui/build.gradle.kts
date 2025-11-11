import com.yapp.convention.setNamespace

plugins {
    id("orbit.android.library")
    id("orbit.android.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    setNamespace("core.ui")
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.media)
    implementation(projects.domain)
    implementation(libs.compose.material)
    implementation(libs.compose.navigation)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.orbit.core)
    implementation(libs.orbit.compose)
    implementation(libs.orbit.viewmodel)
    implementation(libs.lottie.compose)
    implementation(libs.play.services.ads)
}
