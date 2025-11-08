package com.yapp.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureTestKotlin() {
    dependencies {
        // JUnit4 단위 테스트 프레임워크
        "testImplementation"(libs.findLibrary("junit4").get())
        // 코루틴 관련 테스트 도구 (TestCoroutineScope, runTest 등..)
        "testImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
        // Kotlin 기반 mock 객체 생성, 행위 검증
        "testImplementation"(libs.findLibrary("mockk").get())
    }
}
