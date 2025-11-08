import com.yapp.convention.libs

plugins {
    id("orbit.android.library")
    id("orbit.android.compose")
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))

    implementation(libs.findLibrary("compose-navigation").get())
    implementation(libs.findLibrary("lifecycle-viewmodel").get())
    implementation(libs.findLibrary("lifecycle-runtime").get())
    implementation(libs.findLibrary("kotlinx-collections").get())
}
