import { Injectable } from '@nestjs/common'

export interface FaqItem {
  id: string
  topic: string
  question: string
  answer: string
}

@Injectable()
export class HelpCenterService {
  private readonly faqs: FaqItem[] = [
    // ── Bookings ────────────────────────────────────────────────────────────
    {
      id: 'bk-01',
      topic: 'Bookings',
      question: 'How do I book a ride?',
      answer:
        'Search for trips by entering your origin, destination, and travel date on the home screen. Browse available trips and tap one to see details. If it suits you, tap "Book" and confirm. Once the driver accepts, your booking is confirmed and you\'ll receive a notification.',
    },
    {
      id: 'bk-02',
      topic: 'Bookings',
      question: 'Can I cancel a booking?',
      answer:
        'Yes. Open "My Trips", find the booking you want to cancel, and tap "Cancel Booking". Cancellations more than 2 hours before departure are free. Late cancellations may affect your reliability rating.',
    },
    {
      id: 'bk-03',
      topic: 'Bookings',
      question: 'Can I modify a booking after it is confirmed?',
      answer:
        'Bookings cannot be edited after confirmation. If your plans change, cancel the existing booking and search for a new trip. Always do this as early as possible so the driver can offer the seat to someone else.',
    },
    {
      id: 'bk-04',
      topic: 'Bookings',
      question: 'What happens if the driver cancels my booking?',
      answer:
        'You will receive a push notification immediately if a driver cancels. Any payment already made via MobilePay is refunded within 1–3 business days. You can then search for another available trip.',
    },
    {
      id: 'bk-05',
      topic: 'Bookings',
      question: 'How many seats can I book at once?',
      answer:
        'You can book up to the number of available seats on a trip — usually between 1 and 4. Select the number of seats on the trip detail screen before confirming.',
    },

    // ── Payments & pricing ──────────────────────────────────────────────────
    {
      id: 'pay-01',
      topic: 'Payments & pricing',
      question: 'How does payment work?',
      answer:
        'Payment is handled via MobilePay. After your trip is completed, the driver will send a MobilePay request for your share of the cost. You settle directly in the MobilePay app. Hop facilitates the price agreement but does not hold any funds.',
    },
    {
      id: 'pay-02',
      topic: 'Payments & pricing',
      question: 'How is the trip price calculated?',
      answer:
        'Drivers set their own price when posting a trip. The price shown is per seat. Hop suggests a fair price based on distance and current fuel costs, but the final price is always up to the driver.',
    },
    {
      id: 'pay-03',
      topic: 'Payments & pricing',
      question: 'What is the Hop service fee?',
      answer:
        'Hop currently charges no platform fee to passengers. Drivers keep 100% of the agreed price. This may change in the future — any fees will be clearly communicated in advance.',
    },
    {
      id: 'pay-04',
      topic: 'Payments & pricing',
      question: 'What if a passenger refuses to pay?',
      answer:
        'If a passenger does not settle via MobilePay within the agreed window, you can mark the payment as disputed in the app. Repeated non-payment leads to account review. Contact support@ridly.dk if you need help resolving a dispute.',
    },
    {
      id: 'pay-05',
      topic: 'Payments & pricing',
      question: 'Can I get a refund?',
      answer:
        'Payments are made directly between users via MobilePay, so refunds are also handled directly. If the driver cancels or the trip does not take place, ask the driver to send a refund via MobilePay. Contact us at support@ridly.dk if you cannot resolve it.',
    },

    // ── Driver onboarding ────────────────────────────────────────────────────
    {
      id: 'dr-01',
      topic: 'Driver onboarding',
      question: 'How do I become a driver on Hop?',
      answer:
        'Switch to Driver mode on the home screen, then tap "Become a driver". Enter your car details (make, model, year, colour, and registration plate) and your MobilePay phone number. Your profile will be reviewed and activated within 24 hours.',
    },
    {
      id: 'dr-02',
      topic: 'Driver onboarding',
      question: 'How do I post a trip?',
      answer:
        'In Driver mode, tap "Post a trip". Enter the origin, destination, departure date and time, number of available seats, and your price. Choose your pricing model (fixed or per-seat) and publish. Your trip will appear in search results immediately.',
    },
    {
      id: 'dr-03',
      topic: 'Driver onboarding',
      question: 'How do I set up MobilePay for receiving payments?',
      answer:
        'During driver onboarding you enter the phone number linked to your personal MobilePay account. Passengers will use this number to pay you after each trip. Make sure the number is correct — you can update it in Settings → Edit profile.',
    },
    {
      id: 'dr-04',
      topic: 'Driver onboarding',
      question: 'Can I edit or cancel a trip I have posted?',
      answer:
        'You can cancel a trip from "My Trips" (driver view) as long as no passengers have booked it yet. Once passengers are confirmed, cancelling the trip will notify them and may affect your reliability rating. You cannot edit trip details after posting — cancel and repost instead.',
    },
    {
      id: 'dr-05',
      topic: 'Driver onboarding',
      question: 'Do I need a special licence to drive on Hop?',
      answer:
        'No special licence is needed — a standard Danish driving licence is sufficient. Hop is a carpooling platform, not a taxi or ride-hailing service. Drivers share costs with passengers for trips they were already planning to take.',
    },

    // ── Account & profile ────────────────────────────────────────────────────
    {
      id: 'acc-01',
      topic: 'Account & profile',
      question: 'How do I verify my email address?',
      answer:
        'After signing up, a verification email is sent to your address. Open the email and tap the confirmation link. Once confirmed you will be taken back to the app and logged in automatically. Check your spam folder if the email does not arrive within a few minutes.',
    },
    {
      id: 'acc-02',
      topic: 'Account & profile',
      question: 'How do I change my password?',
      answer:
        'Go to Settings → Change password. Enter your current password and then your new one. You can also use "Forgot password" on the login screen to reset it via email.',
    },
    {
      id: 'acc-03',
      topic: 'Account & profile',
      question: 'How do I update my profile photo?',
      answer:
        'Go to your profile by tapping your avatar in the top corner. Tap the camera icon on your profile picture and choose a photo from your library or take a new one. A clear, recent photo helps drivers and passengers trust each other.',
    },
    {
      id: 'acc-04',
      topic: 'Account & profile',
      question: 'How do I delete my account?',
      answer:
        'Account deletion is handled by our support team to ensure all your data is removed correctly in compliance with GDPR. Send a deletion request to support@ridly.dk from your registered email address. Your account will be removed within 30 days.',
    },

    // ── Safety & trust ───────────────────────────────────────────────────────
    {
      id: 'sf-01',
      topic: 'Safety & trust',
      question: 'How does the rating system work?',
      answer:
        'After every completed trip, passengers rate drivers and drivers rate passengers (1–5 stars). Ratings are averaged and displayed on profiles. Consistently low ratings may lead to account review. Always rate honestly — it helps keep the community safe.',
    },
    {
      id: 'sf-02',
      topic: 'Safety & trust',
      question: 'How do I report another user?',
      answer:
        'Open the user\'s profile, scroll down, and tap "Report user". Select the reason and add any relevant details. Our team reviews all reports within 24 hours. For urgent safety concerns, contact us directly at support@ridly.dk.',
    },
    {
      id: 'sf-03',
      topic: 'Safety & trust',
      question: 'What safety measures does Hop have in place?',
      answer:
        'All users must verify their email. Drivers are reviewed before activation. Profiles show ratings and trip history. In-app chat lets you communicate without sharing personal numbers. If anything feels unsafe during a trip, trust your instincts and contact emergency services (112) first.',
    },
    {
      id: 'sf-04',
      topic: 'Safety & trust',
      question: 'Is my personal information shared with other users?',
      answer:
        'Only your first name, profile picture, and star rating are visible to matched passengers or drivers. Your full name, email, phone number, and payment details are never shared. See our Privacy Policy for full details.',
    },

    // ── App & technical issues ───────────────────────────────────────────────
    {
      id: 'tech-01',
      topic: 'App & technical issues',
      question: 'I am not receiving push notifications. What should I do?',
      answer:
        'Go to your phone\'s Settings → Notifications → Hop and make sure notifications are enabled. Inside the app, go to Settings and check that "Push notifications" is toggled on. If the problem persists, log out and back in to re-register the notification token.',
    },
    {
      id: 'tech-02',
      topic: 'App & technical issues',
      question: 'The app is showing an error or crashing. What should I do?',
      answer:
        'Try closing and reopening the app. If the issue continues, check that you have the latest version installed from the App Store or Google Play. If the problem persists, report it to support@ridly.dk with a description of what you were doing and what error you saw.',
    },
    {
      id: 'tech-03',
      topic: 'App & technical issues',
      question: 'Why can I not find any trips in my area?',
      answer:
        'Hop is growing, so availability varies by region. Try widening your search by using nearby cities or being flexible with your departure date. You can also set up a Search Alert — we will notify you when a matching trip is posted.',
    },
    {
      id: 'tech-04',
      topic: 'App & technical issues',
      question: 'The map or location is not working correctly.',
      answer:
        'Make sure Hop has permission to access your location in your phone\'s settings. If the wrong address appears, you can tap the search bar and type the address manually instead of using the detected location.',
    },
  ]

  getFaqs(): { faqs: FaqItem[] } {
    return { faqs: this.faqs }
  }
}
