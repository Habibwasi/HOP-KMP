import SwiftUI
import Shared

/// PA-09 — Rate Driver. Mirrors composeApp `RateDriverScreen.kt`.
struct RateDriverView: View {
    let bookingId: String
    let onSubmitted: () -> Void
    let onSkip: () -> Void
    let onBack: () -> Void

    @StateObject private var wrapper = BookingViewModelWrapper()
    @State private var rating: Int = 0
    @State private var comment: String = ""
    private let commentMax = 280

    private var driverName: String { wrapper.state.driverName }
    private var driverInitials: String { wrapper.state.driverInitials }

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: HopSpacing.sm) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .frame(width: 44, height: 44)
                }.buttonStyle(.plain)
                Text("Rate your trip")
                    .font(HopFont.titleMedium())
                    .foregroundColor(Color.hopAuthTextPrimary)
                Spacer()
            }
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, HopSpacing.xs)

            ScrollView {
                VStack(alignment: .center, spacing: HopSpacing.lg) {
                    Spacer().frame(height: HopSpacing.lg)
                    HopAvatar(name: driverName, size: .large)
                    Text("How was your trip with \(driverName)?")
                        .font(HopFont.headlineSmall(weight: .semibold))
                        .foregroundColor(Color.hopAuthTextPrimary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, HopSpacing.lg)

                    StarRatingInput(rating: $rating, lightSurface: true)

                    VStack(alignment: .leading, spacing: 4) {
                        TextEditor(text: $comment)
                            .font(HopFont.bodyMedium())
                            .foregroundColor(Color.hopAuthTextPrimary)
                            .scrollContentBackground(.hidden)
                            .padding(8)
                            .frame(height: 110)
                            .background(Color.hopAuthInputSurface)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.hopAuthInputBorder, lineWidth: 1)
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                            .onChange(of: comment) { _, new in
                                if new.count > commentMax {
                                    comment = String(new.prefix(commentMax))
                                }
                            }
                        HStack {
                            Spacer()
                            Text("\(comment.count) / \(commentMax)")
                                .font(HopFont.labelSmall())
                                .foregroundColor(Color.hopAuthTextSecondary)
                        }
                    }
                    .padding(.horizontal, HopSpacing.md)
                    Spacer().frame(height: HopSpacing.lg)
                }
                .frame(maxWidth: .infinity)
            }

            HopButton(
                text: "Submit",
                variant: .primary,
                isLoading: wrapper.state.isLoading,
                isEnabled: rating > 0
            ) {
                wrapper.submitRating(
                    bookingId: bookingId,
                    stars: rating,
                    comment: comment.trimmingCharacters(in: .whitespaces).isEmpty ? nil : comment
                )
            }
            .padding(.horizontal, HopSpacing.md)
            Button(action: onSkip) {
                Text("Maybe later")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, HopSpacing.xs)
            }
            .buttonStyle(.plain)
            .padding(.horizontal, HopSpacing.md)
            .padding(.bottom, HopSpacing.md)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.hopBackground.ignoresSafeArea())
        .onAppear {
            wrapper.loadDriverForRating(bookingId: bookingId)
            wrapper.startObserving { effect in
                // Submit success → screen pops via Route layer; here we just navigate.
                if effect is BookingEffectShowSnackbar { return }
                onSubmitted()
            }
        }
    }
}
