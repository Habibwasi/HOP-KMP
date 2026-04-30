import SwiftUI
import Shared

// MARK: — DR-13 Tax Dashboard ─────────────────────────────────────────────────

struct TaxDashboardView: View {

    var onOpenAnnualReport: () -> Void
    var onBack: () -> Void

    @StateObject private var wrapper = TaxViewModelWrapper()

    private static let monthNames = [
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    ]

    var body: some View {
        let s = wrapper.state

        return ZStack {
            Color.hopBackground.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: HopSpacing.md) {
                    // ── Month selector ───────────────────────────────────────
                    HStack {
                        Button { wrapper.previousMonth() } label: {
                            Image(systemName: "chevron.left")
                                .foregroundColor(Color.hopAuthTextPrimary)
                                .padding(8).background(Color.hopCardSurfaceMuted).clipShape(Circle())
                        }.buttonStyle(.plain)

                        Spacer()
                        Text(monthLabel(s))
                            .font(HopFont.headlineSmall(weight: .semibold))
                            .foregroundColor(Color.hopAuthTextPrimary)
                        Spacer()

                        Button { wrapper.nextMonth() } label: {
                            Image(systemName: "chevron.right")
                                .foregroundColor(Color.hopAuthTextPrimary)
                                .padding(8).background(Color.hopCardSurfaceMuted).clipShape(Circle())
                        }.buttonStyle(.plain)
                    }

                    if s.isLoadingDashboard {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, HopSpacing.xl)
                    } else if let summary = s.summary {
                        // ── Hero earnings card ───────────────────────────────
                        VStack(alignment: .leading, spacing: HopSpacing.xs) {
                            Text("Gross earnings")
                                .font(HopFont.labelSmall())
                                .foregroundColor(Color.hopAuthTextPrimary.opacity(0.7))
                            Text("DKK \(Int(summary.grossOere) / 100)")
                                .font(HopFont.displayLarge(weight: .bold))
                                .foregroundColor(Color.hopAuthTextPrimary)
                        }
                        .padding(HopSpacing.md)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.hopPrimaryLime)
                        .clipShape(RoundedRectangle(cornerRadius: 16))

                        // ── Breakdown ────────────────────────────────────────
                        VStack(spacing: HopSpacing.sm) {
                            taxRow("Befordringsfradrag (DKK 2.28/km)", oere: Int(summary.befordringsfradragOere))
                            taxRow("Taxable amount", oere: Int(summary.taxableOere))
                            Divider().background(Color.hopAuthTextSecondary.opacity(0.2))
                            taxRow("Estimated tax", oere: Int(summary.estimatedTaxOere), highlight: true)
                        }
                        .padding(HopSpacing.md)
                        .background(Color.hopCardSurfaceMuted)
                        .clipShape(RoundedRectangle(cornerRadius: 16))

                        Text("Estimates only. Verify with SKAT for filing.")
                            .font(HopFont.bodySmall())
                            .foregroundColor(Color.hopAuthTextSecondary)
                    } else if let err = s.error {
                        EmptyState(
                            systemImage: "exclamationmark.triangle",
                            headline: "Couldn't load summary",
                            subtitle: err
                        )
                    } else {
                        EmptyState(
                            systemImage: "chart.bar.doc.horizontal",
                            headline: "No data yet",
                            subtitle: "Complete trips to see your monthly summary."
                        )
                    }

                    HopButton(text: "Open annual tax report", variant: .ghost, action: onOpenAnnualReport)
                        .padding(.top, HopSpacing.md)
                }
                .padding(HopSpacing.md)
            }
        }
        .task {
            wrapper.startObserving { _ in }
            let now = Date()
            let cal = Calendar.current
            wrapper.loadDashboard(year: cal.component(.year, from: now), month: cal.component(.month, from: now))
        }
    .safeAreaInset(edge: .top, spacing: 0) {
        DriverTopBar(title: "Tax dashboard", onBack: onBack)
            .background(Color.hopBackground)
    }
    }

    private func monthLabel(_ s: TaxUiState) -> String {
        let m = Int(s.selectedMonth)
        let y = Int(s.selectedYear)
        guard m >= 1, m <= 12 else { return "—" }
        return "\(Self.monthNames[m - 1]) \(y)"
    }

    private func taxRow(_ label: String, oere: Int, highlight: Bool = false) -> some View {
        HStack {
            Text(label)
                .font(HopFont.bodyMedium())
                .foregroundColor(Color.hopAuthTextSecondary)
            Spacer()
            Text("DKK \(oere / 100)")
                .font(highlight ? HopFont.headlineSmall(weight: .bold) : HopFont.bodyMedium(weight: .semibold))
                .foregroundColor(highlight ? Color.hopPrimaryLime : Color.hopAuthTextPrimary)
        }
    }
}
