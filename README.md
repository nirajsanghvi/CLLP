# Command Line for Light Phone (CLLP)
An Android app for communicating between two devices via SMS when those devices share a single phone number.

![App logo](https://niraj.blog/content/images/size/w2000/2019/12/CLLP_icon_512-half.png)

Command Line for Light Phone (CLLP) is a tool for anyone that uses phone number sharing (when both devices can receive texts for the same phone number) between their Light Phone 2 and another Android device. It leverages the fact that you can send text messages to yourself and they get delivered to both your devices. The idea is when you go out with your Light Phone 2, CLLP is running on the Android device you leave behind and monitors incoming SMS messages to look for commands from you that instruct it to perform tasks (get directions, check your calendar, etc.) and report the results back to your Light Phone 2.

Unfortunately CLLP is not available on Google Play due to their crazy crackdown on apps that use SMS so it must be side-loaded on an Android device running Android Nougat or higher. You can download the apk to install it here.

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
