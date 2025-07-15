import com.yapp.convention.setNamespace

plugins {
    id("orbit.android.library")
    id("orbit.android.compose")
}

android {
    setNamespace("core.ui")
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.media)
    implementation(libs.compose.material)
    implementation(libs.orbit.core)
    implementation(libs.orbit.compose)
    implementation(libs.orbit.viewmodel)
    implementation(libs.lottie.compose)
    implementation(libs.play.services.ads)
}
