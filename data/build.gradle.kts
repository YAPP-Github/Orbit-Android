import com.yapp.convention.setNamespace

plugins {
    id("orbit.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    setNamespace("data")
}

dependencies {
    api(projects.core.network)
    api(projects.core.database)
    api(projects.core.datastore)

    implementation(projects.domain)
    implementation(projects.core.media)
    implementation(projects.core.remoteconfig)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.logging)
}
