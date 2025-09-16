package com.yapp.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun Project.configureComposeAndroid() {
    with(plugins) {
        apply("org.jetbrains.kotlin.plugin.compose")
    }

    val libs = extensions.libs
    androidExtension.apply {
        dependencies {
            val bom = libs.findLibrary("compose.bom").get()
            add("implementation", platform(bom))

            add("implementation", libs.findLibrary("activity.compose").get())

            add("implementation", libs.findLibrary("compose.material3").get())
            add("implementation", libs.findLibrary("compose.ui").get())
            add("implementation", libs.findLibrary("compose.ui.tooling.preview").get())
            add("debugImplementation", libs.findLibrary("compose.ui.tooling").get())
        }
    }

    extensions.getByType<ComposeCompilerGradlePluginExtension>().apply {
        enableStrongSkippingMode.set(true)
        includeSourceInformation.set(true)
    }
}
