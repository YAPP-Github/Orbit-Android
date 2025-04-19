import com.yapp.convention.setNamespace
import java.util.Properties

plugins {
    id("orbit.android.feature")
}

android {
    setNamespace("feature.alarm.interaction")

    buildTypes {
        val localProperties = Properties()
        localProperties.load(
            project.rootProject.file("local.properties").bufferedReader(),
        )

        debug {
            resValue(
                "string",
                "admob_ad_unit_id",
                localProperties["admobAdUnitIdDebug"] as String,
            )
        }
        release {
            resValue(
                "string",
                "admob_ad_unit_id",
                localProperties["admobAdUnitIdRelease"] as String,
            )
        }
    }
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.common)
    implementation(projects.core.analytics)
    implementation(projects.core.alarm)
    implementation(projects.core.media)
    implementation(projects.domain)
    implementation(projects.core.datastore)
    implementation(libs.orbit.core)
    implementation(libs.orbit.compose)
    implementation(libs.orbit.viewmodel)
    implementation(libs.androidx.material.android)
    implementation(libs.androidx.annotation)
    implementation(libs.gson)
    implementation(libs.play.services.ads)
}
