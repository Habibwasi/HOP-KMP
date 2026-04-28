import SwiftUI
import Shared

// MARK: — DR-11 Mark Trip Complete ────────────────────────────────────────────

struct MarkTripCompleteView: View {

    let tripId: String
    let driverNetOere: Int

    var onCompleted: (_ bookingId: String) -> Void
    var onBack: () -> Void

    @StateObject private var wrapper = DriverViewModelWrapper()
    @State private var toast: String? = nil

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            VStack(spacing: HopSpacing.lg) {
                Spacer()

                ZStack {
                    Circle().fill(Color.hopPrimaryLime.opacity(0.15)).frame(width: 96, height: 96)
                    Image(systemName: "checkmark.seal.fill")
                        .resizable().scaledToFit().frame(width: 44, height: 44)
                        .foregroundColor(Color.hopPrimaryLime)
                }

                VStack(spacing: HopSpacing.xs) {
                    Text("Mark trip complete?")
                        .font(HopFont.headlineMedium(weight: .bold))
                        .foregroundColor(Color.hopTextPrimary)
                    Text("You'll receive DKK \(driverNetOere / 100) net per seat.")
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopTextSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, HopSpacing.xl)
                }

                Spacer()

                VStack(spacing: HopSpacing.sm) {
                    HopButton(
                        text: "Confirm complete",
                        variant: .primary,
                        isLoading: wrapper.state.isLoading
                    ) {
                        wrapper.completeTrip(tripId: tripId)
                    }
                    HopButton(text: "Cancel", variant: .ghost, action: onBack)
                }
                .padding(HopSpacing.md)
            }

            if let msg = toast {
                VStack { Spacer(); HopToast(message: msg).padding(.bottom, HopSpacing.xxl) }
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left").foregroundColor(Color.hopTextPrimary)
                }
            }
            ToolbarItem(placement: .principal) {
                Text("Complete trip").font(HopFont.bodyLarge(weight: .semibold)).foregroundColor(Color.hopTextPrimary)
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            wrapper.startObserving { effect in
                switch effect {
                case let nav as DriverEffectNavigateToRatePassenger:
                    onCompleted(nav.bookingId)
                case let snack as DriverEffectShowSnackbar:
                    toast = snack.message
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) { withAnimation { toast = nil } }
                default: break
                }
            }
        }
    }
}
