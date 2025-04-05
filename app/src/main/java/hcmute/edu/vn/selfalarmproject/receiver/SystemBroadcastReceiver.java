package hcmute.edu.vn.selfalarmproject.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

public class SystemBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SystemBroadcastReceiver", "Battery status changed, sending broadcast...");

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale * 100;

        Intent broadcastIntent = new Intent("hcmute.edu.vn.ACTION_BATTERY_UPDATE");
        broadcastIntent.putExtras(intent.getExtras());
        context.sendBroadcast(broadcastIntent);
    }
}
