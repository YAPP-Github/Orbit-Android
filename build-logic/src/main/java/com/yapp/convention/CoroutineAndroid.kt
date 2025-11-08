package com.yapp.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureCoroutine() {
    addImplementation("kotlinx.coroutines.core")
    addImplementation("kotlinx.coroutines.android")
}

private fun Project.addImplementation(libraryKey: String) {
    dependencies {
        "implementation"(libs.findLibrary(libraryKey).get())
    }
}
