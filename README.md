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
    implementation 'com.github.karhoo.karhoo-android-ui-sdk:uisdk-adyen:1.8.0'

    //Note that only one dependency from the above three should be integrated into your project
}

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

