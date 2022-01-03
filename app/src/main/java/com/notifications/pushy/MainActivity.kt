package com.notifications.pushy

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import me.pushy.sdk.Pushy


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Pushy.listen(this)
        setContentView(R.layout.activity_main)

        // Whitelist app from battery optimization
        // Android M (6) and up only
        // Android M (6) and up only
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Get power manager instance
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

            // Check if app isn't already whitelisted from battery optimizations
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                // Get app name as string
                val appName = packageManager.getApplicationLabel(applicationInfo).toString()

                // Instruct user to whitelist app from battery optimizations
                AlertDialog.Builder(this)
                    .setTitle("Disable battery optimizations")
                    .setMessage("If you'd like to receive notifications in the background, please click OK and select \"All apps\" -> $appName -> Don't optimize.")
                    .setPositiveButton(
                        "OK"
                    ) { dialogInterface, i -> // Display the battery optimization settings screen
                        startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    }
                    .setNegativeButton("Cancel", null).show()
            }
        }

        // Not registered yet?
        if (getDeviceToken() == null) {
            // Register with Pushy
            RegisterForPushNotificationsAsync(this).execute()
        } else {
            // Start Pushy notification service if not already running
            Pushy.listen(this)
        }

        // Enable FCM Fallback Delivery
        Pushy.toggleFCM(true, this);
    }

    private fun getDeviceToken(): String? {
        // Get token stored in SharedPreferences
        return getSharedPreferences().getString("deviceToken", null)
    }

    fun saveDeviceToken(deviceToken: String) {
        // Save token locally in app SharedPreferences
        getSharedPreferences().edit().putString("deviceToken", deviceToken).apply()

        // Your app should store the device token in your backend database
        //new URL("https://{YOUR_API_HOSTNAME}/register/device?token=" + deviceToken).openConnection();
    }

    private fun getSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

    inner class RegisterForPushNotificationsAsync(activity: Activity) :
        AsyncTask<Void, Void, Any>() {
        var activity: Activity = activity;

        override fun doInBackground(vararg params: Void): Any {
            try {
                // Register the device for notifications
                val deviceToken = Pushy.register(activity)

                // Registration succeeded, log token to logcat
                Log.d("Pushy", "Pushy device token: $deviceToken")

                // Save registration token
                saveDeviceToken(deviceToken)

                // Provide token to onPostExecute()
                return deviceToken
            } catch (exc: Exception) {
                // Registration failed, provide exception to onPostExecute()
                return exc
            }
        }

        override fun onPostExecute(result: Any) {
            val message: String

            // Registration failed?
            if (result is Exception) {
                // Log to console
                Log.e("Pushy", result.message.toString())

                // Display error in alert
                message = result.message.toString()
            } else {
                // Registration success, result is device token
                message = "Pushy device token: $result\n\n(copy from logcat)"

            }

            // Display dialog
            AlertDialog.Builder(activity)
                .setTitle("Pushy")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }
}
