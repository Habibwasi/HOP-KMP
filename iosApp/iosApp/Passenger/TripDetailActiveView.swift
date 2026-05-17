import SwiftUI
import Shared

/// PA-08 — Trip Detail Active. Mirrors composeApp `TripDetailActiveScreen.kt`.
struct TripDetailActiveView: View {
    let bookingId: String
    let onBack: () -> Void
    let onNavigateToChat: (_ bookingId: String) -> Void
    let onNavigateToCancellationConfirmation: (_ bookingId: String) -> Void
    let onNavigateToPassengerSettlement: (_ bookingId: String) -> Void

    @StateObject private var wrapper = TripDetailActiveViewModelWrapper()
    @StateObject private var bookingWrapper = BookingViewModelWrapper()
    @State private var showCancelDialog: Bool = false
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: HopSpacing.sm) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .frame(width: 36, height: 36)
                }.buttonStyle(.plain)
                Text("Trip Details")
                    .font(HopFont.titleMedium())
                    .foregroundColor(Color.hopAuthTextPrimary)
                Spacer()
            }
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, HopSpacing.xs)

            if wrapper.state.isLoading {
                Spacer()
                ProgressView().tint(Color.hopPrimaryGreen)
                Spacer()
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: HopSpacing.lg) {
                        DriverInfoSection(state: wrapper.state)
                        Divider()
                        ActiveRouteSection(
                            originName: wrapper.state.originName,
                            destName: wrapper.state.destName,
                            departsAt: wrapper.state.departsAt
                        )
                        Divider()
                        BookingStatusBadge(status: wrapper.state.bookingStatus)
                        if wrapper.state.tripModel == .b,
                           wrapper.state.bookingStatus == .pending,
                           let minT = wrapper.state.minThreshold?.intValue {
                            ThresholdSection(
                                seatsBooked: Int(wrapper.state.seatsBooked),
                                minThreshold: minT,
                                progress: Double(wrapper.state.thresholdProgress)
                            )
                        }
                    }
                    .padding(.horizontal, HopSpacing.md)
                    .padding(.vertical, HopSpacing.md)
                }
            }

            VStack(spacing: HopSpacing.sm) {
                HopButton(text: "Message Driver", variant: .ghost, lightSurface: true) {
                    wrapper.messageDriver()
                }
                if wrapper.state.bookingStatus == .awaitingPayment {
                    HopButton(text: "Pay Driver", variant: .primary) {
                        onNavigateToPassengerSettlement(bookingId)
                    }
                } else {
                    HopButton(
                        text: "Cancel Booking",
                        variant: .destructive,
                        isLoading: bookingWrapper.state.isLoading
                    ) {
                        showCancelDialog = true
                    }
                }
            }
            .padding(HopSpacing.md)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.hopBackground.ignoresSafeArea())
        .alert("Cancel booking?", isPresented: $showCancelDialog) {
            Button("Cancel booking", role: .destructive) {
                bookingWrapper.cancelBooking(id: bookingId)
            }
            Button("Keep booking", role: .cancel) {}
        } message: {
            Text("You can't undo this. Your seat will be released and refund processed.")
        }
        .onAppear {
            wrapper.startObserving { effect in
                if let chat = effect as? TripDetailActiveEffectNavigateToChat {
                    onNavigateToChat(chat.bookingId)
                }
            }
            bookingWrapper.startObserving { effect in
                if let nav = effect as? BookingEffectNavigateToCancellationConfirmation {
                    onNavigateToCancellationConfirmation(nav.bookingId)
                }
            }
            wrapper.load(bookingId: bookingId)
        }
        .onChange(of: scenePhase) { phase in
            if phase == .active {
                wrapper.load(bookingId: bookingId)
            }
        }
        .onChange(of: wrapper.state.bookingStatus) { status in
            if status == .awaitingPayment {
                onNavigateToPassengerSettlement(bookingId)
            }
        }
    }
}

private struct DriverInfoSection: View {
    let state: TripDetailActiveUiState
    var body: some View {
        HStack(alignment: .center, spacing: HopSpacing.md) {
            HopAvatar(name: state.driverName, size: .xlarge, isVerified: state.isDriverVerified)
            VStack(alignment: .leading, spacing: 4) {
                Text(state.driverName)
                    .font(HopFont.headlineSmall(weight: .semibold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                if !state.driverPhone.isEmpty {
                    Text(state.driverPhone)
                        .font(HopFont.bodyMedium())
                        .foregroundColor(Color.hopAuthAccent)
                }
                StarRatingDisplay(rating: Double(state.driverRating), count: nil, starSize: 14)
            }
            Spacer()
            if !state.driverPhone.isEmpty,
               let url = URL(string: "tel:\(state.driverPhone.filter { !$0.isWhitespace })") {
                Link(destination: url) {
                    Image(systemName: "phone.fill")
                        .font(.system(size: 16))
                        .foregroundColor(Color.hopPrimaryGreen)
                        .frame(width: 44, height: 44)
                        .background(Color.hopPrimaryLime.opacity(0.15))
                        .clipShape(Circle())
                }
            }
        }
    }
}

private struct ActiveRouteSection: View {
    let originName: String
    let destName: String
    let departsAt: String
    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.sm) {
            HStack {
                Circle().fill(Color.hopPrimaryLime).frame(width: 12, height: 12)
                Text(originName).font(HopFont.bodyLarge()).foregroundColor(Color.hopAuthTextPrimary)
            }
            HStack {
                Circle().fill(Color.hopAuthTextPrimary).frame(width: 12, height: 12)
                Text(destName).font(HopFont.bodyLarge(weight: .semibold)).foregroundColor(Color.hopAuthTextPrimary)
            }
            HStack(spacing: 6) {
                Image(systemName: "calendar").font(.system(size: 12)).foregroundColor(Color.hopAuthTextSecondary)
                Text(departsAt).font(HopFont.bodyMedium()).foregroundColor(Color.hopAuthTextSecondary)
            }
        }
    }
}

private struct BookingStatusBadge: View {
    let status: BookingStatus
    var body: some View {
        let (label, color): (String, Color) = {
            switch status {
            case .confirmed: return ("Confirmed", Color.hopSuccess)
            case .pending: return ("Pending", Color.hopWarning)
            case .awaitingPayment: return ("Awaiting payment", Color.hopWarning)
            case .cancelled: return ("Cancelled", Color.hopError)
            case .completed: return ("Completed", Color.hopSuccess)
            case .disputed: return ("Disputed", Color.hopError)
            default: return ("Unknown", Color.hopAuthTextSecondary)
            }
        }()
        HStack(spacing: 6) {
            Circle().fill(color).frame(width: 8, height: 8)
            Text(label)
                .font(HopFont.labelMedium(weight: .semibold))
                .foregroundColor(color)
        }
        .padding(.horizontal, HopSpacing.sm)
        .padding(.vertical, 6)
        .background(color.opacity(0.15))
        .clipShape(Capsule())
    }
}

private struct ThresholdSection: View {
    let seatsBooked: Int
    let minThreshold: Int
    let progress: Double
    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.xs) {
            Text("\(seatsBooked) of \(minThreshold) seats confirmed")
                .font(HopFont.bodyMedium(weight: .medium))
                .foregroundColor(Color.hopAuthTextPrimary)
            ProgressView(value: progress).tint(Color.hopWarning)
            Text("Your seat is held until the trip reaches the minimum threshold.")
                .font(HopFont.labelSmall())
                .foregroundColor(Color.hopAuthTextSecondary)
        }
        .padding(HopSpacing.md)
        .background(Color.hopWarning.opacity(0.10))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
