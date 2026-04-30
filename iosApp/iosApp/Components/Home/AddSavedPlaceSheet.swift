import SwiftUI
import Shared

/// PA-01 — Modal sheet for creating a new saved place.
/// Mirrors composeApp `AddSavedPlaceSheet.kt`. Host is responsible for
/// dispatching the upsert + dismissing on save.
struct AddSavedPlaceSheet: View {
    let onSave: (_ label: String, _ address: String, _ kind: SavedPlaceKind?) -> Void
    let onDismiss: () -> Void

    @State private var label: String = ""
    @State private var address: String = ""
    @State private var selectedKind: SavedPlaceKind? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: HopSpacing.md) {
            HStack {
                Text("Add saved place")
                    .font(HopFont.titleMedium())
                    .foregroundColor(Color.hopAuthTextPrimary)
                Spacer()
                Button(action: onDismiss) {
                    Image(systemName: "xmark")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(Color.hopAuthTextSecondary)
                        .frame(width: 28, height: 28)
                }
                .buttonStyle(.plain)
            }
            .padding(.top, HopSpacing.md)

            HStack(spacing: HopSpacing.sm) {
                KindChip(label: "Home", systemImage: "house",
                         selected: selectedKind == .home) {
                    selectedKind = .home
                }
                KindChip(label: "Work", systemImage: "briefcase",
                         selected: selectedKind == .work) {
                    selectedKind = .work
                }
                KindChip(label: "Other", systemImage: "mappin",
                         selected: selectedKind == .custom) {
                    selectedKind = .custom
                }
            }

            SheetField(value: $label, placeholder: "Label (e.g. Home, Mom's place)")
            SheetField(value: $address, placeholder: "Address — street, city")

            HopButton(
                text: "Save",
                variant: .primary,
                isEnabled: !label.trimmingCharacters(in: .whitespaces).isEmpty
                    && !address.trimmingCharacters(in: .whitespaces).isEmpty
            ) {
                onSave(
                    label.trimmingCharacters(in: .whitespaces),
                    address.trimmingCharacters(in: .whitespaces),
                    selectedKind
                )
                onDismiss()
            }
            Spacer(minLength: 0)
        }
        .padding(.horizontal, HopSpacing.md)
        .padding(.bottom, HopSpacing.md)
        .background(Color.hopCardSurface)
    }
}

private struct KindChip: View {
    let label: String
    let systemImage: String
    let selected: Bool
    let onTap: () -> Void

    var body: some View {
        let bg: Color = selected ? Color.hopPrimaryLime : .clear
        let border: Color = selected ? Color.hopPrimaryLime : Color.hopCardBorder
        let fg: Color = selected ? Color.hopAuthTextPrimary : Color.hopAuthTextSecondary

        Button(action: onTap) {
            HStack(spacing: 6) {
                Image(systemName: systemImage)
                    .font(.system(size: 12, weight: .medium))
                Text(label)
                    .font(HopFont.labelMedium(weight: selected ? .semibold : .medium))
            }
            .foregroundColor(fg)
            .padding(.horizontal, HopSpacing.sm)
            .padding(.vertical, 6)
            .background(bg)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(border, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 20))
        }
        .buttonStyle(.plain)
    }
}

private struct SheetField: View {
    @Binding var value: String
    let placeholder: String

    var body: some View {
        TextField("", text: $value, prompt:
            Text(placeholder).foregroundColor(Color(white: 0.69))
        )
        .font(HopFont.bodyMedium())
        .foregroundColor(Color.hopAuthTextPrimary)
        .padding(.horizontal, HopSpacing.md)
        .padding(.vertical, 14)
        .background(Color.hopCardSurfaceMuted)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.hopCardBorder, lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// SKIE exposes Kotlin enum entries with lowercased names; SavedPlaceKind cases
// are .home / .work / .custom on the Swift side.
