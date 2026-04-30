import SwiftUI
import PhotosUI
import Shared

// MARK: — DR-03 Licence Upload (Step 2 of Enable-Driver) ──────────────────────
// Mirrors the Android licence-upload step. Uses iOS PhotosPicker (16+) so the
// driver can attach a real photo of their licence; we then simulate the upload
// (the actual upload endpoint is wired separately by DriverRepository).

struct LicenceUploadView: View {

    var onNavigateBack: () -> Void
    var onNavigateNext: () -> Void  // → DR-04 ReviewPending

    @State private var pickedItem: PhotosPickerItem? = nil
    @State private var pickedImage: Image? = nil
    @State private var isUploading: Bool = false
    @State private var uploaded:    Bool = false

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            VStack(alignment: .leading, spacing: HopSpacing.md) {
                Text("Step 2 of 3 · Licence")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopAuthTextSecondary)
                Text("Upload your driving licence")
                    .font(HopFont.headlineMedium(weight: .bold))
                    .foregroundColor(Color.hopAuthTextPrimary)
                Text("We need a clear photo of the front of your Danish driving licence. The image is reviewed by our team within 24 hours.")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopAuthTextSecondary)

                Spacer().frame(height: HopSpacing.xs)

                // ── Picker tile ────────────────────────────────────────────
                PhotosPicker(selection: $pickedItem, matching: .images, photoLibrary: .shared()) {
                    ZStack {
                        if let pickedImage {
                            pickedImage
                                .resizable()
                                .scaledToFill()
                                .frame(maxWidth: .infinity, minHeight: 220, maxHeight: 220)
                                .clipped()
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                            VStack {
                                Spacer()
                                HStack {
                                    Spacer()
                                    Label(uploaded ? "Uploaded" : (isUploading ? "Uploading…" : "Replace"),
                                          systemImage: uploaded ? "checkmark.circle.fill" : "arrow.triangle.2.circlepath")
                                        .font(HopFont.labelSmall(weight: .semibold))
                                        .foregroundColor(.white)
                                        .padding(.horizontal, HopSpacing.sm)
                                        .padding(.vertical, 6)
                                        .background(Color.black.opacity(0.6))
                                        .clipShape(Capsule())
                                        .padding(HopSpacing.sm)
                                }
                            }
                        } else {
                            VStack(spacing: HopSpacing.sm) {
                                Image(systemName: "photo.on.rectangle.angled")
                                    .font(.system(size: 56))
                                    .foregroundColor(Color.hopAuthTextSecondary)
                                Text("Tap to choose a photo")
                                    .font(HopFont.labelMedium(weight: .semibold))
                                    .foregroundColor(Color.hopAuthTextPrimary)
                                Text("JPEG or PNG · max 10 MB")
                                    .font(HopFont.bodySmall())
                                    .foregroundColor(Color.hopAuthTextSecondary)
                            }
                            .frame(maxWidth: .infinity, minHeight: 220)
                            .background(Color.hopCardSurfaceMuted)
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(style: StrokeStyle(lineWidth: 2, dash: [6]))
                                    .foregroundColor(Color.hopAuthTextSecondary.opacity(0.4))
                            )
                        }
                    }
                }
                .buttonStyle(.plain)

                // ── Reassurance row ────────────────────────────────────────
                HStack(alignment: .top, spacing: HopSpacing.xs) {
                    Image(systemName: "lock.shield.fill")
                        .foregroundColor(Color.hopPrimaryGreen)
                    Text("Stored encrypted. Only verified Hop reviewers can see it.")
                        .font(HopFont.bodySmall())
                        .foregroundColor(Color.hopAuthTextSecondary)
                }

                Spacer()

                HopButton(
                    text: uploaded ? "Continue" : (isUploading ? "Uploading…" : "Upload & continue"),
                    variant: .primary,
                    isLoading: isUploading,
                    isEnabled: pickedImage != nil,
                    action: {
                        if uploaded {
                            onNavigateNext()
                        } else {
                            beginUpload()
                        }
                    }
                )
            }
            .padding(HopSpacing.md)
        }
        .onChange(of: pickedItem) { _, newItem in
            uploaded = false
            Task {
                guard let newItem,
                      let data = try? await newItem.loadTransferable(type: Data.self),
                      let ui = UIImage(data: data) else {
                    pickedImage = nil
                    return
                }
                pickedImage = Image(uiImage: ui)
            }
        }
        .safeAreaInset(edge: .top, spacing: 0) {
            DriverTopBar(title: "Upload licence", onBack: onNavigateBack)
                .background(Color.hopBackground)
        }
    }

    private func beginUpload() {
        guard !isUploading else { return }
        isUploading = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
            isUploading = false
            uploaded = true
        }
    }
}
