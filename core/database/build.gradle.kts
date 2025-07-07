import com.yapp.convention.setNamespace

plugins {
    id("orbit.android.library")
    alias(libs.plugins.room)
}

android {
    setNamespace("core.database")

    sourceSets { getByName("androidTest").assets.srcDir("$projectDir/schemas") }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(projects.domain)

    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    implementation(libs.material)
    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
