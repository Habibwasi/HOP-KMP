This is a Kotlin Multiplatform project targeting Android, iOS.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for the code that will be shared between all targets in the project.
  The most important subfolder is [commonMain](./shared/src/commonMain/kotlin). If preferred, you
  can add code to the platform-specific folders here too.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---
## Trip Data Layer — UNKNOWN Enum Handling (Option 4)

**Pattern:** Keep broken trips visible, greyed-out, soft-logged.

**Implementation (3 steps):**

1. **Domain:** Add `fun Trip.isBroken() = status == UNKNOWN || model == UNKNOWN`

2. **ViewModel:** 
   ```kotlin
   val uiModels = result.data.toUiModels()  // Wraps in TripUiModel
   uiModels.filter { it.isBroken }.forEach { logBrokenTrip(it) }
   state = state.copy(trips = uiModels)  // Store wrapped models
   ```

3. **UI:** 
   ```kotlin
   val isEnabled = !tripUi.isBroken
   Surface(
       modifier = Modifier.alpha(if (isEnabled) 1f else 0.6f)
           .clickable(enabled = isEnabled) { ... }
   )
   ```

**Why:** Observable (logs), recoverable (pull-refresh), version-safe (handles future enums), preserves user visibility.

**Reference Files:** 
- Template ViewModel: `shared/src/commonMain/kotlin/com/example/hop/presentation/trips/SearchTripsViewModel.kt`
- Template UI: `shared/src/commonMain/kotlin/com/example/hop/ui/components/TripListItemOption4.kt`  
- Wrapper Model: `shared/src/commonMain/kotlin/com/example/hop/presentation/model/TripUiModel.kt`

---
Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…