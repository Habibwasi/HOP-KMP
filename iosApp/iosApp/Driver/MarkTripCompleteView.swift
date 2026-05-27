import SwiftUI
import Shared

// MARK: — DR-11 Mark Trip Complete ────────────────────────────────────────────

struct MarkTripCompleteView: View {

    let tripId: String
    let driverNetOere: Int

    var onCompleted: (_ bookingId: String, _ passengerName: String, _ passengerInitials: String,
                      _ passengerAvatarUrl: String?,
                      _ remainingIds: [String], _ remainingNames: [String], _ remainingInitials: [String],
                      _ remainingAvatarUrls: [String?]) -> Void
    var onSettlementRequired: (_ bookingId: String) -> Void
    var onBack: () -> Void

    @StateObject  private var wrapper = DriverViewModelWrapper.shared
    @State private var toast: String? = nil

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: HopSpacing.lg) {
                Spacer()

                ZStack {
                    Circle().fill(Color.hopAuthAccent.opacity(0.12)).frame(width: 96, height: 96)
                    Image(systemName: "checkmark.seal.fill")
                        .resizable().scaledToFit().frame(width: 44, height: 44)
                        .foregroundColor(Color.hopAuthAccent)
                }

                VStack(spacing: HopSpacing.xs) {
                    Text("Mark trip complete?")
                        .font(HopFont.headlineMedium(weight: .bold))
                        .foregroundColor(Color.hopAuthTextPrimary)
                    Text("You'll receive \(formatDkk(driverNetOere)) net per seat.")
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopAuthTextSecondary)
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
                    HopButton(text: "Cancel", variant: .ghost, lightSurface: true, action: onBack)
                }
                .padding(HopSpacing.md)
            }

            if let msg = toast {
                VStack { Spacer(); HopToast(message: msg).padding(.bottom, HopSpacing.xxl) }
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .task {
            for await effect in wrapper.effects {
                switch effect {
                case let nav as DriverEffectNavigateToRatePassenger:
                    let allPassengers = wrapper.state.activeTripDetail.passengers
                    let bookingIds = nav.bookingIds as! [String]
                    let firstId = bookingIds.first ?? ""
                    let remaining = Array(bookingIds.dropFirst())
                    let first = allPassengers.first(where: { $0.bookingId == firstId })
                    let remNames = remaining.map { id in
                        allPassengers.first(where: { $0.bookingId == id })?.fullName ?? ""
                    }
                    let remInitials = remaining.map { id in
                        allPassengers.first(where: { $0.bookingId == id })?.initials ?? ""
                    }
                    let remAvatarUrls: [String?] = remaining.map { id in
                        allPassengers.first(where: { $0.bookingId == id })?.avatarUrl
                    }
                    onCompleted(
                        firstId,
                        first?.fullName ?? "Passenger",
                        first?.initials ?? "P",
                        first?.avatarUrl,
                        remaining,
                        remNames,
                        remInitials,
                        remAvatarUrls
                    )
                case let nav as DriverEffectNavigateToDriverSettlement:
                    onSettlementRequired(nav.tripId)
                case let snack as DriverEffectShowSnackbar:
                    toast = snack.message
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) { withAnimation { toast = nil } }
                default: break
                }
            }
        }
    .safeAreaInset(edge: .top, spacing: 0) {
        DriverTopBar(title: "Mark trip complete", onBack: onBack)
            .background(Color.hopBackground)
    }
    }

    private func formatDkk(_ oere: Int) -> String {
        let kr = oere / 100
        let rem = oere % 100
        return rem == 0 ? "DKK \(kr)" : "DKK \(kr),\(String(format: "%02d", rem))"
    }
}
