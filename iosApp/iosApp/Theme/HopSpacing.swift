import CoreFoundation

/// Spacing tokens — base unit is 4pt. All values are multiples of 4.
/// Mirrors `HopSpacing` in composeApp.
struct HopSpacing {
    static let xxs: CGFloat =  4
    static let xs:  CGFloat =  8
    static let sm:  CGFloat = 12
    static let md:  CGFloat = 16
    static let lg:  CGFloat = 24
    static let xl:  CGFloat = 32
    static let xxl: CGFloat = 48
}
