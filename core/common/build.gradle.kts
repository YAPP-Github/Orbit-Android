import com.yapp.convention.setNamespace

plugins {
    id("orbit.android.library")
    id("orbit.android.hilt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    setNamespace("core.common")
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.domain)
    implementation(libs.kotlinx.serialization.json)
}
