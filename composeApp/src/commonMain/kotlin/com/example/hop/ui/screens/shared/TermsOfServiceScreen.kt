package com.example.hop.ui.screens.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Terms of Service",
                        color = HopColors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = HopColors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HopColors.background,
                ),
            )
        },
        containerColor = HopColors.background,
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = HopSpacing.lg)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            Spacer(modifier = Modifier.height(HopSpacing.md))

            PolicySection(
                title = "Last updated: May 2026",
                body = "Please read these Terms of Service carefully before using the Ridly carpooling platform. By using the service, you agree to be bound by these terms.",
            )

            PolicySection(
                title = "1. Acceptance of Terms",
                body = "By creating an account and using Ridly, you agree to these Terms of Service and our Privacy Policy. If you do not agree to these terms, please do not use our service.",
            )

            PolicySection(
                title = "2. Eligibility",
                body = "You must be at least 18 years of age to use Ridly. By creating an account, you represent and warrant that you meet this requirement and that all registration information you submit is accurate and truthful.",
            )

            PolicySection(
                title = "3. Driver Requirements",
                body = "To offer rides as a driver, you must:\n\n• Hold a valid driver's licence\n• Own or have authorisation to use the vehicle you list\n• Maintain valid vehicle insurance\n• Have a MobilePay account for receiving payments\n\nRidly reserves the right to verify driver eligibility and suspend accounts that do not meet these requirements.",
            )

            PolicySection(
                title = "4. Payments",
                body = "Ridly facilitates direct payments between passengers and drivers via MobilePay. All payment settlement is between the parties involved in the ride. Ridly does not hold or process funds on behalf of users. Disputes regarding payment should first be resolved directly between the passenger and driver.",
            )

            PolicySection(
                title = "5. Cancellation Policy",
                body = "Passengers may cancel a booking before the trip departure. Frequent cancellations may result in account restrictions. Drivers who cancel confirmed trips may also face account restrictions. Neither party is guaranteed a refund or compensation for cancellations — this is subject to the direct agreement between the passenger and driver.",
            )

            PolicySection(
                title = "6. User Conduct",
                body = "You agree not to:\n\n• Provide false or misleading information\n• Use the service for any unlawful purpose\n• Harass, threaten, or intimidate other users\n• Attempt to gain unauthorised access to any part of the service\n• Post or transmit any offensive or harmful content\n\nViolations may result in immediate account termination.",
            )

            PolicySection(
                title = "7. Ratings and Reviews",
                body = "Passengers and drivers may rate each other after a completed trip. Ratings must be honest and based on your actual experience. Ridly reserves the right to remove ratings that violate our community guidelines.",
            )

            PolicySection(
                title = "8. Limitation of Liability",
                body = "Ridly is a platform that connects drivers and passengers. We are not a transportation provider. We are not responsible for the actions of users, the condition of vehicles, or the outcome of rides. To the maximum extent permitted by law, Ridly's liability is limited to the amount you paid for the service in the preceding 12 months.",
            )

            PolicySection(
                title = "9. Modifications to Service",
                body = "Ridly reserves the right to modify, suspend, or discontinue the service at any time. We will provide reasonable notice of significant changes where possible.",
            )

            PolicySection(
                title = "10. Governing Law",
                body = "These terms are governed by and construed in accordance with Danish law. Any disputes shall be subject to the exclusive jurisdiction of the courts of Denmark.",
            )

            PolicySection(
                title = "11. Contact",
                body = "If you have questions about these Terms of Service, please contact us at:\n\nRidly\nEmail: support@ridly.dk\nWebsite: ridly.dk",
            )

            Spacer(modifier = Modifier.height(HopSpacing.xl))
        }
    }
}
