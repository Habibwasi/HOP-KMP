package com.example.hop.ui.screens.shared

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
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
                body = "This Privacy Policy explains how Ridly (\"we\", \"us\", or \"our\") collects, uses, and shares information about you when you use our carpooling platform.",
            )

            PolicySection(
                title = "1. Information We Collect",
                body = "We collect information you provide directly to us, such as your name, email address, and phone number when you register an account. We also collect location data when you use the app to find or offer rides, as well as payment-related information (MobilePay phone number) to facilitate ride settlements between passengers and drivers.",
            )

            PolicySection(
                title = "2. How We Use Your Information",
                body = "We use the information we collect to:\n\n• Provide, maintain, and improve our services\n• Match passengers with drivers\n• Facilitate payments between users\n• Send notifications about your bookings and trips\n• Respond to your comments and questions\n• Send you technical notices and updates\n• Monitor and analyse usage patterns",
            )

            PolicySection(
                title = "3. Information Sharing",
                body = "We share your information only in the following circumstances:\n\n• With other users as necessary to facilitate a ride (e.g., your first name and profile picture are visible to matched passengers/drivers)\n• With service providers who assist us in operating the platform\n• If required by law or to protect the rights and safety of our users\n• In connection with a merger, acquisition, or sale of assets",
            )

            PolicySection(
                title = "4. Location Data",
                body = "We collect precise location data when you use the app to enable ride matching and route display. Location is only collected while the app is in use. You can disable location access through your device settings, though this will limit the functionality of the app.",
            )

            PolicySection(
                title = "5. Data Retention",
                body = "We retain your personal data for as long as your account is active or as needed to provide services. You may request deletion of your account and associated data by contacting us at support@ridly.dk.",
            )

            PolicySection(
                title = "6. Security",
                body = "We use industry-standard security measures to protect your information. Authentication is handled via Supabase with email verification. However, no method of transmission over the internet is 100% secure.",
            )

            PolicySection(
                title = "7. Your Rights (GDPR)",
                body = "If you are located in the European Economic Area, you have the right to:\n\n• Access your personal data\n• Correct inaccurate data\n• Request deletion of your data\n• Object to or restrict processing\n• Data portability\n\nTo exercise these rights, contact us at support@ridly.dk.",
            )

            PolicySection(
                title = "8. Children's Privacy",
                body = "Our services are not directed to individuals under the age of 18. We do not knowingly collect personal information from children.",
            )

            PolicySection(
                title = "9. Contact Us",
                body = "If you have questions about this Privacy Policy, please contact us at:\n\nRidly\nEmail: support@ridly.dk\nWebsite: ridly.dk",
            )

            Spacer(modifier = Modifier.height(HopSpacing.xl))
        }
    }
}

@Composable
internal fun PolicySection(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = HopColors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            color = HopColors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 22.sp,
        )
        Spacer(modifier = Modifier.height(HopSpacing.lg))
    }
}
