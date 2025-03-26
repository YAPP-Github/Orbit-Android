import com.yapp.convention.setNamespace

plugins {
    id("orbit.android.library")
    id("orbit.android.hilt")
}

android {
    setNamespace("core.remoteconfig")
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
}
