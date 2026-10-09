# Hop — Motion Video Story

## Creative brief

**Working title:** Every seat moves us forward  
**Format:** 3-minute product story for Android and iOS  
**Audience:** Passengers and drivers who have not used Hop before  
**Story:** Follow one passenger searching for a ride and one driver offering a seat. Their journeys meet on the road, then continue through the practical details that make a shared trip work.

The film should feel human and optimistic, not like a feature checklist. Use a single visual thread: a route line that begins as a search path, becomes the road between two people, and finally resolves into the Hop mark. Show real app UI in device frames; keep labels and amounts legible. Use Danish locations and DKK formatting where sample content is needed.

## Script and storyboard

| Time | Picture, movement, and UI | Voice-over / on-screen copy |
|---|---|---|
| **00:00–00:09** | Dawn over a Danish city. Two separate phone screens wake: one passenger is planning a journey; a driver is looking at the road ahead. A thin lime route line appears between their map pins and draws the title. | **VO:** “Somewhere between where we are and where we’re going, there’s a seat to share.” **SUPER:** “Hop — Carpooling by Ridly” |
| **00:09–00:21** | The Hop splash resolves into the illustrated onboarding slides. A leaf and shared car animate into view. Move through sign-up, login, email confirmation, and password recovery as quick, clean UI glimpses. The route line becomes a progress indicator, then opens onto Home. | **VO:** “Hop brings passengers and drivers together for the trips already happening.” **SUPER:** “One account. Two ways to ride.” |
| **00:21–00:36** | Passenger Home: the role toggle changes between passenger and driver. The passenger sees the search entry, saved-place shortcuts, recent searches, and an active-booking card if one exists. Briefly reveal trip suggestions and trust information as supporting cards, not as the main action. | **VO:** “Start with a destination, a familiar place, or a search you’ve made before.” |
| **00:36–00:53** | The passenger selects origin and destination using place autocomplete, chooses a date and seat count, then watches matching commute and long-trip cards arrive. Filter by trip type or date. Cards glide into a map-and-route motif. | **VO:** “Choose where and when. Browse available rides, dates, seats, and prices in one place.” **UI:** “From · To · Date · Seats” |
| **00:53–01:08** | Open a ride. The route, departure, price per seat, driver profile, vehicle, and reviews build into a clear trip summary. The passenger confirms a seat; the total updates. For a threshold trip, show the minimum-seat progress and the notice that the trip depends on reaching it. Then land on booking success. | **VO:** “Get to know the trip and the person driving before you book.” **SUPER:** “Review the details. Choose your seat.” |
| **01:08–01:18** | A second search has no matches. Instead of a dead end, the empty state offers “Alert me when one appears.” The passenger taps; the alert settles into a small confirmation toast. | **VO:** “And if the right ride isn’t there yet, ask Hop to let you know when one appears.” |
| **01:18–01:32** | Cut to the driver setting up: car make, model, year, colour, and available passenger seats. Add a MobilePay number. Show the driver’s home with trips and earnings information, then a past route ready to repost. | **VO:** “For drivers, getting started means adding your car and the number passengers can use to pay you directly.” |
| **01:32–01:53** | The driver chooses a trip style. **Daily Commute:** select a route, weekdays, departure time, seats, and a rolling window; show future trips extending along the calendar. **One-off Long Distance:** set route, date, time, seats, and minimum confirmed passengers. Route distance resolves into the price review before posting. | **VO:** “Offer a regular commute that keeps rolling, or post a one-off journey with a minimum number of passengers.” **SUPER:** “Two ways to share a trip” |
| **01:53–02:08** | Driver My Trips: switch between upcoming and past, filter by trip type or date, open trip details, and review booked seats and passengers. Briefly show editing a trip, reposting a previous one, and the confirmation for stopping a recurring route. Finish with the driver marking a completed trip. | **VO:** “Keep trips up to date, see who’s coming, and close the loop when the journey is done.” |
| **02:08–02:24** | Passenger and driver views alternate: My Trips separates upcoming and past journeys; a passenger opens an upcoming booking’s cancellation confirmation, then an active trip opens into booking-scoped chat. A message arrives, followed by an in-app notification. Show profile and review cards, then the report-user action as a discreet safety option. | **VO:** “Plans can change. Stay in touch about the booking, keep track of updates, and build trust through profiles and ratings.” |
| **02:24–02:41** | After arrival, show the passenger settlement screen with the driver’s MobilePay details. The passenger completes payment in MobilePay and marks it paid in Hop; the driver confirms receipt. For accuracy, show the dispute option as an alternative state—not as an automatic payment failure animation. | **VO:** “After the ride, passengers pay the driver directly through MobilePay. Both sides can keep the settlement status clear.” **SUPER:** “Direct MobilePay payment · Confirmed in Hop” |
| **02:41–02:52** | Both riders leave a rating. The driver opens the monthly tax dashboard: gross earnings, deduction, taxable amount, estimated tax; swipe to the annual tax report download. Keep the estimate disclaimer visible. | **VO:** “Then leave a rating. Drivers can also review monthly earnings and download an annual tax report.” **SUPER:** “Tax figures are estimates. Verify with SKAT.” |
| **02:52–03:00** | A quick, calm close: profile, settings, Help Centre FAQ search, privacy policy, and terms of service appear as tidy cards. Return to the shared route line, now connecting the two riders’ phones. End on the Hop logo and both platform marks. | **VO:** “Different journeys. One seat closer.” **SUPER:** “Find your ride. Share your route.” |

## Feature coverage checklist

This storyboard covers the implemented mobile-product journeys identified in the repository:

- **Getting started and account:** splash/onboarding, sign-up, login, email verification, forgot/reset password.
- **Passenger discovery:** role toggle, passenger home, place search/autocomplete, saved places, recent searches, search filters, trip cards, and search alerts when no ride is found.
- **Passenger booking:** trip and driver details, vehicle/reviews, seat and price confirmation, booking success, threshold-trip progress, upcoming/past trips, active trip, and cancellation.
- **Driver journey:** car setup, MobilePay number, daily recurring commute and rolling trip window, one-off threshold trip, route-distance price review, trip posting, trip editing/reposting/stopping, passenger/trip details, completion, and trip history.
- **Shared journey and trust:** booking chat and chat list, in-app/push notifications, own and other-user profiles, mutual ratings, and report-user action.
- **After the ride:** passenger payment status, driver receipt confirmation, and dispute flow; monthly tax dashboard and annual report download.
- **Account and support:** settings, logout, Help Centre/FAQ search, privacy policy, and terms of service.
- **Supporting home content:** recent and saved routes, route suggestions, driver earnings/trip summaries, and available trust/impact information. Show values only when populated by the product.

## Accuracy and production notes

- MobilePay is a **direct passenger-to-driver payment after the ride**. Hop records the passenger-paid and driver-confirmed states; do not depict an in-app card checkout, automatic transfer, or provider-verified payment.
- The previous pre-ride MobilePay handoff destination remains as a placeholder. The film should use the current post-trip settlement screens instead.
- Recurring commute trips and one-off threshold trips are distinct posting models. Do not imply that every trip has a passenger threshold or auto-renews.
- Search alerts are offered from the no-results state. Referral artwork exists, but its passenger-home placement is commented out; do not present referral sharing as a live flow.
- Do not imply live GPS ride tracking. The product journey shown here is trip discovery, booking, messaging, completion, settlement, and rating.
- The backend also contains operator/admin endpoints, health checks, email hooks, and background jobs. These are service capabilities rather than ordinary passenger/driver mobile screens, so they are not depicted as customer-facing app features.
