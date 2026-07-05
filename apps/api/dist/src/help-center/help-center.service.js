"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.HelpCenterService = void 0;
const common_1 = require("@nestjs/common");
let HelpCenterService = class HelpCenterService {
    faqs = [
        {
            id: 'bk-01',
            topic: 'Bookings',
            question: 'How do I book a ride?',
            answer: 'Search for trips by entering your origin, destination, and travel date on the home screen. Browse available trips and tap one to see details. If it suits you, tap "Book" and confirm. Once the driver accepts, your booking is confirmed and you\'ll receive a notification.',
        },
        {
            id: 'bk-02',
            topic: 'Bookings',
            question: 'Can I cancel a booking?',
            answer: 'Yes. Open "My Trips", find the booking you want to cancel, and tap "Cancel Booking". Cancellations more than 2 hours before departure are free. Late cancellations may affect your reliability rating.',
        },
        {
            id: 'bk-03',
            topic: 'Bookings',
            question: 'Can I modify a booking after it is confirmed?',
            answer: 'Bookings cannot be edited after confirmation. If your plans change, cancel the existing booking and search for a new trip. Always do this as early as possible so the driver can offer the seat to someone else.',
        },
        {
            id: 'bk-04',
            topic: 'Bookings',
            question: 'What happens if the driver cancels my booking?',
            answer: 'You will receive a push notification immediately if a driver cancels. Payment is always handled directly between you and the driver via MobilePay, so contact the driver to arrange a refund if you have already paid. You can then search for another available trip.',
        },
        {
            id: 'bk-05',
            topic: 'Bookings',
            question: 'How many seats can I book at once?',
            answer: 'You can book up to the number of available seats on a trip — usually between 1 and 4. Select the number of seats on the trip detail screen before confirming.',
        },
        {
            id: 'pay-01',
            topic: 'Payments & pricing',
            question: 'How does payment work?',
            answer: 'Payment is always arranged directly between the passenger and driver — Ridly does not handle, process, or hold any money. After your trip, the driver will typically request payment via MobilePay. You settle directly with each other outside the app.',
        },
        {
            id: 'pay-02',
            topic: 'Payments & pricing',
            question: 'How is the trip price calculated?',
            answer: 'Drivers set their own price when posting a trip. The price shown is per seat. Ridly suggests a fair price based on distance and current fuel costs, but the final price is always up to the driver.',
        },
        {
            id: 'pay-03',
            topic: 'Payments & pricing',
            question: 'What is the Ridly service fee?',
            answer: 'Ridly currently charges no platform fee to passengers. Drivers keep 100% of the agreed price. This may change in the future — any fees will be clearly communicated in advance.',
        },
        {
            id: 'pay-04',
            topic: 'Payments & pricing',
            question: 'What if a passenger refuses to pay?',
            answer: 'If a passenger does not settle via MobilePay within the agreed window, you can mark the payment as disputed in the app. Repeated non-payment leads to account review. Contact support@ridly.dk if you need help resolving a dispute.',
        },
        {
            id: 'pay-05',
            topic: 'Payments & pricing',
            question: 'Can I get a refund?',
            answer: 'Ridly does not process or hold any payments — all money moves directly between you and the driver. If a trip is cancelled or does not take place, contact the driver directly and ask them to return your payment via MobilePay. If you cannot resolve it, contact us at support@ridly.dk and we will help mediate.',
        },
        {
            id: 'dr-01',
            topic: 'Driver onboarding',
            question: 'How do I become a driver on Ridly?',
            answer: 'Switch to Driver mode on the home screen, then tap "Become a driver". Enter your car details (make, model, year, colour, and registration plate) and your MobilePay phone number. Your profile will be reviewed and activated within 24 hours.',
        },
        {
            id: 'dr-02',
            topic: 'Driver onboarding',
            question: 'How do I post a trip?',
            answer: 'In Driver mode, tap "Post a trip" and choose your trip type. ' +
                '"Daily Commute" (Model A) is for recurring routes: pick your origin and destination, select which days of the week you drive, set your departure time using the time picker, choose your rolling window (how many days ahead your trips are visible — 7 to 90 days), and set your seat count. ' +
                '"One-off Long Distance" (Model B) is for a single trip on a specific date: enter the route, pick the date and departure time, set total seats, and the minimum number of passengers needed for the trip to run. ' +
                'After choosing your settings, review the calculated price and confirm to publish.',
        },
        {
            id: 'dr-03',
            topic: 'Driver onboarding',
            question: 'How do I set up MobilePay for receiving payments?',
            answer: 'During driver onboarding you enter the phone number linked to your personal MobilePay account. Passengers will use this number to pay you after each trip. Make sure the number is correct — you can update it in Settings → Edit profile.',
        },
        {
            id: 'dr-04',
            topic: 'Driver onboarding',
            question: 'Can I edit or cancel a trip I have posted?',
            answer: 'Yes — you can edit a trip\'s route or departure time from "My Trips" (driver view) by tapping the trip and selecting "Edit trip". Changes only affect future instances; past trips and confirmed bookings are not altered. ' +
                'To cancel, open the trip and tap "Cancel trip". If passengers are already confirmed, they will be notified immediately. Cancelling close to departure may affect your reliability rating. ' +
                'For recurring (Model A) trips you can cancel individual days rather than the entire route.',
        },
        {
            id: 'dr-05',
            topic: 'Driver onboarding',
            question: 'Do I need a special licence to drive on Ridly?',
            answer: 'No special licence is needed — a standard Danish driving licence is sufficient. Ridly is a carpooling platform, not a taxi or ride-hailing service. Drivers share costs with passengers for trips they were already planning to take.',
        },
        {
            id: 'dr-06',
            topic: 'Driver onboarding',
            question: 'What is the rolling window for recurring trips?',
            answer: 'When you post a Daily Commute (Model A) trip, the rolling window controls how many days ahead your trip instances are visible to passengers — you can choose 7, 14, 30, 60, or 90 days. ' +
                'For example, with a 30-day window, passengers can always see and book your trips up to 30 days in the future. As old instances pass, new ones are automatically created to maintain the window. ' +
                'A shorter window (e.g. 7 days) means less commitment and fewer instances shown at once; a longer window (e.g. 90 days) gives passengers more lead time to plan. You set this once when posting — it stays fixed for that route.',
        },
        {
            id: 'dr-07',
            topic: 'Driver onboarding',
            question: 'How do I stop a recurring commute route?',
            answer: 'Open My Trips and swipe left on any instance of the recurring route. Tap "Stop route" (the orange button). ' +
                'This cancels all upcoming trips on that route and stops the rolling window from creating new ones — no new instances will ever appear. ' +
                'Passengers with upcoming bookings will be notified. Since payment is always handled directly between you and your passengers, please arrange any refunds with them individually via MobilePay. ' +
                'If you only want to cancel a single day rather than the whole route, swipe left and tap "Cancel" instead.',
        },
        {
            id: 'acc-01',
            topic: 'Account & profile',
            question: 'How do I verify my email address?',
            answer: 'After signing up, a verification email is sent to your address. Open the email and tap the confirmation link. Once confirmed you will be taken back to the app and logged in automatically. Check your spam folder if the email does not arrive within a few minutes.',
        },
        {
            id: 'acc-02',
            topic: 'Account & profile',
            question: 'How do I change my password?',
            answer: 'Go to Settings → Change password. Enter your current password and then your new one. You can also use "Forgot password" on the login screen to reset it via email.',
        },
        {
            id: 'acc-03',
            topic: 'Account & profile',
            question: 'How do I update my profile photo?',
            answer: 'Go to your profile by tapping your avatar in the top corner. Tap the camera icon on your profile picture and choose a photo from your library or take a new one. A clear, recent photo helps drivers and passengers trust each other.',
        },
        {
            id: 'acc-04',
            topic: 'Account & profile',
            question: 'How do I delete my account?',
            answer: 'Account deletion is handled by our support team to ensure all your data is removed correctly in compliance with GDPR. Send a deletion request to support@ridly.dk from your registered email address. Your account will be removed within 30 days.',
        },
        {
            id: 'sf-01',
            topic: 'Safety & trust',
            question: 'How does the rating system work?',
            answer: 'After every completed trip, passengers rate drivers and drivers rate passengers (1–5 stars). Ratings are averaged and displayed on profiles. Consistently low ratings may lead to account review. Always rate honestly — it helps keep the community safe.',
        },
        {
            id: 'sf-02',
            topic: 'Safety & trust',
            question: 'How do I report another user?',
            answer: 'Open the user\'s profile, scroll down, and tap "Report user". Select the reason and add any relevant details. Our team reviews all reports within 24 hours. For urgent safety concerns, contact us directly at support@ridly.dk.',
        },
        {
            id: 'sf-03',
            topic: 'Safety & trust',
            question: 'What safety measures does Ridly have in place?',
            answer: 'All users must verify their email. Drivers are reviewed before activation. Profiles show ratings and trip history. In-app chat lets you communicate without sharing personal numbers. If anything feels unsafe during a trip, trust your instincts and contact emergency services (112) first.',
        },
        {
            id: 'sf-04',
            topic: 'Safety & trust',
            question: 'Is my personal information shared with other users?',
            answer: 'Only your first name, profile picture, and star rating are visible to matched passengers or drivers. Your full name, email, phone number, and payment details are never shared. See our Privacy Policy for full details.',
        },
        {
            id: 'tech-01',
            topic: 'App & technical issues',
            question: 'I am not receiving push notifications. What should I do?',
            answer: 'Go to your phone\'s Settings → Notifications → Ridly and make sure notifications are enabled. Inside the app, go to Settings and check that "Push notifications" is toggled on. If the problem persists, log out and back in to re-register the notification token.',
        },
        {
            id: 'tech-02',
            topic: 'App & technical issues',
            question: 'The app is showing an error or crashing. What should I do?',
            answer: 'Try closing and reopening the app. If the issue continues, check that you have the latest version installed from the App Store or Google Play. If the problem persists, report it to support@ridly.dk with a description of what you were doing and what error you saw.',
        },
        {
            id: 'tech-03',
            topic: 'App & technical issues',
            question: 'Why can I not find any trips in my area?',
            answer: 'Ridly is growing, so availability varies by region. Try widening your search by using nearby cities or being flexible with your departure date. You can also set up a Search Alert — we will notify you when a matching trip is posted.',
        },
        {
            id: 'tech-04',
            topic: 'App & technical issues',
            question: 'The map or location is not working correctly.',
            answer: 'Make sure Ridly has permission to access your location in your phone\'s settings. If the wrong address appears, you can tap the search bar and type the address manually instead of using the detected location.',
        },
    ];
    getFaqs() {
        return { faqs: this.faqs };
    }
};
exports.HelpCenterService = HelpCenterService;
exports.HelpCenterService = HelpCenterService = __decorate([
    (0, common_1.Injectable)()
], HelpCenterService);
//# sourceMappingURL=help-center.service.js.map