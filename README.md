# Command Line for Light Phone (CLLP)
An Android app for communicating between two devices via SMS when those devices both have an active SIM (including duplicate SIMs).

![App logo](https://niraj.blog/content/images/size/w2000/2019/12/CLLP_icon_512-half.png)

Command Line for Light Phone (CLLP) is a tool for Android users that use two active SIMs for their Light Phone 2 and other Android device – either using phone number sharing (when both devices can receive calls and texts for the same phone number via something like T-Mobile DIGITS or Verizon NumberShare) or where each device has its own separate phone number associated with it. CLLP leverages the fact that you can send text messages from one device to the other. When you go out with your Light Phone 2, the CLLP app runs on the Android device you leave behind and monitors incoming SMS messages to look for commands you send that instruct it to perform tasks and report the results back to your Light Phone 2.

Unfortunately CLLP is not available on Google Play due to their [heavy-handed crackdown on apps that use SMS](https://www.xda-developers.com/google-restriction-sms-call-log-permissions/) so it must be side-loaded on an Android device running Android Nougat or higher. You can download the latest apk to install it from [the releases page](https://github.com/nirajsanghvi/CLLP/releases). After initial install, the app will automatically notify you as new releases become available.

![App demo](https://i.imgur.com/3XegkKH.gif)

Current features supported by CLLP:
* Google Maps directions
  * Walking <pre>[origin] *walkto* [destination]</pre>
  * Transit <pre>[origin] *transitto* [destination]</pre>
  * Driving <pre>[origin] *driveto* [destination]</pre>
  * Bicycling <pre>[origin] *biketo* [destination]</pre>
* Google Calendar
  * Agenda for next 3 days <pre>*calagenda*</pre>
  * Add event to calendar <pre>*addtocal* [event title] on [date and time] at [location]</pre>
* List all available commands <pre>*helpme*</pre>

![App help demo](https://i.imgur.com/Cq86Bid.gif)

Wishlist:
* Yelp (search by business name or type of food, etc.)
* Use the Android device location to help give a hint when using commands looking for places so you don't have to type as much on the Light Phone
* Weather
* Wikipedia

The CLLP app tries to explain what permissions are needed for different things. For instance, it absolutely needs notification access (to catch incoming SMS commands) and SMS sending permission (to send back results). Currently you can optionally give calendar access permission for the agenda and event adding features, and a Google Maps API key you generate to get directions (this is because [they changed the pricing model](https://www.reddit.com/r/GoogleMaps/comments/8gl0zl/google_maps_api_pricing_change/) last year so it's prohibitively expensive for me to use my own API key for all users). The intention is to have all permissions be optional so you can only grant the ones you need for the features you want.

If you like the app and want to support its ongoing development, you can [buy me a beer](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=CBGPF2LBBH3QW&currency_code=USD&source=url) which is much appreciated! 🍻Issues and PRs are also welcome, though I'll note that I intend to adhere strictly to the Light Phone ethos of only providing utility and reducing distractions.

*__Note:__ If you want to fork this repo, make sure to update the UpdateChecker which is hardcoded to look for releases from this repo, and provide your own (or disable) Firebase Analytics, which I'm relying on for crash reports since this app is not in the Play Store.*
