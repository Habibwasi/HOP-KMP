import SwiftUI

// MARK: — SH-07 Privacy Policy ────────────────────────────────────────────────
//
// Mirrors PrivacyPolicyScreen.kt. Light background (hopBackground).

struct PrivacyPolicyView: View {
    var onBack: () -> Void

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                PolicyTopBar(title: "Privacy Policy", onBack: onBack)

                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 0) {
                        Spacer().frame(height: HopSpacing.md)

                        PolicySection(
                            title: "Last updated: May 2026",
                            content: "This Privacy Policy explains how Ridly (\"we\", \"us\", or \"our\") collects, uses, and shares information about you when you use our carpooling platform."
                        )
                        PolicySection(
                            title: "1. Information We Collect",
                            content: "We collect information you provide directly to us, such as your name, email address, and phone number when you register an account. We also collect location data when you use the app to find or offer rides, as well as payment-related information (MobilePay phone number) to facilitate ride settlements between passengers and drivers."
                        )
                        PolicySection(
                            title: "2. How We Use Your Information",
                            content: "We use the information we collect to:\n\n• Provide, maintain, and improve our services\n• Match passengers with drivers\n• Facilitate payments between users\n• Send notifications about your bookings and trips\n• Respond to your comments and questions\n• Send you technical notices and updates\n• Monitor and analyse usage patterns"
                        )
                        PolicySection(
                            title: "3. Information Sharing",
                            content: "We share your information only in the following circumstances:\n\n• With other users as necessary to facilitate a ride (e.g., your first name and profile picture are visible to matched passengers/drivers)\n• With service providers who assist us in operating the platform\n• If required by law or to protect the rights and safety of our users\n• In connection with a merger, acquisition, or sale of assets"
                        )
                        PolicySection(
                            title: "4. Location Data",
                            content: "We collect precise location data when you use the app to enable ride matching and route display. Location is only collected while the app is in use. You can disable location access through your device settings, though this will limit the functionality of the app."
                        )
                        PolicySection(
                            title: "5. Data Retention",
                            content: "We retain your personal data for as long as your account is active or as needed to provide services. You may request deletion of your account and associated data by contacting us at support@ridly.dk."
                        )
                        PolicySection(
                            title: "6. Security",
                            content: "We use industry-standard security measures to protect your information. Authentication is handled via Supabase with email verification. However, no method of transmission over the internet is 100% secure."
                        )
                        PolicySection(
                            title: "7. Your Rights (GDPR)",
                            content: "If you are located in the European Economic Area, you have the right to:\n\n• Access your personal data\n• Correct inaccurate data\n• Request deletion of your data\n• Object to or restrict processing\n• Data portability\n\nTo exercise these rights, contact us at support@ridly.dk."
                        )
                        PolicySection(
                            title: "8. Children's Privacy",
                            content: "Our services are not directed to individuals under the age of 18. We do not knowingly collect personal information from children."
                        )
                        PolicySection(
                            title: "9. Contact Us",
                            content: "If you have questions about this Privacy Policy, please contact us at:\n\nRidly\nEmail: support@ridly.dk\nWebsite: ridly.dk"
                        )

                        Spacer().frame(height: HopSpacing.xl)
                    }
                    .padding(.horizontal, HopSpacing.lg)
                }
            }
        }
        .toolbar(.hidden, for: .navigationBar)
    }
}

// MARK: — SH-08 Terms of Service ──────────────────────────────────────────────

struct TermsOfServiceView: View {
    var onBack: () -> Void

    var body: some View {
        ZStack {
            Color.hopBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                PolicyTopBar(title: "Terms of Service", onBack: onBack)

                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 0) {
                        Spacer().frame(height: HopSpacing.md)

                        PolicySection(
                            title: "Last updated: May 2026",
                            content: "Please read these Terms of Service carefully before using the Ridly carpooling platform. By using the service, you agree to be bound by these terms."
                        )
                        PolicySection(
                            title: "1. Acceptance of Terms",
                            content: "By creating an account and using Ridly, you agree to these Terms of Service and our Privacy Policy. If you do not agree to these terms, please do not use our service."
                        )
                        PolicySection(
                            title: "2. Eligibility",
                            content: "You must be at least 18 years of age to use Ridly. By creating an account, you represent and warrant that you meet this requirement and that all registration information you submit is accurate and truthful."
                        )
                        PolicySection(
                            title: "3. Driver Requirements",
                            content: "To offer rides as a driver, you must:\n\n• Hold a valid driver's licence\n• Own or have authorisation to use the vehicle you list\n• Maintain valid vehicle insurance\n• Have a MobilePay account for receiving payments\n\nRidly reserves the right to verify driver eligibility and suspend accounts that do not meet these requirements."
                        )
                        PolicySection(
                            title: "4. Payments",
                            content: "Ridly facilitates direct payments between passengers and drivers via MobilePay. All payment settlement is between the parties involved in the ride. Ridly does not hold or process funds on behalf of users. Disputes regarding payment should first be resolved directly between the passenger and driver."
                        )
                        PolicySection(
                            title: "5. Cancellation Policy",
                            content: "Passengers may cancel a booking before the trip departure. Frequent cancellations may result in account restrictions. Drivers who cancel confirmed trips may also face account restrictions. Neither party is guaranteed a refund or compensation for cancellations — this is subject to the direct agreement between the passenger and driver."
                        )
                        PolicySection(
                            title: "6. User Conduct",
                            content: "You agree not to:\n\n• Provide false or misleading information\n• Use the service for any unlawful purpose\n• Harass, threaten, or intimidate other users\n• Attempt to gain unauthorised access to any part of the service\n• Post or transmit any offensive or harmful content\n\nViolations may result in immediate account termination."
                        )
                        PolicySection(
                            title: "7. Ratings and Reviews",
                            content: "Passengers and drivers may rate each other after a completed trip. Ratings must be honest and based on your actual experience. Ridly reserves the right to remove ratings that violate our community guidelines."
                        )
                        PolicySection(
                            title: "8. Limitation of Liability",
                            content: "Ridly is a platform that connects drivers and passengers. We are not a transportation provider. We are not responsible for the actions of users, the condition of vehicles, or the outcome of rides. To the maximum extent permitted by law, Ridly's liability is limited to the amount you paid for the service in the preceding 12 months."
                        )
                        PolicySection(
                            title: "9. Modifications to Service",
                            content: "Ridly reserves the right to modify, suspend, or discontinue the service at any time. We will provide reasonable notice of significant changes where possible."
                        )
                        PolicySection(
                            title: "10. Governing Law",
                            content: "These terms are governed by and construed in accordance with Danish law. Any disputes shall be subject to the exclusive jurisdiction of the courts of Denmark."
                        )
                        PolicySection(
                            title: "11. Contact",
                            content: "If you have questions about these Terms of Service, please contact us at:\n\nRidly\nEmail: support@ridly.dk\nWebsite: ridly.dk"
                        )

                        Spacer().frame(height: HopSpacing.xl)
                    }
                    .padding(.horizontal, HopSpacing.lg)
                }
            }
        }
        .toolbar(.hidden, for: .navigationBar)
    }
}

// MARK: — Shared components ───────────────────────────────────────────────────

private struct PolicyTopBar: View {
    let title: String
    let onBack: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            Button(action: onBack) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 18, weight: .regular))
                    .foregroundColor(Color.hopAuthTextPrimary)
                    .frame(width: 44, height: 44)
            }
            .accessibilityLabel("Navigate back")

            Text(title)
                .font(HopFont.bodyLarge(weight: .semibold))
                .foregroundColor(Color.hopAuthTextPrimary)

            Spacer()
        }
        .padding(.horizontal, HopSpacing.xs)
        .padding(.vertical, HopSpacing.xs)
        .background(Color.hopBackground)
    }
}

private struct PolicySection: View {
    let title: String
    let content: String

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.system(size: 15, weight: .semibold))
                .foregroundColor(Color.hopAuthTextPrimary)
            Text(content)
                .font(.system(size: 14))
                .foregroundColor(Color.hopAuthTextSecondary)
                .lineSpacing(6)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(.bottom, HopSpacing.lg)
    }
}
