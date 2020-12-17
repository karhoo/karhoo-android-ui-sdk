#!/bin/bash
	./sauce-runner-virtual-0.1.2-linux/bin/sauce-runner-virtual \
   -u "$SAUCE_USERNAME" \
   -k "$SAUCE_ACCESS_KEY" \
   -f espresso \
   --include-tests='class com.karhoo.uisdk.address.AddressTests' \
   --data-center eu-central-1 \
   -a ../app/build/outputs/apk/debug/app-debug.apk \
   -t ../app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk \
   -d 'deviceName=Google Pixel GoogleAPI Emulator,platformVersion=8.1'
