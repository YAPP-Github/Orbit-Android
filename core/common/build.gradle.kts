import com.yapp.convention.setNamespace

plugins {
    id("orbit.android.library")
    id("orbit.android.hilt")
    id("orbit.android.compose")
}

android {
    setNamespace("core.common")
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.domain)
    implementation(libs.compose.navigation)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
