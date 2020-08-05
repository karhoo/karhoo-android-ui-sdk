package com.karhoo.uisdk.util;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

public abstract class IntentUtils {

    public static Intent dialIntent(String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {

            if (phoneNumber.startsWith("00") && phoneNumber.length() > 2) {
                phoneNumber = phoneNumber.substring(2);
            }

            if (!phoneNumber.startsWith("+")) {
                phoneNumber = "+" + phoneNumber;
            }

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            return intent;
        }
        return null;
    }

}
