<div align="center">
<a href="https://karhoo.com">
  <img
    alt="Karhoo logo"
    width="250px"
    src="https://cdn.karhoo.com/s/images/logos/karhoo_logo.png"
  />
</a>

<h1>Karhoo Android UI SDK</h1>

The UI SDK extends on our [**Network SDK**](https://github.com/karhoo/karhoo-android-sdk) with ready to use screens and views for your end users to book rides with [**Karhoo**](https://karhoo.com/) in your application.

<br />

[**Read The Docs**](https://developer.karhoo.com/docs/build-apps-using-sdks)

</div>

<hr />

# Introduction

The KarhooUISDK extends upon the Karhoo network SDK to give you the UI side of a ride hailing/booking experience. This is useful for Whitelabel applications or faster integration into existing applications. The UISDK provides entry points for View Controllers (screens) and components (views), that interact with the KarhooAPI to give out of the box experiences for users.
You create the many different views found in the Traveller App. This could be the bookings screen, rides screen, profile screen, etc. Any of them all come from here, the UISDK. 

## Integration
TBC

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

_Looking to contribute?_

### 🐛 Bugs

Please file an issue for bugs, missing documentation, or unexpected behavior.

### 💡 Feature Requests

Please file an issue to suggest new features. Vote on feature requests by adding
a 👍. This helps maintainers prioritize what to work on.

### ❓ Questions

For questions related to using the library, please re-visit a documentation first. If there are no answer, please create an issue with a label `help needed`.

## Useful Links

[Karhoo Developer Site](https://developer.karhoo.com/)

[The Android Network SDK](https://github.com/karhoo/karhoo-android-sdk)

