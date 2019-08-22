package com.easyar.helper;

import android.text.TextUtils;
import android.util.Patterns;
import android.webkit.URLUtil;

import java.net.URL;
import java.util.regex.Pattern;

public class StringHelper {

    public static boolean isValidURL(CharSequence input) {
        if (TextUtils.isEmpty(input)) {
            return false;
        }
        Pattern URL_PATTERN = Patterns.WEB_URL;
        boolean isURL = URL_PATTERN.matcher(input).matches();
        if (!isURL) {
            String urlString = input + "";
            if (URLUtil.isNetworkUrl(urlString)) {
                try {
                    new URL(urlString);
                    isURL = true;
                } catch (Exception e) {
                }
            }
        }
        return isURL;
    }

    public static boolean isNullOrBlank(String s) {
        return (s == null || s.trim().equals(""));
    }

    public static boolean isNullOrBlank(CharSequence s) {
        return (s == null || s.toString().trim().equals(""));
    }

}
