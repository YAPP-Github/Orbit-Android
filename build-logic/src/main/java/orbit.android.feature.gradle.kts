import com.yapp.convention.configureHiltAndroid
import com.yapp.convention.libs

plugins {
    id("orbit.android.library")
    id("orbit.android.compose")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

configureHiltAndroid()

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))

    val libs = project.extensions.libs
    implementation(libs.findLibrary("compose-navigation").get())
    implementation(libs.findLibrary("lifecycle-viewmodel").get())
    implementation(libs.findLibrary("lifecycle-runtime").get())
    implementation(libs.findLibrary("kotlinx-collections").get())
}
