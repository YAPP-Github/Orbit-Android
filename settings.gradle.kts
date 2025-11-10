enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    includeBuild("build-logic") {
        name = "build-logic"
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Orbit"
include(":app")
include(":core:network")
include(":core:designsystem")
include(":core:common")
include(":core:datastore")
include(":core:buildconfig")
include(":data")
include(":domain")
include(":feature")
include(":core:ui")
include(":feature:home")
include(":feature:onboarding")
include(":feature:mission")
include(":feature:fortune")
include(":core:media")
include(":feature:setting")
include(":feature:alarm-interaction")
include(":core:alarm")
include(":feature:splash")
include(":feature:webview")
include(":core:analytics")
include(":core:remoteconfig")
include(":core:database")
include(":baselineprofile")
