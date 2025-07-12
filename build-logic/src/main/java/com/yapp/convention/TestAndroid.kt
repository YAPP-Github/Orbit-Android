package com.yapp.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureTestAndroid() {
    configureJUnitAndroid()
    // feature 모듈에만 UI 테스트 관련 설정 적용
    if (path.startsWith(":feature:")) {
        configureComposeUiTest()
    }
}

internal fun Project.configureComposeUiTest() {
    val libs = extensions.libs
    dependencies {
        "androidTestImplementation"(libs.findLibrary("compose-ui-test-junit4").get())
        "debugImplementation"(libs.findLibrary("compose-ui-test-manifest").get())
    }
}

@Suppress("UnstableApiUsage")
internal fun Project.configureJUnitAndroid() {
    androidExtension.apply {
        testOptions { unitTests.all { it.useJUnitPlatform() } }
        defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }

        val libs = extensions.libs
        dependencies {
            "androidTestImplementation"(libs.findLibrary("androidx-test-ext-junit").get())
            "androidTestImplementation"(libs.findLibrary("androidx-test-runner").get())
        }
    }
}
