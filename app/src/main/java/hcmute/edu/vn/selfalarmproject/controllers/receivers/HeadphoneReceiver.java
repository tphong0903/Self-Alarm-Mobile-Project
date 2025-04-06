package hcmute.edu.vn.selfalarmproject.controllers.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import hcmute.edu.vn.selfalarmproject.controllers.services.MusicService;
import hcmute.edu.vn.selfalarmproject.views.viewmodels.ShareSongViewModel;

@UnstableApi
public class HeadphoneReceiver extends BroadcastReceiver {
    private static final String TAG = "HeadphoneReceiver";
    ExoPlayer exoPlayer;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        exoPlayer = MusicService.exoPlayer;

        if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case 0:
                    Log.d(TAG, "Wired Headphones Disconnected");
                    if (exoPlayer != null && exoPlayer.isPlaying()) {
                        exoPlayer.stop();
                        ShareSongViewModel.setStatus(false);
                    }
                    break;
                case 1:
                    Log.d(TAG, "Wired Headphones Connected");
                    break;
                default:
                    Log.d(TAG, "Unknown Wired Headphone State");
            }
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "Bluetooth Headphones Connected: " + device.getName());
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "Bluetooth Headphones Disconnected: " + device.getName());
            if (exoPlayer != null && exoPlayer.isPlaying()) {
                exoPlayer.pause();
                ShareSongViewModel.setStatus(false);
            }
        }
    }
}
