package com.yapp.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies


internal fun Project.configureTestAndroid() {
    // feature 모듈에만 테스트 관련 설정 적용
    if (path.startsWith(":feature:")) {
        configureComposeUiTest()
    }
}

internal fun Project.configureComposeUiTest() {
    val libs = extensions.libs
    dependencies {
        // Jetpack Compose UI 테스트용
        "androidTestImplementation"(libs.findLibrary("compose-ui-test-junit4").get())
        // 테스트용 AndroidManifest 제공해주는 거 (debug 빌드에서만 사용, 테스트 시 Activity 실행 지원)
        "debugImplementation"(libs.findLibrary("compose-ui-test-manifest").get())
        // 테스트를 실제로 돌려주는 실행기
        "androidTestImplementation"(libs.findLibrary("androidx-test-runner").get())
        // JUnit4 기능을 안드로이드 테스트에 연결해주는 어댑터
        "androidTestImplementation"(libs.findLibrary("androidx-test-ext-junit").get())
    }
}
