package com.yapp.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureHiltAndroid() {
    with(pluginManager) {
        apply("dagger.hilt.android.plugin")
        apply("com.google.devtools.ksp")
    }

    dependencies {
        "implementation"(libs.findLibrary("hilt.android").get())
        "ksp"(libs.findLibrary("hilt.android.compiler").get())
        "ksp"(libs.findLibrary("androidx-hilt-compiler").get())
        "implementation"(libs.findLibrary("hilt-navigation-compose").get())
        "implementation"(libs.findLibrary("hilt-worker").get())
    }
}

internal class HiltAndroidPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            configureHiltAndroid()
        }
    }
}
