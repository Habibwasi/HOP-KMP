import SwiftUI
import Shared

// MARK: — SH-03 Other User's Profile ──────────────────────────────────────────
// Mirrors `OtherProfileScreen.kt`. Read-only view of another user with a
// "Report" action.

struct OtherProfileView: View {

    let userId: String
    var onBack: () -> Void

    @StateObject private var wrapper = OtherProfileViewModelWrapper()
    @State private var reportReason: String = ""
    @State private var toast: String? = nil

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopSurface.ignoresSafeArea()

            VStack(spacing: 0) {
                // Inline top bar — toolbar(.hidden) in HopNavigationStack hides ToolbarItem
                HStack(spacing: 0) {
                    Button(action: onBack) {
                        Image(systemName: "arrow.left")
                            .font(.system(size: 18, weight: .regular))
                            .foregroundColor(Color.hopTextPrimary)
                            .frame(width: 44, height: 44)
                    }
                    .accessibilityLabel("Navigate back")
                    Text("Profile")
                        .font(HopFont.bodyLarge(weight: .semibold))
                        .foregroundColor(Color.hopTextPrimary)
                    Spacer()
                }
                .padding(.horizontal, HopSpacing.xs)
                .background(Color.hopSurface)

                ScrollView(showsIndicators: false) {
                VStack(spacing: HopSpacing.lg) {
                    if let user = wrapper.state.user {
                        HopAvatar(name: user.fullName, size: .xlarge, isVerified: user.phoneVerified)
                            .padding(.top, HopSpacing.lg)

                        Text(user.fullName)
                            .font(HopFont.headlineMedium(weight: .bold))
                            .foregroundColor(Color.hopTextPrimary)

                        HStack(spacing: HopSpacing.lg) {
                            RatingBadge(label: "As driver",    rating: user.ratingDriver as? Double)
                            RatingBadge(label: "As passenger", rating: user.ratingPassenger as? Double)
                        }

                        if let car = wrapper.state.carDetails {
                            SectionHeader(title: "Vehicle")
                            InfoRow(
                                title: "\(car.make) \(car.model) · \(car.year)",
                                value: "\(car.colour.capitalized) · \(car.licensePlate)",
                                icon: "car"
                            )
                        }

                        SectionHeader(title: "Reviews")
                        if wrapper.state.reviews.isEmpty {
                            Text("No reviews yet.")
                                .font(HopFont.bodyMedium())
                                .foregroundColor(Color.hopTextSecondary)
                                .padding(.horizontal, HopSpacing.md)
                        } else {
                            VStack(spacing: HopSpacing.xs) {
                                ForEach(wrapper.state.reviews, id: \.id) { ReviewRow(review: $0) }
                            }
                        }

                        HopButton(text: "Report user", variant: .destructive, action: wrapper.showReportDialog)
                            .padding(.top, HopSpacing.md)

                    } else if wrapper.state.isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: Color.hopTextPrimary))
                            .scaleEffect(1.3)
                            .padding(.top, HopSpacing.xxl)
                    } else if let err = wrapper.state.error {
                        EmptyState(systemImage: "exclamationmark.triangle", headline: "Failed to load", subtitle: err, ctaLabel: "Retry", ctaAction: { wrapper.load(userId: userId) })
                    }

                    Spacer().frame(height: HopSpacing.xxl)
                }
                .padding(.horizontal, HopSpacing.md)
                } // end ScrollView
            } // end VStack (top bar + scroll)

            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarBackButtonHidden(true)
        .sheet(isPresented: Binding(
            get: { wrapper.state.isReportDialogVisible },
            set: { if !$0 { wrapper.dismissReportDialog() } }
        )) {
            ReportSheet(
                reason: $reportReason,
                isSubmitting: wrapper.state.isSubmittingReport,
                onCancel: wrapper.dismissReportDialog,
                onSubmit: {
                    wrapper.submitReport(reason: reportReason)
                    reportReason = ""
                }
            )
            .presentationDetents([.medium])
        }
        .task {
            wrapper.startObserving { effect in
                switch effect {
                case let snack as OtherProfileEffectShowSnackbar:
                    toast = snack.message
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) { withAnimation { toast = nil } }
                case is OtherProfileEffectNavigateBack:
                    onBack()
                default: break
                }
            }
            wrapper.load(userId: userId)
        }
    }
}

private struct ReportSheet: View {
    @Binding var reason: String
    let isSubmitting: Bool
    let onCancel: () -> Void
    let onSubmit: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.md) {
            Text("Report user")
                .font(HopFont.headlineSmall(weight: .bold))
                .foregroundColor(Color.hopTextPrimary)

            Text("Tell us why. Reports are reviewed by Hop's safety team.")
                .font(HopFont.bodySmall())
                .foregroundColor(Color.hopTextSecondary)

            TextEditor(text: $reason)
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopTextPrimary)
                .scrollContentBackground(.hidden)
                .frame(minHeight: 120)
                .padding(HopSpacing.xs)
                .background(Color.hopSurfaceElevated)
                .clipShape(RoundedRectangle(cornerRadius: 12))

            Spacer()

            HStack(spacing: HopSpacing.sm) {
                HopButton(text: "Cancel", variant: .ghost, action: onCancel)
                HopButton(
                    text: "Submit",
                    variant: .destructive,
                    isLoading: isSubmitting,
                    isEnabled: !reason.trimmingCharacters(in: .whitespaces).isEmpty,
                    action: onSubmit
                )
            }
        }
        .padding(HopSpacing.md)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(Color.hopSurface)
    }
}
