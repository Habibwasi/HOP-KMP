import SwiftUI
import Shared

// MARK: — DR-12 Rate Passenger ────────────────────────────────────────────────

struct RatePassengerView: View {

    let bookingId:         String
    let passengerName:     String
    let passengerInitials: String

    var onSubmitted: () -> Void
    var onBack:      () -> Void

    @StateObject private var wrapper = BookingViewModelWrapper()

    @State private var stars:   Int    = 0
    @State private var comment: String = ""

    private var canSubmit: Bool { stars > 0 && !wrapper.state.isLoading }
    private let commentMaxChars = 280

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            VStack(spacing: 0) {
                VStack(spacing: HopSpacing.sm) {
                    HopAvatar(name: passengerInitials, imageURL: nil, size: .xlarge)
                        .padding(.top, HopSpacing.xxl)
                    Text("How was your trip with \(passengerName)?")
                        .font(HopFont.headlineMedium(weight: .bold))
                        .foregroundColor(Color.hopTextPrimary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, HopSpacing.md)
                }

                HopStarRating(rating: $stars)
                    .padding(.top, HopSpacing.xl)

                VStack(alignment: .leading, spacing: HopSpacing.xs) {
                    Text("Leave a comment (optional)")
                        .font(HopFont.labelMedium(weight: .medium))
                        .foregroundColor(Color.hopTextSecondary)
                    ZStack(alignment: .topLeading) {
                        if comment.isEmpty {
                            Text("How were they as a passenger?")
                                .font(HopFont.bodyMedium())
                                .foregroundColor(Color.hopTextSecondary.opacity(0.6))
                                .padding(.top, 12).padding(.leading, 6)
                        }
                        TextEditor(text: $comment)
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopTextPrimary)
                            .scrollContentBackground(.hidden)
                            .frame(minHeight: 96)
                            .onChange(of: comment) { _, newValue in
                                if newValue.count > commentMaxChars {
                                    comment = String(newValue.prefix(commentMaxChars))
                                }
                            }
                    }
                    .padding(.horizontal, HopSpacing.sm)
                    .padding(.vertical, HopSpacing.xs)
                    .background(Color.hopSurfaceElevated)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.top, HopSpacing.xl)

                Spacer()

                HopButton(
                    text: "Submit Rating",
                    variant: .primary,
                    isLoading: wrapper.state.isLoading,
                    isEnabled: canSubmit
                ) {
                    wrapper.submitRating(
                        bookingId: bookingId,
                        stars: stars,
                        comment: comment.isEmpty ? nil : comment
                    )
                }
                .padding(HopSpacing.md)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left").foregroundColor(Color.hopTextPrimary)
                }
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            wrapper.startObserving { effect in
                if effect is BookingEffectNavigateToMyTripsPassenger {
                    onSubmitted()
                }
            }
        }
    }
}
