#
#           Circle CI & secure.properties live in harmony
#
# Android convention is to store your API keys in a local, non-versioned
# gradle.properties file. Circle CI doesn't allow users to upload pre-populated
# gradle.properties files to store this secret information, but instaed allows
# users to store such information as environment variables.
#
# This script creates a local gradle.properties file on current the Circle CI
# instance. It then reads environment variable TEST_API_KEY_ENV_VAR which a user
# has defined in their Circle CI project settings environment variables, and
# writes this value to the Circle CI instance's gradle.properties file.
#
# You must execute this script via your circle.yml as a pre-process dependency,
# so your gradle build process has access to all variables.
#
#   dependencies:
#       pre:
#        - source environmentSetup.sh && copyEnvVarsToGradleSecureProperties

#!/usr/bin/env bash

function copyEnvVarsToGradleProperties() {
    GRADLE_PROPERTIES=$HOME"$CIRCLE_WORKING_DIRECTORY/secure.properties"
    export GRADLE_PROPERTIES
    echo "Gradle Secure Properties should exist at $GRADLE_PROPERTIES"

    if [ ! -f "$GRADLE_PROPERTIES" ]; then
        echo "Gradle Secure Properties does not exist"

        echo "Creating Gradle Secure Properties file..."
        touch $GRADLE_PROPERTIES

        echo "Writing Configs to secure.properties..."
        echo "GOOGLE_MAPS_API_KEY_DEBUG=$GOOGLE_MAPS_API_KEY_DEBUG" >> $GRADLE_PROPERTIES
        echo "GOOGLE_MAPS_API_KEY_RELEASE=$GOOGLE_MAPS_API_KEY_RELEASE" >> $GRADLE_PROPERTIES
        echo "ADYEN_GUEST_CHECKOUT_IDENTIFIER=\"ADYEN_GUEST_CHECKOUT_IDENTIFIER\"" >> app/secure.properties
        echo "ADYEN_GUEST_CHECKOUT_ORGANISATION_ID=\"$ADYEN_GUEST_CHECKOUT_ORGANISATION_ID\"" >> app/secure.properties
        echo "BRAINTREE_GUEST_CHECKOUT_IDENTIFIER=\"$BRAINTREE_GUEST_CHECKOUT_IDENTIFIER\"" >> app/secure.properties
        echo "BRAINTREE_GUEST_CHECKOUT_ORGANISATION_ID=\"$BRAINTREE_GUEST_CHECKOUT_ORGANISATION_ID\"" >> app/secure.properties
        echo "GUEST_CHECKOUT_REFERER=\"$GUEST_CHECKOUT_REFERER\"" >> app/secure.properties
        echo "ADYEN_CLIENT_ID=\"ADYEN_CLIENT_ID\"" >> app/secure.properties
        echo "ADYEN_CLIENT_SCOPE=\"ADYEN_CLIENT_SCOPE\"" >> app/secure.properties
        echo "BRAINTREE_CLIENT_ID=\"BRAINTREE_CLIENT_ID\"" >> app/secure.properties
        echo "BRAINTREE_CLIENT_SCOPE=\"BRAINTREE_CLIENT_SCOPE\"" >> app/secure.properties
        echo "STAGING_HOST=\"$STAGING_HOST\"" >> $GRADLE_PROPERTIES
        echo "STAGING_AUTH_HOST=\"$STAGING_AUTH_HOST\"" >> $GRADLE_PROPERTIES
        echo "STAGING_GUEST_HOST=\"$STAGING_GUEST_HOST\"" >> $GRADLE_PROPERTIES
        echo "ADYEN_PUBLIC_KEY=\"ADYEN_PUBLIC_KEY\"" >> $GRADLE_PROPERTIES
    fi
}

copyEnvVarsToGradleProperties
