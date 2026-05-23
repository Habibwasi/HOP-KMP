import UIKit
import UserNotifications
import Sentry
import Shared

/// Handles APNS push notification registration for the Hop iOS app.
///
/// Wired via `@UIApplicationDelegateAdaptor` in `iOSApp.swift`.
/// Token is forwarded to the KMP shared `UserRepository.savePushToken(token, "ios")`
/// via `KoinIOSKt.registerPushToken(token:platform:)`.
class HopAppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Initialise Sentry before anything else so crashes during launch are captured.
        if let dsn = Bundle.main.object(forInfoDictionaryKey: "SENTRY_DSN") as? String, !dsn.isEmpty {
            SentrySDK.start { options in
                options.dsn = dsn
                #if DEBUG
                options.environment = "development"
                #else
                options.environment = Bundle.main.object(forInfoDictionaryKey: "SENTRY_ENV") as? String ?? "production"
                #endif
                options.releaseName = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String

                // Error monitoring — crash reporting, app hangs, watchdog terminations.
                options.enableCrashHandler = true
                options.enableAppHangTracking = true
                options.enableReportNonFullyBlockingAppHangs = true
                options.enableWatchdogTerminationTracking = true
                options.attachScreenshot = true
                options.attachViewHierarchy = true
                options.sendDefaultPii = true

                // Tracing — 20% of transactions; auto-instruments app launch, network, UIViewController.
                options.tracesSampleRate = 0.2
                options.enableUserInteractionTracing = true
                options.tracePropagationTargets = ["hop.ridly.dk"]

                // Profiling — 10% of sessions, lifecycle tied to traces.
                options.configureProfiling = {
                    $0.sessionSampleRate = 0.1
                    $0.lifecycle = .trace
                }

                // Session Replay — 10% continuous, 100% on errors (disabled on iOS 26+ by default).
                options.sessionReplay.sessionSampleRate = 0.1
                options.sessionReplay.onErrorSampleRate = 1.0

                // Structured logging (SDK 9.0+).
                options.enableLogs = true
            }
        }

        UNUserNotificationCenter.current().delegate = self

        // Request push notification permission; register for remote notifications only if granted.
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            guard granted else { return }
            DispatchQueue.main.async {
                UIApplication.shared.registerForRemoteNotifications()
            }
        }
        return true
    }

    /// Called by iOS when APNS issues (or reissues) a device token.
    /// Converts the token `Data` to a lowercase hex string and registers it with the backend.
    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        let hexToken = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        KoinIOSKt.registerPushToken(token: hexToken, platform: "ios")
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        // Non-fatal — push will not work until the next successful registration.
        print("[HopAppDelegate] APNS registration failed: \(error.localizedDescription)")
    }

    /// Show notifications as banners even when the app is in the foreground.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .badge])
    }
}
