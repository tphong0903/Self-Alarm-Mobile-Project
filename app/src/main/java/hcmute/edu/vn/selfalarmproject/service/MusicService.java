package hcmute.edu.vn.selfalarmproject.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.util.Objects;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.adapters.ShareSongViewModel;

public class MusicService extends Service {
    ShareSongViewModel shareSongViewModel;
    private MediaPlayer mediaPlayer;
    private static final int NOTIFICATION_ID = 10;
    private static final String CHANNEL_ID = "MusicServiceChannel";
    private Handler handler = new Handler();
    private Runnable updateRunnable;
    String title, url;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service created", "Service created");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service started", "Service started");
        if (intent != null) {
            if (intent.hasExtra("MUSIC_URL")) {
                url = intent.getStringExtra("songURL");
                title = intent.getStringExtra("title");
                playMusic(url);
            }

            if ("PLAY".equals(intent.getAction())) {
                playMusic(url);
            } else if ("PAUSE".equals(intent.getAction())) {
                pauseMusic();
            } else if ("STOP".equals(intent.getAction())) {
                stopSelf();
            }
        }
        showNotification(title);
//        if(intent.getStringExtra("title") != null){
//            title = intent.getStringExtra("title");
//        }
//        if(intent.getStringExtra("songURL") != null){
//            url = intent.getStringExtra("songURL");
//        }
//        try {
//            mediaPlayer.setDataSource(url);
//            mediaPlayer.prepareAsync(); // Prepare asynchronously to avoid blocking UI thread
//            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    mp.start();
//                    updateRunnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                                Intent intent = new Intent("UPDATE_DURATION");
//                                Log.d("Duration", "Duration: " + (mediaPlayer.getCurrentPosition() / 1000) + "s");
//                                intent.putExtra("DURATION", mediaPlayer.getCurrentPosition());
//                                sendBroadcast(intent);
//                                handler.postDelayed(this, 1000); // Update every second
//                            }
//                        }
//                    };
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        showNotification(title);
//
//        handler.post(updateRunnable);

        return START_STICKY;
    }

    private void playMusic(String url) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                showNotification(title);
            });
        } catch (IOException e) {
            Log.e("MusicService", "Error loading audio: " + e.getMessage());
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        showNotification(title);
    }

    private void showNotification(String title) {
        createNotificationChannel();
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction("PLAY");
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(this, MusicService.class);
        pauseIntent.setAction("PAUSE");
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText(mediaPlayer != null && mediaPlayer.isPlaying() ? "Playing..." : "Paused")
                .setSmallIcon(R.drawable.baseline_queue_music_24)
                .addAction(R.drawable.baseline_play_arrow_24, "Play", playPendingIntent)
                .addAction(R.drawable.baseline_pause_24, "Pause", pausePendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();
//        Intent playPauseIntent = new Intent(this, MusicService.class);
//        playPauseIntent.setAction("PLAY_PAUSE");
//        PendingIntent pendingIntent = PendingIntent.getService(this, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle(title)
//                .setContentText(mediaPlayer.isPlaying() ? "Playing" : "Paused")
//                .setSmallIcon(R.drawable.baseline_queue_music_24)
////                .addAction(mediaPlayer.isPlaying() ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24, "Play/Pause", pendingIntent)
//                .setPriority(NotificationCompat.PRIORITY_LOW)
//                .setOngoing(true)
//                .build();

        startForeground(NOTIFICATION_ID, notification);
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
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
