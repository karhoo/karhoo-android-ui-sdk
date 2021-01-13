# Traveller App
This is the Traveller app.

## Introduction

The Traveller App is the base application using both the SDK and UISDK to function. With this, you can play around with the Karhoo App, book fake rides, try out some of your own changes or generally test the apps use and functionality given to it by the SDKs.

Keep in mind that this is a "Starter kit" for using the Karhoo SDKs.

## Getting Started with the sample app

This project uses the Gradle build system.

First download the project by cloning this repository or downloading an archived
snapshot. (See the options at the top of the page.)

In Android Studio, use the "Import non-Android Studio project" or "Import Project" option. 
If prompted for a gradle configuration accept the default settings.

Alternatively use the `gradlew build` command to build the project directly.

The demo app require that you add your own set of API keys:

- Create a file in the app directory called `secure.properties` (this file should *NOT* be under version control to protect your API key)
- Add the API keys and configurations to secure.properties. You can also take a look at the `secure.properties.template` as an example.
    - [Get a Maps API key](https://developers.google.com/maps/documentation/android-sdk/get-api-key)
    - Enable Firebase analytics/crashlytics and add config file to the project (google-service.json) (Optional)
    - Add GUEST CHECKOUT configuration for your account in order to enable the guest checkout journey
    - Add Staging environment configuration in order to be able to use Staging environment
- Update fabric API key in app/build.gradle 
- Build and run
## Features

With the Traveller App, you can view plenty of features that the SDKs allow you to use. This means you can test all our features though the Traveller App like making a booking, address screen with its get current location and select pin on map features, viewing upcoming and past rides, prebooking, editing and saving profile changes, rating trips, tracking trips and much more. All features are present. 

<img src="https://files.readme.io/10bfedc-android_address.jpg" width="200"/>
<img src="https://files.readme.io/b28bcb0-android_quotes.jpg" width="200"/>
<img src="https://files.readme.io/8b18d5f-android_flight.jpg" width="200"/>
<img src="https://files.readme.io/e785bdf-android_rides.jpg" width="200"/>
