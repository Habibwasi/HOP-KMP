import SwiftUI
import Shared

// MARK: — DR-14 Annual Tax Report Download ────────────────────────────────────

struct TaxReportDownloadView: View {

    var onBack: () -> Void

    @StateObject private var wrapper = TaxViewModelWrapper()

    @State private var year: Int = Calendar.current.component(.year, from: Date()) - 1

    var body: some View {
        let s = wrapper.state

        return ZStack {
            Color.hopBackground.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: HopSpacing.md) {
                    // Year selector
                    HStack {
                        Button { year -= 1; wrapper.loadTaxReport(year: year) } label: {
                            Image(systemName: "chevron.left")
                                .foregroundColor(Color.hopAuthTextPrimary)
                                .padding(8).background(Color.hopCardSurfaceMuted).clipShape(Circle())
                        }.buttonStyle(.plain)
                        Spacer()
                        Text("Tax Year \(year)")
                            .font(HopFont.headlineSmall(weight: .semibold))
                            .foregroundColor(Color.hopAuthTextPrimary)
                        Spacer()
                        Button { year += 1; wrapper.loadTaxReport(year: year) } label: {
                            Image(systemName: "chevron.right")
                                .foregroundColor(Color.hopAuthTextPrimary)
                                .padding(8).background(Color.hopCardSurfaceMuted).clipShape(Circle())
                        }.buttonStyle(.plain)
                    }

                    if s.isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: Color.hopPrimaryLime))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, HopSpacing.xl)
                    } else if let err = s.error {
                        EmptyState(
                            systemImage: "exclamationmark.triangle",
                            headline: "Couldn't load report",
                            subtitle: err,
                            ctaLabel: "Retry",
                            ctaAction: { wrapper.loadTaxReport(year: year) }
                        )
                    } else {
                        VStack(spacing: HopSpacing.sm) {
                            row("Total earnings", oere: Int(s.totalEarningsOere))
                            row("Total deduction", oere: Int(s.totalDeductionOere))
                            Divider().background(Color.hopAuthTextSecondary.opacity(0.2))
                            row("Total taxable", oere: Int(s.totalTaxableOere), highlight: true)
                        }
                        .padding(HopSpacing.md)
                        .background(Color.hopCardSurfaceMuted)
                        .clipShape(RoundedRectangle(cornerRadius: 16))

                        if let url = s.reportUrl {
                            HopButton(text: "Download PDF", variant: .primary) {
                                if let u = URL(string: url) { UIApplication.shared.open(u) }
                            }
                            HopButton(text: "Share", variant: .ghost) {
                                if let u = URL(string: url) {
                                    let av = UIActivityViewController(activityItems: [u], applicationActivities: nil)
                                    UIApplication.shared.connectedScenes
                                        .compactMap { ($0 as? UIWindowScene)?.keyWindow }
                                        .first?.rootViewController?.present(av, animated: true)
                                }
                            }
                        } else {
                            HopButton(text: "Generate report URL", variant: .primary) {
                                wrapper.getReportUrl(year: year)
                            }
                        }
                    }
                }
                .padding(HopSpacing.md)
            }
        }
        .task {
            wrapper.startObserving { effect in
                if let open = effect as? TaxEffectOpenReportUrl, let u = URL(string: open.url) {
                    UIApplication.shared.open(u)
                }
            }
            wrapper.loadTaxReport(year: year)
        }
    .safeAreaInset(edge: .top, spacing: 0) {
        DriverTopBar(title: "Tax report", onBack: onBack)
            .background(Color.hopBackground)
    }
    }

    private func row(_ label: String, oere: Int, highlight: Bool = false) -> some View {
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
