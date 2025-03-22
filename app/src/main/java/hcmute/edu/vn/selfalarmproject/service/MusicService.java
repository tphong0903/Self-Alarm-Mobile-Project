package hcmute.edu.vn.selfalarmproject.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.SimpleExoPlayer;


import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.adapters.ShareSongViewModel;
import hcmute.edu.vn.selfalarmproject.views.MainActivity;
import hcmute.edu.vn.selfalarmproject.views.MusicChildMainFragment;

@UnstableApi
public class MusicService extends Service {
    public static SimpleExoPlayer exoPlayer;
    private static final String CHANNEL_ID = "MusicServiceChannel";
    private String title, url, artist;
    public static int pos;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service created", "Service created");
        exoPlayer = new SimpleExoPlayer.Builder(this).build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service started", "Service started");
        if(intent.getStringExtra("title") != null){
            title = intent.getStringExtra("title");
        }
        if(intent.getStringExtra("songURL") != null){
            url = intent.getStringExtra("songURL");
        }
        if(intent.getStringExtra("artist") != null){
            artist = intent.getStringExtra("artist");
        }
        if(intent.getIntExtra("position", -1) != -1){
            pos = intent.getIntExtra("position", -1);
        }

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    long duration = exoPlayer.getDuration();
                    if (duration != C.TIME_UNSET) {
                        Log.d("Duration", exoPlayer.getDuration() + "");
                        ShareSongViewModel.setSongDuration((int) exoPlayer.getDuration());
                        MusicChildMainFragment.startUpdatingTime();
                    }
                }
            }
        });

        startForeground(1, getNotification());
        return START_STICKY;
    }

    private Notification getNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title.isEmpty() ? "No title" : title)
                .setContentText(artist.isEmpty() ? "No artist" : artist)
                .setSmallIcon(R.drawable.baseline_queue_music_24)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID, "Music Service Channel",
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Service stopped", "Service stopped");
        if(exoPlayer.isPlaying()){
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
