<div align="center">
<a href="https://karhoo.com">
<img
alt="Karhoo logo"
width="250px"
src="https://cdn.karhoo.com/s/images/logos/karhoo_logo.png"
/>
</a>
</div>

[![](https://jitpack.io/v/karhoo/karhoo-android-ui-sdk.svg)](https://jitpack.io/#karhoo/karhoo-android-ui-sdk)

#Karhoo Android UI SDK

The UI SDK extends our [**Network SDK**](https://github.com/karhoo/karhoo-android-sdk) with ready to use screens and views for your end users to book rides with [**Karhoo**](https://karhoo.com/) in your application.

For more general information about the SDKs, checkout [**the karhoo developer portal**](https://developer.karhoo.com/docs/build-apps-using-sdks)

## Installation

Depending on the payment provider that you wish to integrate with, we offer three dependencies, each one containing a different payment provider integration. Only one of them should be used when adding the UISDK to your project

Stable Release
```gradle
dependencies {
    //... Other project dependencies

    //The -adyen dependency contains the Adyen integration:
    implementation 'com.github.karhoo.karhoo-android-ui-sdk:uisdk-adyen:1.7.4'

    //The -braintree dependency contains the Braintree integration:
    implementation 'com.github.karhoo.karhoo-android-ui-sdk:uisdk-braintree:1.7.4'

    //The -full dependency contains both payment providers and it's up to you which payment provider you shall use:
    implementation 'com.github.karhoo.karhoo-android-ui-sdk:uisdk-full:1.7.4'

    //Note that only one dependency from the above three should be integrated into your project
}

```

Canary Release
```gradle
implementation 'com.github.karhoo:karhoo-android-ui-sdk:develop-SNAPSHOT'
```
## Initialisation

There are a few things the UI SDK needs to know before you can get started such as what environment to connect to, or what kind of authentication method to use.
To configure the SDK you will need to provide an implementation of our KarhooUISDKConfiguration interface. This lets our SDK grab certain dependencies and configuration settings.


```kotlin
class KarhooConfig(val context: Context): KarhooUISDKConfiguration {
  override lateinit var paymentManager: PaymentManager
  var sdkAuthenticationRequired: ((callback: () -> Unit) -> Unit)? = null

  override fun logo(): Drawable? {
    return context.getDrawable(R.drawable.your-logo)
  }

  override fun environment(): KarhooEnvironment {
    return KarhooEnvironment.Sandbox()
  }

  override fun context(): Context {
    return context
  }

  override fun authenticationMethod(): AuthenticationMethod {
    return AuthenticationMethod.KarhooUser()
  }

  override suspend fun requireSDKAuthentication(callback: () -> Unit) {
    sdkAuthenticationRequired?.invoke(callback)
  }
}

// Then set the payment provider and register the configuration in your Application file

// The payment provider's view needs to be instantiated. It can be AdyenPaymentView or BraintreePaymentView depending on the PSP choice
// Adyen integration
val paymentManager = AdyenPaymentManager()
paymentManager.paymentProviderView = AdyenPaymentView()

// Braintree integration
val paymentManager = BraintreePaymentManager()
paymentManager.paymentProviderView = BraintreePaymentView()

// Later down the line
val config = SDKConfig(context = this.applicationContext)
config.sdkAuthenticationRequired = {
  loginInBackground(it, yourToken)
}

KarhooApi.setConfiguration(configuration = config)

// Implementing the token refresh flow
private var deferredRequests: MutableList<(()-> Unit)> = arrayListOf()
private fun loginInBackground(callback: () -> Unit, token: String) {
  if (!requestedAuthentication) {
    Log.e(TAG, "Need an external authentication")
    requestedAuthentication = true
    deferredRequests.add(callback)

    GlobalScope.launch {
      //Refresh your own access token in order to ensure a proper validity period for the Karhoo token
      //Then use that token to refresh the credentials inside the SDK
      KarhooApi.authService.login(token).execute { result ->
        when (result) {
          is Resource.Success -> {
            Log.e(TAG, "We got a new token from the back-end")
            deferredRequests.map {
              it.invoke()
            }
            deferredRequests.clear()
            requestedAuthentication = false
          }
          is Resource.Failure -> toastErrorMessage(result.error)
        }
      }
    }
  } else {
    deferredRequests.add(callback)
  }
}
```

With this configuration the UISDK can be initialised in your Activities/Fragments. This will also ensure the network layer (KarhooSDK) is initialised and configured properly.

For full documentation of SDK services please visit our Developer Portal: https://developer.karhoo.com/reference#user-service

## Authentication

The KarhooSDK requires authentication, attempting to interact with the SDKs without authenticating will result in errors. There are three possible authentication methods supported:
- Username/password
- Token authentication
- Guest authentication

The AuthenticationMethod is set as part of the configuration. The sample code above is the config for username/password.
In order to authenticate via a token, you will first need to [**integrate your external authentication system with the Karhoo platform**](https://developer.karhoo.com/docs/how-to-integrate-third-party-auth-system#3-issuing-tokens) before initialising the SDK with your client id

```kotlin
override fun authenticationMethod(): AuthenticationMethod {
  return AuthenticationMethod.TokenExchange(clientId = "your-app-id", scope = "openid profile email phone https://karhoo.com/traveller"
}
```

For authenticating with a guest user, the following authentication method should be set
```kotlin
  override fun authenticationMethod(): AuthenticationMethod {
        return AuthenticationMethod.Guest(identifier = "client_identifier", referer = "referer", organisationId = "organisation_id")
    }
```

The UISDK also provides the requireSDKAuthentication method in the KarhooUISDKConfiguration interface to notify whenever an external authentication is required. This happens only when all attempts to refresh the access token needed for authenticating requests have failed.
This method receives a callback parameter which should be invoked when the external authentication has finalized. An example of how the requireSDKAuthentication flow should be handled is found above.

## Screens

Once the SDK is authenticated you can use it to show pre-built customisable Activities and Views. Consider this framework like a UI API. You feed certain parameters into the view controllers and views, and your end users interactions with the views triggers actionable output.

#### Booking
The booking screen is the main entry point to the UISDK.
From the booking screen the user can select a pickup point, a destination and a booking time ("ASAP" if left blank). They will then receive a list of quotes and be able to select one to book.
When instantiating the Booking screen make sure the user has granted location permissions.

<div align="center">
<a href="https://karhoo.com">
<img
alt="Booking screen"
width="400px"
src="https://files.readme.io/491a2aa-Screenshot_2022-11-02_at_10.19.00.png"
/>
</a>
</div>

The booking screen can be launched in a classic way, via an Intent.
Along the builder data we can find a few parameters that can be passed down:
- "tripDetails" of type TripInfo which may contain the origin and destination in order for the addressView to be filled in
- "outboundTripId" can be used when "rebooking" a trip
- "initialLocation" should be passed if the user shouldn't wait for the GPS sensor to retrieve a location
- "passengerDetails" can be passed to pre-fill the passenger details in the checkout screen
  The below example launches a Booking Activity in a default state:

```kotlin
// launching for primary flow
val intent = BookingActivity.Builder.builder
    .initialLocation(location)
    .build(this)
startActivity(intent)

// launching for callback example
val MY_REQ_CODE = 101
val callbackIntent = BookingActivity.Builder.builder
    .initialLocation(location)
    .buildForOnActivityResultCallback(this)
startActivityForResult(callbackIntent, MY_REQ_CODE)

// receiving booked trip for callback flow
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  if (resultCode == Activity.RESULT_OK && requestCode == MY_REQ_CODE) {
    val bookedTrip = data?.getParcelableExtra<TripInfo>(BookingCodes.BOOKED_TRIP)
    // do something with bookedTrip
  }
  super.onActivityResult(requestCode, resultCode, data)
}
```
### Quotes List
After selecting two addresses in the previous screen, the quotes list screen may be used to show up the results.
<div align="center">
<a href="https://karhoo.com">
<img
alt="Quote list screen"
width="400px"
src="https://files.readme.io/394bfd5-Screenshot_2022-11-02_at_11.07.08.png"
/>
</a>
</div>

```kotlin
val builder = QuotesActivity.Builder()
.restorePreviousData(restorePreviousData)
.validityTimestamp(validityTimestamp)
.bookingInfo(journeyDetails)   
startActivityForResult(builder.build(this@BookingActivity), QUOTES_INFO_REQUEST_NUMBER)
```

When the user selects a quote, they are then taken to the checkout details screen which enables them to add their booking details i.e. first name, last name, email address, phone number and payment details. The Book Ride button is disabled until all mandatory fields have been completed.

<div align="center">
<a href="https://karhoo.com">
<img
alt="Checkout screen"
width="400px"
src="https://files.readme.io/848b44d-Screenshot_2022-10-31_at_20.54.08.png"
/>
</a>
</div>

Here the user can check the T&Cs if they are required and must complete the passenger contact details in order to get to the next step.
After everything is filled up correctly, the button will state "CONFIRM AND PAY" and then the Adyen/Braintree payment flow will start.
If the payment is successful, the booking is created.

Here is how it looks filled up

<div align="center">
<a href="https://karhoo.com">
<img
alt="Checkout screen filled"
width="400px"
src="https://files.readme.io/abbc1b2-Screenshot_2022-11-02_at_09.20.49.png"
/>
</a>
</div>

We also offer the possibility to enable the use of loyalty to earn and burn points on trips. Some extra development on your side is needed to enable this capability.

<div align="center">
<a href="https://karhoo.com">
<img
alt="Checkout screen with loyalty"
width="400px"
src="https://files.readme.io/d6379ba-Screenshot_2022-11-02_at_11.23.51.png"
/>
</a>
</div>

```kotlin
// launching for primary flow
val builder = CheckoutActivity.Builder()
                            .quote(actions.quote)
                            .bookingMetadata(bookingMetadata)
                            .passengerDetails(passengerDetails)
                            .comments(bookingComments)
                            .bookingInfo(BookingInfo(pickup, destination, date))
                            .currentValidityDeadlineTimestamp(ts)

// launching for callback example
startActivityForResult(builder.build(this), REQ_CODE_BOOKING_REQUEST_ACTIVITY)
```
#### Flight Number

The flight number screen is launched if a user has after entered an address that is an Airport POI (Pick up or drop-off). It allows the user to enter a valid Flight Number, once confirmed the user is taken back to the Trip screen, the flight number is added to the trip object and can be passed on to the fleet for pick up accuracy. This flow is only applicable for authenticated users as guest users will be able to add their flight details on the details screen.
<div align="center">
<a href="https://karhoo.com">
<img
alt="Booking screen"
width="400px"
src="https://files.readme.io/8b18d5f-android_flight.jpg"
/>
</a>
</div>

To show the Flight Screen, an intent for the AirportActivity should be launched
```kotlin
val intent = AirportActivity.Builder.builder.build(this)
startActivityForResult(intent, FlightCodes.REQ_CODE)

// result example
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode == RESULT_OK && requestCode == FlightCodes.REQ_CODE) {
        val flightDetails = data?.getParcelableExtra<FlightDetails>(FlightCodes.FLIGHT_DETAILS)
    }
    super.onActivityResult(requestCode, resultCode, data)
}
```

#### Trip

The trip screen allows the user to track a trip from the point of allocation through to completed. Shows the pickup point, destination and location of the driver’s car throughout the trip.
The user has options to cancel the trip or call the driver or fleet at any point before passenger on board.
When the trip is complete the user is presented with a summary and is given the option to rebook the return trip via the booking screen.
When instantiating the Android trip screen make sure the user has granted location permissions.

<div align="center">
<a href="https://karhoo.com">
<img
alt="Booking screen"
width="400px"
src="https://files.readme.io/f5a469f-android_trip.jpg"
/>
</a>
</div>

The trip screen can be shown by creating an intent for the TripActivity. The tripInfo parameter of type TripInfo which contains the origin and destination is needed

```kotlin
// launching example
val intent = TripActivity.Builder.builder
        .tripInfo(trip)
        .build(context)

startActivity(intent)
```

#### Rides
The rides screen provides a list of a user’s upcoming and past rides. A user can select a ride and be taken to the ride details screen

This screen can be launched from the top right hand corner “Rides” button. Once launched it shows the list of trips that the user has booked as well as the trips which the user has previously taken.
The screen contains two different pages contained in a viewpager, one for the upcoming rides and one for the past ones.
Retrieving the rides can be done only by being logged-in with an user, the feature being unavailable for a guest user

<div align="center">
<a href="https://karhoo.com">
<img
alt="Booking screen"
width="400px"
src="https://files.readme.io/e785bdf-android_rides.jpg"
/>
</a>
</div>

In order to launch the Rides screen, an intent for the RidesActivity should be made
```kotlin
// launching example
val intent = RidesActivity.Builder.builder
        .build(context)

startActivity(intent)
```

#### Ride Details
The ride details screen provides a breakdown of the trip details for an upcoming or past ride. The user can track the ride by opening the Trip screen, cancel an upcoming ride, contact the driver of an upcoming ride.

<div align="center">
<a href="https://karhoo.com">
<img
alt="Booking screen"
width="400px"
src="https://files.readme.io/8157c69-android_details.jpg"
/>
</a>
</div>

In order to launch the Rides screen, an intent for the RideDetailsActivity has to be made. Providing the tripInfo of type TripInfo is needed to fill in the details of the ride

```kotlin
// launching example
val intent = RideDetailsActivity.Builder.builder
 .trip(tripInfo)
        .build(context)
startActivity(intent)
```

## Customisation
You can override aspects of the screens and components to align the UI SDK more towards your own brand. This includes the colour scheme, font, translations and custom routing within screens.

### Translations / text copies
You can set any label text in any language by overriding the key / value pair in your apps localisation files. A full list of strings is available in ```res/strings``` directory.

### Assets
When populating the views, the UISDK checks the main application bundle first for an image with the desired name. If it can't find an asset in the main bundle it will default to its own bundle. So to override any asset or image in the UISDK you can place the image with the right name in your asset bundle. A full list of assets can be found in ```/res/drawable/``` folder of the library

```
<style name="KhPickUpDot">
    <item name="srcCompat">@drawable/custom_ic_pickup</item>
</style>
```

### Color
The UI in this SDK conforms to a colour scheme, which is overridable by creating your own 'KarhooColors' implementation and injecting it to the SDK.

```xml
<color name="primary">@color/pink</color>
<color name="secondary">@color/yellow</color>
```
Add a custom theme in the styles.xml file of your app

```xml
<style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
    <!-- Customize your theme here. -->
    <item name="colorPrimary">@color/primary</item>
    <item name="colorPrimaryDark">@color/secondary</item>
    <item name="colorAccent">@color/primary</item>
    <item name="fontFamily">sans-serif-medium</item>
</style>
```

### Custom view styles
The internal views of the UISDK can be customized by overriding their default styles that can be found in the library's ```/res/styles``` file
For example, the following widgets/views can be customised:
- pick up and drop off icons
- map location icon, pick up pin, drop off pins and line colour
- address picker background and flipper image
- sort and vehicle category selection tabs

### Injectable routing

Each screen in the UISDK automatically routes to the next. You may want to only use particular screens in the UISDK and custom screens for others. For example you may want to book a trip with the UISDK but use your own address search screen. You can inject a routing implementation to override the navigation flow in the UISDK. To do this you would create your own screen builder and inject it into the SDK.

## Set up for developing the SDK
This project uses the Gradle build system.

First download the project by cloning this repository or downloading an archived
snapshot. (See the options at the top of the page.)

In Android Studio, use the "Import non-Android Studio project" or "Import Project" option.
If prompted for a gradle configuration accept the default settings.

Alternatively use the `gradlew build` command to build the project directly.

## Getting Started with the sample app
The demo app require that you add your own set of API keys:

- Create a file in the app directory called `secure.properties` (this file should *NOT* be under version control to protect your API key)
- Add the API keys and configurations to secure.properties. You can also take a look at the `secure.properties.template` as an example.
  - [Get a Maps API key](https://developers.google.com/maps/documentation/android-sdk/get-api-key)
  - Enable Firebase analytics/crashlytics and add config file to the project (google-service.json) (Optional)
  - Add GUEST CHECKOUT configuration for your account in order to enable the guest checkout journey
  - Add Staging environment configuration in order to be able to use Staging environment
- Update fabric API key in app/build.gradle
- Build and run

## Issues

### 🐛 Bugs

Please file an issue for bugs, missing documentation, or unexpected behavior.

### 💡 Feature Requests

Please file an issue to suggest new features. Vote on feature requests by adding
a 👍. This helps maintainers prioritize what to work on.

### ❓ Questions

For questions related to using the library, please re-visit a documentation first. If there are no answer, please create an issue with a label `help needed`.

## License
[BSD-2-Clause](./LICENSE)

