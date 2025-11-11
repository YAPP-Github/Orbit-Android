import com.yapp.convention.setNamespace

plugins {
    id("orbit.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    setNamespace("data")
}

dependencies {
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(projects.core.datastore)

    implementation(projects.domain)
    implementation(projects.core.remoteconfig)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.logging)
}
