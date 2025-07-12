import com.yapp.convention.setNamespace

plugins {
    id("orbit.android.library")
    id("androidx.room")
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

    androidTestImplementation(libs.androidx.room.testing)
}
