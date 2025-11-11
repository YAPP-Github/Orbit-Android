plugins {
    id("orbit.android.application")
    id("orbit.android.compose")
    alias(libs.plugins.google.service)
    alias(libs.plugins.firebase.app.distribution)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.android.application)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.yapp.orbit"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            isDebuggable = true
        }

        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }

    lint {
        disable.add("NullSafeMutableLiveData")
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.analytics)
    implementation(projects.core.buildconfig)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.data)
    implementation(projects.feature.splash)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.home)
    implementation(projects.feature.alarmInteraction)
    implementation(projects.feature.fortune)
    implementation(projects.feature.mission)
    implementation(projects.feature.setting)
    implementation(projects.feature.webview)

    implementation(libs.compose.material)
    implementation(libs.kotlin.reflect)
    implementation(libs.hilt.worker)
    implementation(libs.androidx.work.runtime)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.play.services.ads)

    implementation(libs.androidx.profileinstaller)
    baselineProfile(projects.baselineprofile)
}
