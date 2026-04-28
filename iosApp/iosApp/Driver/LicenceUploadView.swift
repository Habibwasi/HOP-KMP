import SwiftUI
import Shared

// MARK: — DR-03 Licence Upload (Step 2 of Enable-Driver) ──────────────────────
// Mirrors the Android licence-upload step. The actual file picker is platform-
// specific; for now we present a placeholder card that simulates upload and
// proceeds to DR-04.

struct LicenceUploadView: View {

    var onNavigateBack: () -> Void
    var onNavigateNext: () -> Void  // → DR-04 ReviewPending

    @State private var isUploading: Bool = false
    @State private var uploaded:    Bool = false

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            VStack(alignment: .leading, spacing: HopSpacing.md) {
                Text("Step 2 of 3 · Licence")
                    .font(HopFont.bodySmall())
                    .foregroundColor(Color.hopTextSecondary)
                Text("Upload your driving licence")
                    .font(HopFont.headlineMedium(weight: .bold))
                    .foregroundColor(Color.hopTextPrimary)
                Text("We need a clear photo of the front of your Danish driving licence.")
                    .font(HopFont.bodyMedium())
                    .foregroundColor(Color.hopTextSecondary)

                Spacer().frame(height: HopSpacing.md)

                VStack(spacing: HopSpacing.sm) {
                    Image(systemName: uploaded ? "checkmark.circle.fill" : "photo.on.rectangle.angled")
                        .font(.system(size: 56))
                        .foregroundColor(uploaded ? Color.hopSuccess : Color.hopTextSecondary)
                    Text(uploaded ? "Licence uploaded" : "Tap to upload")
                        .font(HopFont.labelMedium(weight: .semibold))
                        .foregroundColor(Color.hopTextPrimary)
                }
                .frame(maxWidth: .infinity, minHeight: 180)
                .background(Color.hopSurfaceElevated)
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(style: StrokeStyle(lineWidth: 2, dash: [6]))
                        .foregroundColor(Color.hopTextSecondary.opacity(0.4))
                )
                .onTapGesture {
                    isUploading = true
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                        isUploading = false
                        uploaded = true
                    }
                }

                Spacer()

                HopButton(
                    text: "Continue",
                    variant: .primary,
                    isLoading: isUploading,
                    isEnabled: uploaded,
                    action: onNavigateNext
                )
            }
            .padding(HopSpacing.md)
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onNavigateBack) {
                    Image(systemName: "arrow.left").foregroundColor(Color.hopTextPrimary)
                }
            }
            ToolbarItem(placement: .principal) {
                Text("Become a Driver").font(HopFont.bodyLarge(weight: .semibold)).foregroundColor(Color.hopTextPrimary)
            }
        }
        .toolbarBackground(Color.hopSurface, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
    }
}
