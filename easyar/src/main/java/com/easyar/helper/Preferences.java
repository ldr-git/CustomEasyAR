package com.easyar.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.BoolRes;
import androidx.annotation.IntegerRes;
import androidx.annotation.StringRes;

import com.easytargetar.BuildConfig;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class Preferences {
    private static final String TAG = Preferences.class.getSimpleName();

    private static String DATABASE_NAME;

    private static SharedPreferences cache;
    private static boolean dirty = true;
    private static SharedPreferences.Editor currentEditor;
    private static int held = 0;
    private static WeakHashMap<SharedPreferences.OnSharedPreferenceChangeListener, Void> observers = new WeakHashMap<>();

    private static Context context;

    public static void invalidate() {
        dirty = true;
    }

    public static void init(Context ctx, String name) {
        context = ctx.getApplicationContext();
        DATABASE_NAME = name;
    }

    private static SharedPreferences.Editor getEditor(SharedPreferences pref) {
        if (currentEditor == null) {
            currentEditor = pref.edit();
        }
        return currentEditor;
    }

    public static String encrypt(String input) {
        if (!StringHelper.isNullOrBlank(input))
            return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
        else return input;
    }

    public static String decrypt(String input) {
        if (!StringHelper.isNullOrBlank(input))
            return new String(Base64.decode(input, Base64.DEFAULT));
        else return input;
    }

    public static int getInt(Enum<?> key, int def) {
        return getInt(key.toString(), def);
    }

    public static int getInt(String key, int def) {
        SharedPreferences pref = read();
        return pref.getInt(key, def);
    }

    public static float getFloat(Enum<?> key, float def) {
        SharedPreferences pref = read();
        return pref.getFloat(key.toString(), def);
    }

    public static float getFloat(String key, float def) {
        SharedPreferences pref = read();
        return pref.getFloat(key, def);
    }

    public static long getLong(Enum<?> key, long def) {
        return getLong(key.toString(), def);
    }

    public static long getLong(String key, long def) {
        SharedPreferences pref = read();
        return pref.getLong(key, def);
    }

    public static String getString(Enum<?> key, String def) {
        SharedPreferences pref = read();
        return decrypt(pref.getString(key.toString(), encrypt(def)));
    }

    public static String getString(String key, String def) {
        SharedPreferences pref = read();
        return decrypt(pref.getString(key, encrypt(def)));
    }

    public static String getString(Enum<?> key) {
        SharedPreferences pref = read();
        return decrypt(pref.getString(key.toString(), null));
    }

    public static String getString(String key) {
        SharedPreferences pref = read();
        return decrypt(pref.getString(key, null));
    }

    public static boolean getBoolean(Enum<?> key, boolean def) {
        return getBoolean(key.toString(), def);
    }

    public static boolean getBoolean(String key, boolean def) {
        SharedPreferences pref = read();
        return pref.getBoolean(key, def);
    }

    public static Object get(String key) {
        SharedPreferences pref = read();
        return pref.getAll().get(key);
    }

    public static Map<String, ?> getAll() {
        SharedPreferences pref = read();
        return pref.getAll();
    }

    public static Set<String> getAllKeys() {
        return new HashSet<>(getAll().keySet());
    }

    public static void setInt(Enum<?> key, int val) {
        setInt(key.toString(), val);
    }

    public static void setInt(String key, int val) {
        SharedPreferences pref = read();
        getEditor(pref).putInt(key, val);
        commitIfNotHeld();
        if (BuildConfig.DEBUG) Log.d(TAG, key + " = (int) " + val);
    }

    public static void setFloat(Enum<?> key, float val) {
        setFloat(key.toString(), val);
    }

    public static void setFloat(String key, float val) {
        SharedPreferences pref = read();
        getEditor(pref).putFloat(key, val);
        commitIfNotHeld();
        if (BuildConfig.DEBUG) Log.d(TAG, key + " = (float) " + val);
    }

    public static void setLong(Enum<?> key, long val) {
        setLong(key.toString(), val);
    }

    public static void setLong(String key, long val) {
        SharedPreferences pref = read();
        getEditor(pref).putLong(key, val);
        commitIfNotHeld();
        if (BuildConfig.DEBUG) Log.d(TAG, key + " = (long) " + val);
    }

    public static void setString(Enum<?> key, String val) {
        setString(key.toString(), val);
    }

    public static void setString(String key, String val) {
        SharedPreferences pref = read();
        getEditor(pref).putString(key, encrypt(val));
        commitIfNotHeld();
        if (BuildConfig.DEBUG) Log.d(TAG, key + " = (string) " + encrypt(val));
    }

    public static void setBoolean(Enum<?> key, boolean val) {
        setBoolean(key.toString(), val);
    }

    public static void setBoolean(String key, boolean val) {
        SharedPreferences pref = read();
        getEditor(pref).putBoolean(key, val);
        commitIfNotHeld();
        if (BuildConfig.DEBUG) Log.d(TAG, key + " = (bool) " + val);
    }

    public static boolean contains(final Enum<?> key) {
        SharedPreferences pref = read();
        return pref.contains(key.toString());
    }

    public static boolean contains(final String key) {
        SharedPreferences pref = read();
        return pref.contains(key);
    }

    public static void remove(Enum<?> key) {
        remove(key.toString());
    }

    public static void remove(String key) {
        SharedPreferences pref = read();
        getEditor(pref).remove(key);
        commitIfNotHeld();
        if (BuildConfig.DEBUG) Log.d(TAG, key + " removed");
    }

    public static int getInt(@StringRes final int keyStringResId, @IntegerRes final int defaultIntResId) {
        final Resources r = context.getResources();
        final String key = r.getString(keyStringResId);
        final Object value = get(key);
        if (value == null) {
            return r.getInteger(defaultIntResId);
        } else {
            return (int) value;
        }
    }

    public static boolean getBoolean(@StringRes final int keyStringResId, @BoolRes final int defaultIntResId) {
        final Resources r = context.getResources();
        final String key = r.getString(keyStringResId);
        final Object value = get(key);
        if (value == null) {
            return r.getBoolean(defaultIntResId);
        } else {
            return (boolean) value;
        }
    }

    private synchronized static void commitIfNotHeld() {
        if (held > 0) {
            // don't do anything now
        } else {
            if (currentEditor != null) {
                currentEditor.apply();
                currentEditor = null;
            }
        }
    }

    public synchronized static void hold() {
        held++;
    }

    public synchronized static void unhold() {
        if (held <= 0) {
            throw new RuntimeException("unhold called too many times");
        }
        held--;
        if (held == 0) {
            if (currentEditor != null) {
                currentEditor.apply();
                currentEditor = null;
            }
        }
    }

    public synchronized static void registerObserver(final SharedPreferences.OnSharedPreferenceChangeListener observer) {
        SharedPreferences pref = read();
        pref.registerOnSharedPreferenceChangeListener(observer);
        observers.put(observer, null);
    }

    public synchronized static void unregisterObserver(final SharedPreferences.OnSharedPreferenceChangeListener observer) {
        SharedPreferences pref = read();
        pref.unregisterOnSharedPreferenceChangeListener(observer);
        observers.remove(observer);
    }

    private synchronized static SharedPreferences read() {
        SharedPreferences res;
        if (dirty || cache == null) {
            long start = 0;
            if (BuildConfig.DEBUG) start = SystemClock.uptimeMillis();
            res = context.getSharedPreferences(DATABASE_NAME, Context.MODE_PRIVATE);
            if (BuildConfig.DEBUG)
                Log.d(DATABASE_NAME, "Preferences was read from disk in " + (SystemClock.uptimeMillis() - start) + " ms");
            dirty = false;
            // re-register observers if the SharedPreferences object changes
            if (cache != null && res != cache && observers.size() > 0) {
                for (final SharedPreferences.OnSharedPreferenceChangeListener observer : observers.keySet()) {
                    cache.unregisterOnSharedPreferenceChangeListener(observer);
                    res.registerOnSharedPreferenceChangeListener(observer);
                }
            }
            cache = res;
        } else {
            if (BuildConfig.DEBUG) Log.d(DATABASE_NAME, "Preferences was read from disk");
            res = cache;
        }
        return res;
    }

}