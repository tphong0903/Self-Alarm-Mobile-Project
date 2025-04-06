package hcmute.edu.vn.selfalarmproject.controllers.services;

import android.accounts.AccountManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.selfalarmproject.views.MainActivity;

public class SystemSettingsService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "SystemSettingsServiceChannel";

    private BroadcastReceiver batteryChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SystemSettingsService", "Received broadcast - handling battery level");

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level * 100 / (float) scale;

            adjustSettingsBasedOnBattery(batteryPct);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SystemSettingsService", "Service started!");

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryChangeReceiver, filter);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "System Settings Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Channel for System Settings Service");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private android.app.Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Battery Monitoring")
                .setContentText("Monitoring battery to adjust system settings")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void adjustSettingsBasedOnBattery(float batteryPct) {
        Log.d("SystemSettingsService", "Adjusting settings based on battery: " + batteryPct + "%");

        if (batteryPct < 20) {
            setScreenBrightness(0.2f);
        } else if (batteryPct < 50) {
            setScreenBrightness(0.5f);
        }

        if (batteryPct < 15) {
            toggleWifi(false);
        }

        if (batteryPct < 10) {
            toggleSync(false);
        }
    }

    private void toggleWifi(boolean enable) {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                wifiManager.setWifiEnabled(enable);
                Log.d("SystemSettingsService", "WiFi " + (enable ? "enabled" : "disabled"));
            }
        } catch (Exception e) {
            Log.e("SystemSettingsService", "Failed to toggle WiFi: " + e.getMessage());
        }
    }

    private void toggleSync(boolean enable) {
        try {
            ContentResolver.setMasterSyncAutomatically(enable);
            Log.d("SystemSettingsService", "Master sync set to " + enable);

            android.accounts.Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
            if (accounts != null && accounts.length > 0) {
                for (android.accounts.Account account : accounts) {
                    ContentResolver.setSyncAutomatically(account, "com.google", enable);
                    Log.d("SystemSettingsService", "Sync for account " + account.name + " set to " + enable);
                }
            } else {
                Log.d("SystemSettingsService", "No accounts found to configure sync settings");
            }
        } catch (Exception e) {
            Log.e("SystemSettingsService", "Failed to toggle sync: " + e.getMessage());
        }
    }

    private void setScreenBrightness(float brightness) {
        try {
            if (!Settings.System.canWrite(this)) {
                Log.w("SystemSettingsService", "No WRITE_SETTINGS permission. Requesting...");

                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
            }

            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

            int brightnessValue = (int) (brightness * 255);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue);

            Log.d("SystemSettingsService", "Brightness set to " + brightnessValue);

        } catch (Exception e) {
            Log.e("SystemSettingsService", "Failed to set brightness: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(batteryChangeReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
