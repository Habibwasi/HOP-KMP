import SwiftUI
import PhotosUI
import Shared

// MARK: — SH-02 Own Profile ───────────────────────────────────────────────────
//
// Mirrors `OwnProfileScreen.kt` 1:1.  Light theme.  Sections: avatar with
// edit overlay, inline editable name, contact info (email + phone with
// "Add phone" affordance), ratings block, recent reviews list, and (driver
// only) car details card.

struct OwnProfileView: View {

    var navigate: (HopRoute) -> Void
    var onBack:   () -> Void

    @StateObject private var wrapper = OwnProfileViewModelWrapper()
    @State private var toast: String? = nil
    @State private var showPhotoSourceSheet = false
    @State private var showCameraPicker = false
    @State private var showLibraryPicker = false
    @State private var selectedPhotoItem: PhotosPickerItem? = nil

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                ProfileTopBar(
                    title: "Profile",
                    onBack: onBack,
                    trailing: {
                        AnyView(
                            Button(action: { navigate(.settings) }) {
                                Image(systemName: "gearshape")
                                    .font(.system(size: 20))
                                    .foregroundColor(Color.hopAuthTextPrimary)
                                    .frame(width: 44, height: 44)
                            }
                            .accessibilityLabel("Settings")
                        )
                    }
                )

                if wrapper.state.isLoading && wrapper.state.user == nil {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: Color.hopAuthAccent))
                        .scaleEffect(1.3)
                    Spacer()
                } else {
                    ScrollView(showsIndicators: false) {
                        VStack(spacing: 0) {
                            avatarSection

                            Spacer().frame(height: HopSpacing.lg)

                            nameSection

                            Spacer().frame(height: HopSpacing.sm)

                            contactSection

                            ratingsSection

                            reviewsSection

                            carSection

                            mobilepaySection

                            Spacer().frame(height: HopSpacing.xl)
                        }
                    }
                }
            }

            if let msg = toast {
                HopToast(message: msg)
                    .padding(.bottom, HopSpacing.xl)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .task {
            wrapper.startObserving { effect in
                switch effect {
                case let snack as OwnProfileEffectShowSnackbar:
                    withAnimation { toast = snack.message }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                        withAnimation { toast = nil }
                    }
                case is OwnProfileEffectNavigateToEditCar:
                    navigate(.enableDriverStep1)
                case is OwnProfileEffectNavigateToPhoneVerification:
                    navigate(.otpVerification(phone: wrapper.state.user?.phone ?? ""))
                case is OwnProfileEffectNavigateBack:
                    onBack()
                default: break
                }
            }
            wrapper.load()
        }
    }

    // MARK: — Sections

    @ViewBuilder
    private var avatarSection: some View {
        ZStack(alignment: .bottomTrailing) {
            HopAvatar(
                name: wrapper.state.user?.fullName ?? "",
                imageURL: wrapper.state.user?.avatarUrl.flatMap { URL(string: $0) },
                size: .xlarge,
                isVerified: wrapper.state.user?.phoneVerified == true
            )

            ZStack {
                Circle()
                    .fill(Color.hopPrimaryLime)
                    .frame(width: 28, height: 28)
                if wrapper.state.isUploadingAvatar {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: Color.hopAuthTextPrimary))
                        .scaleEffect(0.5)
                        .frame(width: 28, height: 28)
                } else {
                    Image(systemName: "camera.fill")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(Color.hopAuthTextPrimary)
                }
            }
            .accessibilityLabel("Edit profile photo")
            .onTapGesture {
                if !wrapper.state.isUploadingAvatar { showPhotoSourceSheet = true }
            }
        }
        .padding(.top, HopSpacing.xl)
        .confirmationDialog("Change profile photo", isPresented: $showPhotoSourceSheet, titleVisibility: .visible) {
            Button("Camera") { showCameraPicker = true }
            Button("Photo Library") { showLibraryPicker = true }
            Button("Cancel", role: .cancel) { }
        }
        .photosPicker(isPresented: $showLibraryPicker, selection: $selectedPhotoItem, matching: .images)
        .sheet(isPresented: $showCameraPicker) {
            CameraPickerView { image in
                showCameraPicker = false
                handleSelectedImage(image)
            }
        }
        .onChange(of: selectedPhotoItem) { _, newItem in
            guard let newItem else { return }
            Task {
                if let data = try? await newItem.loadTransferable(type: Data.self),
                   let uiImage = UIImage(data: data),
                   let jpeg = uiImage.jpegData(compressionQuality: 0.8) {
                    wrapper.uploadAvatar(data: jpeg)
                }
                selectedPhotoItem = nil
            }
        }
    }

    private func handleSelectedImage(_ image: UIImage?) {
        guard let image, let jpeg = image.jpegData(compressionQuality: 0.8) else { return }
        wrapper.uploadAvatar(data: jpeg)
    }

    @ViewBuilder
    private var nameSection: some View {
        if wrapper.state.isEditingName {
            VStack(spacing: HopSpacing.sm) {
                HopTextField(
                    label: "Full name",
                    placeholder: "",
                    text: Binding(
                        get: { wrapper.state.nameDraft },
                        set: { wrapper.nameDraftChanged($0) }
                    ),
                    isEnabled: !wrapper.state.isSavingName,
                    lightSurface: true
                )
                .padding(.horizontal, HopSpacing.md)

                HStack {
                    Spacer()
                    Button(action: wrapper.cancelEditName) {
                        Text("Cancel")
                            .font(.system(size: 14))
                            .foregroundColor(Color.hopAuthTextSecondary)
                    }
                    .disabled(wrapper.state.isSavingName)

                    Spacer().frame(width: HopSpacing.sm)

                    Button(action: wrapper.saveName) {
                        if wrapper.state.isSavingName {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: Color.hopAuthAccent))
                                .scaleEffect(0.8)
                                .frame(width: 16, height: 16)
                        } else {
                            Text("Save")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(Color.hopAuthAccent)
                        }
                    }
                    .disabled(wrapper.state.isSavingName || wrapper.state.nameDraft.trimmingCharacters(in: .whitespaces).isEmpty)
                }
                .padding(.horizontal, HopSpacing.md)
            }
        } else {
            HStack(spacing: HopSpacing.sm) {
                Text(wrapper.state.user?.fullName ?? "")
                    .font(HopFont.headlineSmall(weight: .bold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                Button(action: wrapper.startEditName) {
                    Image(systemName: "pencil")
                        .font(.system(size: 16))
                        .foregroundColor(Color.hopAuthAccent)
                        .frame(width: 44, height: 44)
                }
                .accessibilityLabel("Edit name")
            }
        }
    }

    @ViewBuilder
    private var contactSection: some View {
        VStack(spacing: HopSpacing.xs) {
            ProfileInfoRow(
                icon: "envelope",
                label: wrapper.state.user?.email ?? ""
            )
            phoneRow
        }
    }

    @ViewBuilder
    private var phoneRow: some View {
        let phone = wrapper.state.user?.phone
        let isVerified = wrapper.state.user?.phoneVerified == true
        if let phone = phone {
            HStack(spacing: HopSpacing.sm) {
                Image(systemName: "phone")
                    .font(.system(size: 16))
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .frame(width: 18, height: 18)
                Text(phone)
                    .font(.system(size: 14))
                    .foregroundColor(Color.hopAuthTextSecondary)
                Spacer()
                if isVerified {
                    Image(systemName: "checkmark.seal.fill")
                        .font(.system(size: 16))
                        .foregroundColor(Color.hopSuccess)
                        .accessibilityLabel("Phone verified")
                }
            }
            .padding(.horizontal, HopSpacing.md)
            .padding(.vertical, HopSpacing.xs)
        } else {
            Button(action: wrapper.addPhone) {
                HStack(spacing: HopSpacing.sm) {
                    Image(systemName: "phone")
                        .font(.system(size: 16))
                        .foregroundColor(Color.hopAuthTextSecondary)
                        .frame(width: 18, height: 18)
                    Text("Add phone number")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(Color.hopAuthAccent)
                    Spacer()
                }
                .padding(.horizontal, HopSpacing.md)
                .padding(.vertical, HopSpacing.xs)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Add phone number")
        }
    }

    @ViewBuilder
    private var ratingsSection: some View {
        let driver: Double? = wrapper.state.user?.ratingDriver as? Double
        let passenger: Double? = wrapper.state.user?.ratingPassenger as? Double
        if driver != nil || passenger != nil {
            ProfileSectionDividerBlock {
                ProfileSectionTitle("Ratings")
                Spacer().frame(height: HopSpacing.md)
                VStack(alignment: .leading, spacing: HopSpacing.sm) {
                    if let d = driver { ProfileRatingRow(label: "As driver", rating: d) }
                    if let p = passenger { ProfileRatingRow(label: "As passenger", rating: p) }
                }
                .padding(.horizontal, HopSpacing.md)
            }
        }
    }

    @ViewBuilder
    private var reviewsSection: some View {
        ProfileSectionDividerBlock {
            ProfileSectionTitle("Recent reviews")
            Spacer().frame(height: HopSpacing.md)
            if wrapper.state.reviews.isEmpty {
                Text("No reviews yet")
                    .font(.system(size: 14))
                    .foregroundColor(Color.hopAuthTextSecondary)
                    .padding(.horizontal, HopSpacing.md)
            } else {
                VStack(spacing: HopSpacing.sm) {
                    ForEach(wrapper.state.reviews.prefix(5), id: \.id) { review in
                        ProfileReviewCard(review: review)
                    }
                }
            }
        }
    }

    @ViewBuilder
    private var carSection: some View {
        let isDriver = wrapper.state.user?.roles.contains(where: { ($0 as? UserRole) == UserRole.driver }) ?? false
        if isDriver {
            ProfileSectionDividerBlock {
                HStack {
                    ProfileSectionTitle("Car details")
                    Spacer()
                    Button(action: wrapper.editCar) {
                        Text("Edit")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(Color.hopAuthAccent)
                    }
                    .padding(.trailing, HopSpacing.md)
                }
                Spacer().frame(height: HopSpacing.sm)

                if let car = wrapper.state.carDetails {
                    VStack(spacing: HopSpacing.sm) {
                        CarDetailRow(label: "Make / Model", value: "\(car.make) \(car.model)")
                        CarDetailRow(label: "Year",         value: "\(car.year)")
                        CarDetailRow(label: "Colour",       value: car.colour)
                        CarDetailRow(label: "Plate",        value: car.licensePlate)
                        CarDetailRow(label: "Seats",        value: "\(car.seatsAvailable)")
                    }
                    .padding(HopSpacing.md)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.hopAuthInputSurface)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.horizontal, HopSpacing.md)
                } else {
                    HStack(spacing: HopSpacing.sm) {
                        Image(systemName: "car")
                            .font(.system(size: 18))
                            .foregroundColor(Color.hopAuthTextSecondary)
                        Text("No car added yet")
                            .font(.system(size: 14))
                            .foregroundColor(Color.hopAuthTextSecondary)
                        Spacer()
                    }
                    .padding(HopSpacing.md)
                    .background(Color.hopAuthInputSurface)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.horizontal, HopSpacing.md)
                }
            }
        }
    }
    @ViewBuilder
    private var mobilepaySection: some View {
        let isDriver = wrapper.state.user?.roles.contains(where: { ($0 as? UserRole) == UserRole.driver }) ?? false
        if isDriver {
            ProfileSectionDividerBlock {
                HStack {
                    ProfileSectionTitle("MobilePay number")
                    Spacer()
                    Button(action: wrapper.startEditMobilepay) {
                        Image(systemName: "pencil")
                            .font(.system(size: 16))
                            .foregroundColor(Color.hopAuthAccent)
                            .frame(width: 44, height: 44)
                    }
                    .accessibilityLabel("Edit MobilePay number")
                    .padding(.trailing, HopSpacing.md)
                }

                Spacer().frame(height: HopSpacing.sm)

                if wrapper.state.isEditingMobilepay {
                    VStack(alignment: .leading, spacing: HopSpacing.sm) {
                        HopTextField(
                            label: "8-digit number",
                            placeholder: "e.g. 20123456",
                            text: Binding(
                                get: { wrapper.state.mobilepayDraft },
                                set: { new in
                                    let digits = new.filter { $0.isNumber }
                                    if digits.count <= 8 {
                                        wrapper.mobilepayDraftChanged(digits)
                                    }
                                }
                            ),
                            keyboardType: .numberPad,
                            isEnabled: !wrapper.state.isSavingMobilepay,
                            lightSurface: true
                        )
                        .padding(.horizontal, HopSpacing.md)

                        Text("Passengers will send money here after the ride")
                            .font(.system(size: 12))
                            .foregroundColor(Color.hopAuthTextSecondary)
                            .padding(.horizontal, HopSpacing.md)

                        HStack {
                            Spacer()
                            Button(action: wrapper.cancelEditMobilepay) {
                                Text("Cancel")
                                    .font(.system(size: 14))
                                    .foregroundColor(Color.hopAuthTextSecondary)
                            }
                            .disabled(wrapper.state.isSavingMobilepay)

                            Spacer().frame(width: HopSpacing.sm)

                            Button(action: wrapper.saveMobilepay) {
                                if wrapper.state.isSavingMobilepay {
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: Color.hopAuthAccent))
                                        .scaleEffect(0.8)
                                        .frame(width: 16, height: 16)
                                } else {
                                    Text("Save")
                                        .font(.system(size: 14, weight: .semibold))
                                        .foregroundColor(Color.hopAuthAccent)
                                }
                            }
                            .disabled(wrapper.state.isSavingMobilepay || wrapper.state.mobilepayDraft.count != 8)
                        }
                        .padding(.horizontal, HopSpacing.md)
                    }
                    .padding(.bottom, HopSpacing.sm)
                } else {
                    let number = wrapper.state.user?.mobilepayNumber
                    if let num = number, !num.isEmpty {
                        Text(num)
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(Color.hopAuthTextPrimary)
                            .padding(.horizontal, HopSpacing.md)
                            .padding(.bottom, HopSpacing.sm)
                    } else {
                        Text("Not set — passengers pay you via MobilePay")
                            .font(.system(size: 13))
                            .foregroundColor(Color.hopAuthTextSecondary)
                            .padding(.horizontal, HopSpacing.md)
                            .padding(.bottom, HopSpacing.sm)
                    }
                }
            }
        }
    }
}

// MARK: — Top bar

struct ProfileTopBar: View {
    let title: String
    let onBack: () -> Void
    var trailing: () -> AnyView = { AnyView(EmptyView()) }

    var body: some View {
        HStack(spacing: 0) {
            Button(action: onBack) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 18, weight: .regular))
                    .foregroundColor(Color.hopAuthTextPrimary)
                    .frame(width: 44, height: 44)
            }
            .accessibilityLabel("Back")

            Text(title)
                .font(HopFont.headlineSmall(weight: .semibold))
                .foregroundColor(Color.hopAuthTextPrimary)

            Spacer()

            trailing()
        }
        .padding(.horizontal, HopSpacing.xs)
        .padding(.vertical, HopSpacing.xs)
        .background(Color.hopBackground)
    }
}

// MARK: — Sub-rows

struct ProfileSectionTitle: View {
    let text: String
    init(_ text: String) { self.text = text }
    var body: some View {
        Text(text)
            .font(HopFont.bodyLarge(weight: .semibold))
            .foregroundColor(Color.hopAuthTextPrimary)
            .padding(.horizontal, HopSpacing.md)
    }
}

struct ProfileSectionDividerBlock<Content: View>: View {
    @ViewBuilder var content: Content
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Spacer().frame(height: HopSpacing.lg)
            Rectangle()
                .fill(Color.hopAuthInputSurface)
                .frame(height: 1)
                .padding(.horizontal, HopSpacing.md)
            Spacer().frame(height: HopSpacing.lg)
            content
        }
    }
}

struct ProfileInfoRow: View {
    let icon: String
    let label: String
    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(Color.hopAuthTextSecondary)
                .frame(width: 18, height: 18)
            Text(label)
                .font(.system(size: 14))
                .foregroundColor(Color.hopAuthTextSecondary)
            Spacer()
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, HopSpacing.xs)
    }
}

struct ProfileRatingRow: View {
    let label: String
    let rating: Double
    var body: some View {
        HStack(spacing: HopSpacing.sm) {
            Text(label)
                .font(.system(size: 12))
                .foregroundColor(Color.hopAuthTextSecondary)
                .frame(width: 100, alignment: .leading)
            StarRatingDisplay(rating: rating, starSize: 14)
            Text(String(format: "%.1f", rating))
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(Color.hopAuthTextPrimary)
        }
    }
}

struct ProfileReviewCard: View {
    let review: UserReview
    var body: some View {
        HStack(alignment: .top, spacing: HopSpacing.sm) {
            HopAvatar(name: review.raterName, size: .small)
            VStack(alignment: .leading, spacing: HopSpacing.xs) {
                HStack {
                    Text(review.raterName)
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(Color.hopAuthTextPrimary)
                    Spacer()
                    StarRatingDisplay(rating: Double(review.stars), starSize: 12)
                }
                if let comment = review.comment, !comment.isEmpty {
                    Text(comment)
                        .font(.system(size: 12))
                        .foregroundColor(Color.hopAuthTextSecondary)
                }
            }
        }
        .padding(HopSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.hopAuthInputSurface)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .padding(.horizontal, HopSpacing.md)
    }
}

struct CarDetailRow: View {
    let label: String
    let value: String
    var body: some View {
        HStack {
            Text(label)
                .font(.system(size: 12))
                .foregroundColor(Color.hopAuthTextSecondary)
                .frame(width: 100, alignment: .leading)
            Text(value)
                .font(.system(size: 12, weight: .medium))
                .foregroundColor(Color.hopAuthTextPrimary)
            Spacer()
        }
    }
}

#Preview {
    OwnProfileView(navigate: { _ in }, onBack: {})
}
