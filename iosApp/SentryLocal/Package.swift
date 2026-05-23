// swift-tools-version:5.7
import PackageDescription

let package = Package(
    name: "SentryLocal",
    platforms: [.iOS(.v13)],
    products: [
        .library(name: "Sentry", targets: ["Sentry"]),
    ],
    targets: [
        .binaryTarget(
            name: "Sentry",
            path: "Sentry.xcframework"
        ),
    ]
)
