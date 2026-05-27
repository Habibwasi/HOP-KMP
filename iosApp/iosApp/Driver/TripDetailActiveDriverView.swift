import SwiftUI
import Shared

// MARK: — DR-10 Trip Detail Active (Driver) ───────────────────────────────────

struct TripDetailActiveDriverView: View {

    let tripId: String

    var onMarkComplete: (_ tripId: String, _ driverNetOere: Int) -> Void
    var onMessagePassenger: (String) -> Void
    var onBack: () -> Void

    @StateObject  private var wrapper = DriverViewModelWrapper.shared

    var body: some View {
        let detail = wrapper.state.activeTripDetail

        return ZStack {
            Color.hopBackground.ignoresSafeArea()

            ScrollView {
                if detail.isLoading && detail.trip == nil {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                        .padding(.top, HopSpacing.xxl)
                } else if let trip = detail.trip {
                    VStack(alignment: .leading, spacing: HopSpacing.md) {
                        // Trip card
                        VStack(alignment: .leading, spacing: HopSpacing.sm) {
                            HStack {
                                Text("\(trip.originName) → \(trip.destName)")
                                    .font(HopFont.headlineSmall(weight: .semibold))
                                    .foregroundColor(Color.hopAuthTextPrimary)
                                Spacer()
                                tripStatusBadge(trip.trip.status)
                            }
                            Text(trip.formattedDepartsAt)
                                .font(HopFont.bodySmall())
                                .foregroundColor(Color.hopAuthTextSecondary)
                            Text("\(trip.seatsBooked)/\(trip.seatsTotal) seats booked")
                                .font(HopFont.bodySmall(weight: .semibold))
                                .foregroundColor(Color.hopAuthAccent)
                        }
                        .padding(HopSpacing.md)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.hopCardSurfaceMuted)
                        .clipShape(RoundedRectangle(cornerRadius: 12))

                        Text("Passengers")
                            .font(HopFont.labelMedium(weight: .semibold))
                            .foregroundColor(Color.hopAuthTextPrimary)
                            .padding(.top, HopSpacing.sm)

                        if detail.passengers.isEmpty {
                            Text("No passengers booked yet.")
                                .font(HopFont.bodyMedium())
                                .foregroundColor(Color.hopAuthTextSecondary)
                                .padding(.vertical, HopSpacing.md)
                        } else {
                            VStack(spacing: HopSpacing.xs) {
                                ForEach(detail.passengers, id: \.bookingId) { p in
                                    PassengerRow(
                                        passenger: p,
                                        onMessage: { onMessagePassenger(p.bookingId) }
                                    )
                                }
                            }
                        }

                        Spacer().frame(height: HopSpacing.lg)

                        HopButton(text: "Mark trip complete", variant: .primary) {
                            onMarkComplete(trip.id, Int(trip.trip.driverNetOere))
                        }
                    }
                    .padding(HopSpacing.md)
                } else if let err = detail.error {
                    EmptyState(
                        systemImage: "exclamationmark.triangle",
                        headline: "Couldn't load trip",
                        subtitle: err
                    )
                }
            }
        }
        .task {
            wrapper.loadActiveTripDetail(tripId: tripId)
        }
    .safeAreaInset(edge: .top, spacing: 0) {
        DriverTopBar(title: "Trip detail", onBack: onBack)
            .background(Color.hopBackground)
    }
    }

    @ViewBuilder
    private func tripStatusBadge(_ status: TripStatus) -> some View {
        let (label, color): (String, Color) = {
            switch status {
            case .active:          return ("Active",            Color.hopAuthAccent)
            case .confirmed:       return ("Confirmed",         Color.hopSuccess)
            case .completed:       return ("Completed",         Color.hopAuthTextSecondary)
            case .cancelled:       return ("Cancelled",         Color.hopError)
            case .thresholdNotMet: return ("Threshold not met", Color.hopWarning)
            default:               return ("Unknown",           Color.hopAuthTextSecondary)
            }
        }()
        Text(label)
            .font(HopFont.labelSmall(weight: .semibold))
            .foregroundColor(color)
            .padding(.horizontal, HopSpacing.xs)
            .padding(.vertical, 2)
            .background(color.opacity(0.15))
            .clipShape(Capsule())
    }
}

private struct PassengerRow: View {
    let passenger: PassengerSummary
    let onMessage: () -> Void

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            HopAvatar(name: passenger.initials, imageURL: passenger.avatarUrl.flatMap { URL(string: $0) }, size: .medium)
            VStack(alignment: .leading, spacing: 2) {
                Text(passenger.fullName)
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                HStack(spacing: 4) {
                    Image(systemName: "star.fill").font(.system(size: 10)).foregroundColor(Color.hopWarning)
                    Text(String(format: "%.1f", passenger.rating))
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopAuthTextSecondary)
                    Text("· \(passenger.seats) seat\(passenger.seats == 1 ? "" : "s")")
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopAuthTextSecondary)
                }
            }
            Spacer()
            Button(action: onMessage) {
                Image(systemName: "bubble.left")
                    .foregroundColor(Color.hopAuthAccent)
                    .padding(8)
                    .background(Color.hopCardSurfaceMuted)
                    .clipShape(Circle())
            }
            .buttonStyle(.plain)
        }
        .padding(HopSpacing.sm)
        .background(Color.hopCardSurfaceMuted)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
