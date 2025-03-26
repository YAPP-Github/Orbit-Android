import com.yapp.convention.setNamespace

plugins {
    id("orbit.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    setNamespace("data")
}

dependencies {
    implementation(projects.domain)
    implementation(projects.core.network)
    implementation(projects.core.datastore)
    implementation(projects.core.media)
    implementation(projects.core.remoteconfig)

    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.datastore)
}
