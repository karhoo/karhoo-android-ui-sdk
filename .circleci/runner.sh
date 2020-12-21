#!/bin/bash
java -jar runner.jar espresso \
   --apikey "$SAUCE_ACCESS_KEY" \
   --datacenter https://ondemand.eu-central-1.saucelabs.com \
   --app ../app/build/outputs/apk/debug/app-debug.apk \
   --test ../app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk \
   --e class com.karhoo.uisdk.address.AddressTests \
   --platformVersion='10' \
   --deviceNameQuery='Samsung Galaxy S10'
   --help
