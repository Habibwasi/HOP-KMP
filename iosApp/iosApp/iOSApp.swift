import SwiftUI
import Shared

@main
struct iOSApp: App {

    @UIApplicationDelegateAdaptor(HopAppDelegate.self) var appDelegate

    init() {
        let supabaseUrl = Bundle.main.infoDictionary?["SUPABASE_URL"] as? String ?? ""
        let supabaseAnonKey = Bundle.main.infoDictionary?["SUPABASE_ANON_KEY"] as? String ?? ""
        let mapsApiKey = Bundle.main.infoDictionary?["MAPS_API_KEY"] as? String ?? ""
        KoinIOSKt.doInitKoin(supabaseUrl: supabaseUrl, supabaseAnonKey: supabaseAnonKey, mapsApiKey: mapsApiKey)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    guard url.scheme == "hop" else { return }
                    let vm = KoinIOSKt.getAuthViewModel()
                    vm.onEvent(event: AuthEventHandleDeepLink(url: url.absoluteString))
                }
        }
    }
}