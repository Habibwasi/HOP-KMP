import SwiftUI
import Shared

// MARK: — PA-09 Rate Driver ───────────────────────────────────────────────────
//
// Post-trip rating screen for a completed passenger booking.
// Supports 1-5 star rating and optional text comment.
// Dispatches BookingEventSubmitRating via BookingViewModelWrapper.
// Effect BookingEffectNavigateToMyTripsPassenger → onSubmitted()

struct RateDriverView: View {

    let bookingId:      String
    let driverName:     String
    let driverInitials: String

    var onSubmitted: () -> Void
    var onBack:      () -> Void

    @StateObject private var wrapper = BookingViewModelWrapper()

    @State private var starRating: Int    = 0
    @State private var comment:    String = ""
    @State private var submitted:  Bool   = false

    private var canSubmit: Bool { starRating > 0 && !wrapper.state.isLoading }

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            VStack(spacing: 0) {

                // ── Driver avatar ─────────────────────────────────────────────
                VStack(spacing: HopSpacing.sm) {
                    HopAvatar(name: driverInitials, imageURL: nil, size: .xlarge)
                        .padding(.top, HopSpacing.xxl)
                    Text("Rate your trip with")
                        .font(HopFont.bodyMedium()).foregroundColor(Color.hopTextSecondary)
                    Text(driverName)
                        .font(HopFont.headlineMedium()).fontWeight(.bold)
                        .foregroundColor(Color.hopTextPrimary)
                }

                // ── Star picker ───────────────────────────────────────────────
                VStack(spacing: HopSpacing.xs) {
                    HopStarRating(rating: $starRating)
                        .padding(.top, HopSpacing.xl)
                    Text(ratingLabel)
                        .font(HopFont.bodyMedium()).foregroundColor(Color.hopPrimaryLime)
                        .opacity(starRating > 0 ? 1 : 0)
                        .animation(.easeInOut(duration: 0.2), value: starRating)
                }

                // ── Optional comment ──────────────────────────────────────────
                VStack(alignment: .leading, spacing: HopSpacing.xs) {
                    Text("Leave a comment (optional)")
                        .font(HopFont.labelMedium()).fontWeight(.medium)
                        .foregroundColor(Color.hopTextSecondary)

                    ZStack(alignment: .topLeading) {
                        if comment.isEmpty {
                            Text("How was your experience?")
                                .font(HopFont.bodyMedium())
                                .foregroundColor(Color.hopTextSecondary.opacity(0.6))
                                .padding(.top, 12).padding(.leading, 6)
                        }
                        TextEditor(text: $comment)
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopTextPrimary)
                            .scrollContentBackground(.hidden)
                            .frame(minHeight: 96)
                    }
                    .padding(.horizontal, HopSpacing.sm)
                    .padding(.vertical, HopSpacing.xs)
                    .background(Color.hopSurfaceElevated)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.hopPrimaryLime.opacity(comment.isEmpty ? 0 : 0.5), lineWidth: 1))
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.top, HopSpacing.xl)

                Spacer()

                // ── CTA ───────────────────────────────────────────────────────
                VStack(spacing: HopSpacing.sm) {
                    HopPrimaryButton(
                        title:     "Submit Rating",
                        isLoading: wrapper.state.isLoading,
                        isEnabled: canSubmit
                    ) {
                        wrapper.submitRating(bookingId: bookingId, stars: starRating, comment: comment.isEmpty ? nil : comment)
                    }
                    HopButton(text: "Skip for now", variant: .ghost) {
                        onSubmitted()
                    }
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.bottom, HopSpacing.xl)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color.hopTextPrimary)
                }
                .accessibilityLabel("Back")
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            wrapper.startObserving { effect in
                if effect is BookingEffectNavigateToMyTripsPassenger {
                    submitted = true
                    onSubmitted()
                }
            }
        }
    }

    private var ratingLabel: String {
        switch starRating {
        case 1: return "Could be better"
        case 2: return "Not great"
        case 3: return "It was okay"
        case 4: return "Pretty good!"
        case 5: return "Excellent! 🎉"
        default: return ""
        }
    }
}

// MARK: — Star rating component

struct HopStarRating: View {
    @Binding var rating: Int
    var maxStars: Int = 5

    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            ForEach(1...maxStars, id: \.self) { index in
                Image(systemName: index <= rating ? "star.fill" : "star")
                    .font(.system(size: 36))
                    .foregroundColor(index <= rating ? Color.hopPrimaryLime : Color.hopTextSecondary.opacity(0.4))
                    .scaleEffect(index == rating ? 1.2 : 1.0)
                    .animation(.spring(response: 0.25, dampingFraction: 0.5), value: rating)
                    .onTapGesture {
                        withAnimation { rating = index }
                    }
                    .accessibilityLabel("\(index) star\(index == 1 ? "" : "s")")
                    .accessibilityAddTraits(index <= rating ? .isSelected : [])
            }
        }
    }
}

// MARK: — Preview

#Preview("PA-09 Rate Driver") {
    NavigationStack {
        RateDriverView(
            bookingId:      "bk-001",
            driverName:     "Mikkel Hansen",
            driverInitials: "MH",
            onSubmitted:    {},
            onBack:         {}
        )
    }
    .preferredColorScheme(.dark)
}
