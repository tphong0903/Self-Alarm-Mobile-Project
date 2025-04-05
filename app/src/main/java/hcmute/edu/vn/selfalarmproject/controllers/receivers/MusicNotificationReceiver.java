package hcmute.edu.vn.selfalarmproject.controllers.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import java.util.Objects;

import hcmute.edu.vn.selfalarmproject.controllers.service.MusicService;
import hcmute.edu.vn.selfalarmproject.views.adapters.ShareSongViewModel;


public class MusicNotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PREV = "ACTION_PREV";
    public static final String ACTION_NEXT = "ACTION_NEXT";

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Action", Objects.requireNonNull(intent.getAction()));
        if (intent.getAction() != null) {
            switch ((intent.getAction())) {
                case ACTION_PREV:
                    ShareSongViewModel.setPosition(ShareSongViewModel.getPosition().getValue() - 1);
                    break;
                case ACTION_PLAY:
                    ShareSongViewModel.setStatus(!ShareSongViewModel.getPlayStatus().getValue());
                    if (ShareSongViewModel.getPlayStatus().getValue()) {
                        MusicService.exoPlayer.play();
                    } else {
                        MusicService.exoPlayer.pause();
                    }
                    break;
                case ACTION_NEXT:
                    ShareSongViewModel.setPosition(ShareSongViewModel.getPosition().getValue() + 1);
                    break;
            }
        }
    }
}
