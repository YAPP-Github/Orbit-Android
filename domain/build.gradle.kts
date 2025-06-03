import com.yapp.convention.setNamespace

plugins {
    id("orbit.android.library")
    id("kotlin-parcelize")
}

android {
    setNamespace("domain")
}

dependencies {
    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)
}
