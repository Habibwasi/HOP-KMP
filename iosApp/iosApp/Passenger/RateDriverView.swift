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
    private let commentMaxChars = 280

    var body: some View {
        ZStack {
            Color.hopSurface.ignoresSafeArea()

            VStack(spacing: 0) {

                // ── Driver avatar ─────────────────────────────────────────────
                VStack(spacing: HopSpacing.sm) {
                    HopAvatar(name: driverInitials, imageURL: nil, size: .xlarge)
                        .padding(.top, HopSpacing.xxl)
                    Text("How was your trip with \(driverName)?")
                        .font(HopFont.headlineMedium()).fontWeight(.bold)
                        .foregroundColor(Color.hopTextPrimary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, HopSpacing.md)
                }

                // ── Star picker ───────────────────────────────────────────────
                HopStarRating(rating: $starRating)
                    .padding(.top, HopSpacing.xl)

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
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.hopPrimaryLime.opacity(comment.isEmpty ? 0 : 0.5), lineWidth: 1))

                    HStack {
                        Spacer()
                        Text("\(comment.count)/\(commentMaxChars)")
                            .font(HopFont.bodySmall())
                            .foregroundColor(comment.count >= commentMaxChars ? Color.hopError : Color.hopTextSecondary)
                    }
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
            ToolbarItem(placement: .principal) {
                Text("Rate your trip")
                    .font(HopFont.bodyLarge()).fontWeight(.semibold)
                    .foregroundColor(Color.hopTextPrimary)
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
