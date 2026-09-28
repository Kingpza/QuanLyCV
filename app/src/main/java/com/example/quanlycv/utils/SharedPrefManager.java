package com.example.quanlycv.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "QuanLyCVPrefs";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_LANGUAGE = "language"; // "vi" or "en"
    private static final String KEY_PIN_ENABLED = "pinEnabled";
    private static final String KEY_PIN_CODE = "pinCode";

    private final SharedPreferences pref;

    public SharedPrefManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserName(String name) {
        pref.edit().putString(KEY_USER_NAME, name).apply();
    }

    public void saveUserEmail(String email) {
        pref.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "Người dùng");
    }

    public void saveUserLogin(int userId, String name, String email) {
        pref.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public void logout() {
        pref.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }



    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "user@quanlycv.com");
    }

    public void setDarkMode(boolean enabled) {
        pref.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public boolean isDarkMode() {
        return pref.getBoolean(KEY_DARK_MODE, false);
    }

    public void setLanguage(String langCode) {
        pref.edit().putString(KEY_LANGUAGE, langCode).apply();
    }

    public String getLanguage() {
        return pref.getString(KEY_LANGUAGE, "vi");
    }

    public void setPinLock(boolean enabled, String pinCode) {
        pref.edit()
                .putBoolean(KEY_PIN_ENABLED, enabled)
                .putString(KEY_PIN_CODE, pinCode)
                .apply();
    }

    public boolean isPinEnabled() {
        return pref.getBoolean(KEY_PIN_ENABLED, false);
    }

    public String getPinCode() {
        return pref.getString(KEY_PIN_CODE, "");
    }
}
