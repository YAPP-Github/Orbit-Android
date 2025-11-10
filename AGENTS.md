# Repository Guidelines

## Project Structure & Module Organization
Orbit is a multi-module Android app. The entry point lives in `app/`, while shared layers live in `domain/` and `data/`. Reusable UI, networking, analytics, alarms, and configuration code sit in `core/*` (for example `core:designsystem`, `core:network`, `core:analytics`). Feature-specific flows ship from `feature/*` (e.g., `feature:fortune`, `feature:mission`). Common build scripts are centralized under `build-logic/` and dependency catalogues live in `gradle/`.

## Build, Test & Development Commands
- `./gradlew assembleDebug` – Build the debug APK with all modules.
- `./gradlew :app:installDebug` – Install the latest debug build on a connected device or emulator.
- `./gradlew testDebugUnitTest` – Execute JVM unit tests across modules.
- `./gradlew connectedDebugAndroidTest` – Run instrumentation/UI tests on a device.
- `./gradlew ktlintCheck` / `ktlintFormat` – Verify or auto-format Kotlin style.

## Coding Style & Naming Conventions
Kotlin source uses 4-space indentation, trailing commas where helpful, and idiomatic coroutines/Flow patterns. Follow Jetpack Compose best practices: hoist state, keep composables small, and preview via `@Preview` where possible. Classes, objects, and functions use UpperCamelCase or lowerCamelCase; resource IDs and Gradle task names use snake_case. Keep packages aligned with features (`feature.fortune`, `core.network`). ktlint enforces formatting; run it before opening a PR.

## Testing Guidelines
Unit tests rely on JUnit4, MockK, and coroutine-testing; place them under `src/test`. UI and integration tests use Espresso, Compose testing, or Robolectric in `src/androidTest`. Prefer test method names that read like sentences (e.g., `shouldReturnDefaultMission_whenAlarmCreated`). For new features, cover at least happy-path and primary failure cases, and update any existing baselines.

## Commit & Pull Request Guidelines
Commit messages follow the `"[TYPE/#issue] Summary"` convention observed in the log (e.g., `[BUGFIX/#256] Resolve ghost mission state`). Types commonly include FEAT, BUGFIX, CHORE, and HOTFIX. For pull requests, include a concise summary, linked issue reference, screenshots or screen recordings for UI-facing changes, and a checklist of tests you ran. Request reviews from module owners and ensure CI builds, tests, and ktlint checks are green before merging.
