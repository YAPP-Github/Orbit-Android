import com.yapp.convention.setNamespace
import java.util.Properties

plugins {
    id("orbit.android.library")
    id("orbit.android.compose")
}

android {
    setNamespace("core.ui")

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
    implementation(projects.core.designsystem)
    implementation(projects.core.media)
    implementation(libs.compose.material)
    implementation(libs.orbit.core)
    implementation(libs.orbit.compose)
    implementation(libs.orbit.viewmodel)
    implementation(libs.lottie.compose)
    implementation(libs.play.services.ads)
}
