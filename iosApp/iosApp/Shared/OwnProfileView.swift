import SwiftUI
import Shared

// MARK: — SH-02 Own Profile ───────────────────────────────────────────────────
// Mirrors `OwnProfileScreen.kt`.

struct OwnProfileView: View {

    var navigate: (HopRoute) -> Void
    var onBack:   () -> Void

    @StateObject private var wrapper = OwnProfileViewModelWrapper()

    @State private var toast: String? = nil

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopSurface.ignoresSafeArea()

            ScrollView(showsIndicators: false) {
                VStack(spacing: HopSpacing.lg) {
                    headerSection
                    accountSection
                    vehicleSection
                    reviewsSection
                    Spacer().frame(height: HopSpacing.xxl)
                }
                .padding(.horizontal, HopSpacing.md)
            }

            loadingOverlay
            toastOverlay
        }
        .navigationBarBackButtonHidden(true)
        .navigationTitle("Profile")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(content: toolbarContent)
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .task {
            wrapper.startObserving { effect in
                switch effect {
                case let snack as OwnProfileEffectShowSnackbar:
                    toast = snack.message
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) { withAnimation { toast = nil } }
                case is OwnProfileEffectNavigateToEditCar:
                    navigate(.enableDriverStep1)
                default: break
                }
            }
            wrapper.load()
        }
    }

    // MARK: — Sections

    @ViewBuilder
    private var headerSection: some View {
        if let user = wrapper.state.user {
            HopAvatar(
                name: user.fullName,
                imageURL: nil,
                size: .xlarge,
                isVerified: user.phoneVerified
            )
            .padding(.top, HopSpacing.lg)

            if wrapper.state.isEditingName {
                editNameRow
            } else {
                HStack(spacing: HopSpacing.sm) {
                    Text(user.fullName)
                        .font(HopFont.headlineMedium(weight: .bold))
                        .foregroundColor(Color.hopTextPrimary)
                    Button(action: wrapper.startEditName) {
                        Image(systemName: "pencil")
                            .foregroundColor(Color.hopPrimaryLime)
                    }
                }
            }

            HStack(spacing: HopSpacing.lg) {
                RatingBadge(label: "As driver",    rating: user.ratingDriver as? Double)
                RatingBadge(label: "As passenger", rating: user.ratingPassenger as? Double)
            }
        }
    }

    @ViewBuilder
    private var editNameRow: some View {
        VStack(spacing: HopSpacing.xs) {
            HopTextField(
                label: "Full name",
                placeholder: "",
                text: Binding(
                    get: { wrapper.state.nameDraft },
                    set: { wrapper.nameDraftChanged($0) }
                )
            )
            .padding(.horizontal, HopSpacing.md)

            HStack(spacing: HopSpacing.sm) {
                HopButton(text: "Cancel", variant: .ghost, action: wrapper.cancelEditName)
                HopButton(
                    text: "Save",
                    variant: .primary,
                    isLoading: wrapper.state.isSavingName,
                    action: wrapper.saveName
                )
            }
            .padding(.horizontal, HopSpacing.md)
        }
    }

    @ViewBuilder
    private var accountSection: some View {
        SectionHeader(title: "Account")
        InfoRow(title: "Email", value: wrapper.state.user?.email ?? "—", icon: "envelope")
        phoneRow
    }

    @ViewBuilder
    private var phoneRow: some View {
        let phone: String? = wrapper.state.user?.phone
        if phone == nil {
            InfoRow(title: "Phone", value: "Add phone", icon: "phone", action: wrapper.addPhone)
        } else {
            InfoRow(title: "Phone", value: phone!, icon: "phone", action: nil)
        }
    }

    @ViewBuilder
    private var vehicleSection: some View {
        SectionHeader(title: "Vehicle")
        if let car = wrapper.state.carDetails {
            InfoRow(
                title: "\(car.make) \(car.model) · \(car.year)",
                value: "\(car.colour.capitalized) · \(car.licensePlate)",
                icon: "car",
                action: wrapper.editCar
            )
        } else {
            InfoRow(
                title: "No vehicle",
                value: "Add car details to start driving",
                icon: "car",
                action: wrapper.editCar
            )
        }
    }

    @ViewBuilder
    private var reviewsSection: some View {
        SectionHeader(title: "Reviews")
        if wrapper.state.reviews.isEmpty {
            Text("No reviews yet.")
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopTextSecondary)
                .padding(.horizontal, HopSpacing.md)
        } else {
            VStack(spacing: HopSpacing.xs) {
                ForEach(wrapper.state.reviews, id: \.id) { review in
                    ReviewRow(review: review)
                }
            }
        }
    }

    @ViewBuilder
    private var loadingOverlay: some View {
        if wrapper.state.isLoading && wrapper.state.user == nil {
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                .scaleEffect(1.3)
        }
    }

    @ViewBuilder
    private var toastOverlay: some View {
        if let msg = toast {
            HopToast(message: msg)
                .padding(.bottom, HopSpacing.xl)
                .transition(.move(edge: .bottom).combined(with: .opacity))
        }
    }

    @ToolbarContentBuilder
    private func toolbarContent() -> some ToolbarContent {
        ToolbarItem(placement: .navigationBarLeading) {
            Button(action: onBack) {
                Image(systemName: "arrow.left").foregroundColor(Color.hopTextPrimary)
            }
        }
        ToolbarItem(placement: .navigationBarTrailing) {
            Button(action: { navigate(.settings) }) {
                Image(systemName: "gearshape").foregroundColor(Color.hopTextPrimary)
            }
        }
    }
}

// MARK: — Helpers used by Profile screens

struct SectionHeader: View {
    let title: String
    var body: some View {
        HStack {
            Text(title)
                .font(HopFont.labelMedium(weight: .semibold))
                .foregroundColor(Color.hopTextSecondary)
            Spacer()
        }
        .padding(.top, HopSpacing.md)
    }
}

struct InfoRow: View {
    let title: String
    let value: String
    let icon: String
    var action: (() -> Void)? = nil

    var body: some View {
        Button(action: { action?() }) {
            HStack(spacing: HopSpacing.sm) {
                Image(systemName: icon)
                    .foregroundColor(Color.hopTextSecondary)
                    .frame(width: 24)
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(HopFont.labelMedium(weight: .semibold))
                        .foregroundColor(Color.hopTextPrimary)
                    Text(value)
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopTextSecondary)
                }
                Spacer()
                if action != nil {
                    Image(systemName: "chevron.right")
                        .foregroundColor(Color.hopTextSecondary)
                        .font(.system(size: 12))
                }
            }
            .padding(HopSpacing.md)
            .background(Color.hopSurfaceElevated)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
        .disabled(action == nil)
    }
}

struct RatingBadge: View {
    let label: String
    let rating: Double?

    var body: some View {
        VStack(spacing: 2) {
            HStack(spacing: 4) {
                Image(systemName: "star.fill")
                    .foregroundColor(Color.hopPrimaryLime)
                    .font(.system(size: 14))
                Text(rating.map { String(format: "%.1f", $0) } ?? "—")
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
            }
            Text(label)
                .font(HopFont.bodySmall())
                .foregroundColor(Color.hopTextSecondary)
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.xs)
        .background(Color.hopSurfaceElevated)
        .clipShape(Capsule())
    }
}

struct ReviewRow: View {
    let review: UserReview

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.xs) {
            HStack {
                Text(review.raterName)
                    .font(HopFont.labelMedium(weight: .semibold))
                    .foregroundColor(Color.hopTextPrimary)
                Spacer()
                HStack(spacing: 2) {
                    ForEach(0..<5, id: \.self) { i in
                        Image(systemName: i < Int(review.stars) ? "star.fill" : "star")
                            .foregroundColor(Color.hopPrimaryLime)
                            .font(.system(size: 12))
                    }
                }
            }
            if let comment = review.comment, !comment.isEmpty {
                Text(comment)
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopTextSecondary)
            }
        }
        .padding(HopSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.hopSurfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}


