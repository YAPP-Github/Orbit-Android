package com.yapp.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

internal fun Project.configureTestCoverage() {
    pluginManager.apply("jacoco")

    val libs = extensions.libs
    extensions.configure<JacocoPluginExtension> {
        toolVersion = libs.findVersion("jacoco").get().toString()
    }

    // 모든 유닛 테스트에 Jacoco 설정 적용
    tasks.withType<Test>().configureEach {
        extensions.configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }

    // Android 모듈이면 커버리지 설정 추가
    extensions.findByType(ApplicationExtension::class.java)?.buildTypes?.configureEach {
        enableUnitTestCoverage = true
    }

    extensions.findByType(LibraryExtension::class.java)?.buildTypes?.configureEach {
        enableUnitTestCoverage = true
    }

    // 커버리지 리포트 Task 등록
    tasks.register("generateTestCoverageReport") {
        group = "verification"
        description = "Run unit tests and generate coverage report."

        dependsOn("testDebugUnitTest")
        dependsOn("createDebugUnitTestCoverageReport")
    }

    // .exec 파일 없을 경우 createDebugUnitTestCoverageReport task 스킵
    tasks.matching { it.name == "createDebugUnitTestCoverageReport" }.configureEach {
        onlyIf {
            val execFile = layout.buildDirectory
                .file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
                .get().asFile
            execFile.exists()
        }

        (this as? JacocoReport)?.reports?.xml?.required?.set(true)
    }
}
