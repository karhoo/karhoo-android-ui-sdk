#!/bin/bash
java -jar runner.jar espresso \
   -u "$SAUCE_USERNAME" \
   --apikey "$SAUCE_ACCESS_KEY" \
   --e 'class com.karhoo.uisdk.address.AddressTests' \
   --data-center EU \
   --app ../app/build/outputs/apk/debug/app-debug.apk \
   --test ../app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk \
   --device 'deviceNameQuery=Samsung Galaxy S10,platformVersion=10’
