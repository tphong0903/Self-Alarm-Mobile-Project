package hcmute.edu.vn.selfalarmproject.views.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.controllers.receivers.MusicNotificationReceiver;
import hcmute.edu.vn.selfalarmproject.controllers.services.MusicService;
import hcmute.edu.vn.selfalarmproject.models.SongModel;
import hcmute.edu.vn.selfalarmproject.utils.ServiceUtils;
import hcmute.edu.vn.selfalarmproject.views.MainActivity;
import hcmute.edu.vn.selfalarmproject.views.adapters.SongRecyclerAdapter;
import hcmute.edu.vn.selfalarmproject.views.viewmodels.ShareSongViewModel;


@UnstableApi
public class MusicChildMainFragment extends Fragment {
    //    public static MediaPlayer mediaPlayer;
    public static ExoPlayer exoPlayer;
    static ShareSongViewModel viewModel;
    static Handler handler;
    static TextView temp;
    TextView remain, musicBarTitle, musicBarArtist;
    FloatingActionButton addMusicBtn;
    RelativeLayout musicBar;
    RecyclerView recyclerView;
    FirebaseFirestore firestore;
    public List<SongModel> musicList = new ArrayList<>();
    ;
    SongRecyclerAdapter songRecyclerAdapter;
    LoadingAlert loadingAlert;
    ImageButton musicBarBtn;
    private long lastClickTime;
    Intent serviceIntent;
    ImageView image;
    MediaSessionCompat mediaSessionCompat;
    private static final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (exoPlayer != null) {
                int currentPosition = (int) exoPlayer.getCurrentPosition();
                int duration = (int) exoPlayer.getDuration();

                int remainTime = duration - currentPosition;

                viewModel.setPassTime(currentPosition);
                viewModel.setRemainTime(remainTime);

                temp.setText(formatDuration(currentPosition));

                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_musicmain_child, container, false);

        componentInit(v);

        songRecyclerAdapter = new SongRecyclerAdapter(this.getContext(), musicList, position -> {
            Log.d("Clicked", "Song clicked");
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 1000) {
                return;
            }
            lastClickTime = currentTime;
            ShareSongViewModel.setPosition(position);
        });

        recyclerView.setAdapter(songRecyclerAdapter);
        refreshList();

        if (ServiceUtils.isServiceRunning(requireContext(), MusicService.class)) {
            Log.d("Service running", "Service running");
            loadingMusicBar(null, MusicService.title, MusicService.imageURL, MusicService.artist);
        } else {
            Log.d("Service not running", "Service not running");
        }


        viewModel.getPosition().removeObservers(getViewLifecycleOwner());
        if (!viewModel.getPosition().hasActiveObservers()) {
            viewModel.getPosition().observe(getViewLifecycleOwner(), position -> {
                Log.i("Pos", position + "");
                if (position != -100) {
                    songChange(position);
                    Log.i("Info", "It runs");
                }
            });
        }

        viewModel.getSong().observe(getViewLifecycleOwner(), new Observer<SongModel>() {
            @Override
            public void onChanged(SongModel value) {
                musicBar.setVisibility(View.VISIBLE);
                musicBarTitle.setText(value.getTitle());
                musicBarArtist.setText(value.getAuthor());
                Glide.with(requireContext()).load(value.getImageURL()).into(image);
            }
        });


        musicBar.setOnClickListener(view -> switchFragment("Detail"));

        ShareSongViewModel.getPlayStatus().observe(getViewLifecycleOwner(), status -> {
            if (status) {
                musicBarBtn.setImageResource(R.drawable.baseline_pause_24);
                showNotification(R.drawable.baseline_pause_24, ShareSongViewModel.getPosition().getValue());
            } else {
                musicBarBtn.setImageResource(R.drawable.baseline_play_arrow_24);
                showNotification(R.drawable.baseline_play_arrow_24, ShareSongViewModel.getPosition().getValue());
            }
        });

        musicBarBtn.setOnClickListener(view -> {
            if (exoPlayer != null) {
                if (exoPlayer.isPlaying()) {
                    ShareSongViewModel.setStatus(false);
                    exoPlayer.pause();
                    musicBarBtn.setImageResource(R.drawable.baseline_play_arrow_24);
                } else {
                    ShareSongViewModel.setStatus(true);
                    exoPlayer.play();
                    musicBarBtn.setImageResource(R.drawable.baseline_pause_24);
                }
            }
        });

        addMusicBtn.setOnClickListener(view -> switchFragment("Add"));

        return v;
    }

    private void songChange(int position) {
        Log.i("Song change", "Song change");
        if (!musicList.isEmpty()) {
            SongModel selected_song = null;
            if (position == musicList.size()) {
                ShareSongViewModel.setPosition(0);
            } else if (position < 0) {
                ShareSongViewModel.setPosition(musicList.size() - 1);
            } else {
                loadingAlert.startAlert();
                selected_song = musicList.get(position);

                SongModel songModel = musicList.get(position);
                serviceIntent.putExtra("title", songModel.getTitle());
                serviceIntent.putExtra("songURL", songModel.getSongURL());
                serviceIntent.putExtra("artist", songModel.getAuthor());
                serviceIntent.putExtra("imageURL", songModel.getImageURL());
                serviceIntent.putExtra("position", position);

                loadingMusicBar(selected_song, null, null, null);

                requireContext().stopService(serviceIntent);
                requireContext().startService(serviceIntent);
                showNotification(R.drawable.baseline_pause_24, position);

                ShareSongViewModel.setStatus(true);
                loadingAlert.stopAlert();
            }
        }
    }

    public void showNotification(int playBtn, int position) {
        if (!musicList.isEmpty()) {
            SongModel songModel = musicList.get(position);

            Intent intent = new Intent(requireContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
            Intent prevIntent = new Intent(requireContext(), MusicNotificationReceiver.class).setAction("ACTION_PREV");
            PendingIntent prevPendingIntent = PendingIntent.getBroadcast(requireContext(), 0, prevIntent, PendingIntent.FLAG_MUTABLE);
            Intent playIntent = new Intent(requireContext(), MusicNotificationReceiver.class).setAction("ACTION_PLAY");
            playIntent.putExtra("songURL", songModel.getSongURL());
            playIntent.putExtra("position", position);
            PendingIntent playPendingIntent = PendingIntent.getBroadcast(requireContext(), 0, playIntent, PendingIntent.FLAG_MUTABLE);
            Intent nextIntent = new Intent(requireContext(), MusicNotificationReceiver.class).setAction("ACTION_NEXT");
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(requireContext(), 0, nextIntent, PendingIntent.FLAG_MUTABLE);

            Glide.with(requireContext())
                    .asBitmap()
                    .load(musicList.get(position).getImageURL())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Notification notification = new NotificationCompat.Builder(requireContext(), "MusicServiceChannel")
                                    .setSmallIcon(R.drawable.baseline_queue_music_24)
                                    .setLargeIcon(resource)
                                    .setContentTitle(musicList.get(position).getTitle())
                                    .setContentText(musicList.get(position).getAuthor())
                                    .addAction(R.drawable.baseline_skip_previous_24, "Previous", prevPendingIntent)
                                    .addAction(playBtn, "Play", playPendingIntent)
                                    .addAction(R.drawable.baseline_skip_next_24, "Next", nextPendingIntent)
                                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setContentIntent(pendingIntent)
                                    .setOngoing(true)
                                    .setOnlyAlertOnce(true)
                                    .build();

                            NotificationManager notificationManager = (NotificationManager) requireActivity().getSystemService(requireContext().NOTIFICATION_SERVICE);
                            notificationManager.notify(1, notification);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }

    }

    public void loadingMusicBar(SongModel selected_song, String title, String imageURL, String artist) {
        if (selected_song != null) {
            viewModel.setSong(selected_song);
            Log.i("Song not null", "Song not null");
            musicBar.setVisibility(View.VISIBLE);
            musicBarTitle.setText(selected_song.getTitle());
            musicBarArtist.setText(selected_song.getAuthor());
            Glide.with(this).load(selected_song.getImageURL()).into(image);
        } else {
            Log.i("Song null", "Song null");
            musicBar.setVisibility(View.VISIBLE);
            musicBarTitle.setText(title);
            musicBarArtist.setText(artist);
            Glide.with(this).load(imageURL).into(image);
        }
    }

    @Override
    public void onDestroy() {
        Log.d("Fragment Destroy", "Fragment Destroy");
        super.onDestroy();
        NotificationManager notificationManager = (NotificationManager) requireActivity().getSystemService(requireContext().NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }

    private void componentInit(View v) {
        viewModel = new ViewModelProvider(requireActivity()).get(ShareSongViewModel.class);
        handler = new Handler();
        musicBar = v.findViewById(R.id.musicBar);
        temp = v.findViewById(R.id.timeDemo);
        musicBarBtn = v.findViewById(R.id.musicBar_btn);
        recyclerView = v.findViewById(R.id.recyclerView);
        addMusicBtn = v.findViewById(R.id.addMusic);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        firestore = FirebaseFirestore.getInstance();
        loadingAlert = new LoadingAlert(getActivity());
        exoPlayer = MusicService.exoPlayer;
        serviceIntent = new Intent(requireContext(), MusicService.class);

        musicBarTitle = (TextView) v.findViewById(R.id.musicBar_title);
        musicBarArtist = (TextView) v.findViewById(R.id.musicBar_artist);
        image = (ImageView) v.findViewById(R.id.musicBar_img);

        mediaSessionCompat = new MediaSessionCompat(requireContext(), "PlayerAudio");
    }

    public static void startUpdatingTime() {
        exoPlayer = MusicService.exoPlayer;
        handler.post(updateTimeRunnable);
    }

    private void stopUpdatingTime() {
        handler.removeCallbacks(updateTimeRunnable);
    }

    @NonNull
    private static String formatDuration(int durationMs) {
        int seconds = (durationMs / 1000) % 60;
        int minutes = (durationMs / 1000) / 60;
        return minutes + ":" + String.format("%02d", seconds);
    }

    public void switchFragment(String fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);

        MusicChildMainFragment fragment1 = (MusicChildMainFragment) getActivity().getSupportFragmentManager().findFragmentByTag("FRAG1");

        switch (fragment) {
            case "Detail":
                MusicDetailChildFragment fragment2 = (MusicDetailChildFragment) getActivity().getSupportFragmentManager().findFragmentByTag("FRAG2");

                assert fragment1 != null;
                transaction.hide(fragment1);
                assert fragment2 != null;
                transaction.show(fragment2);
                transaction.commit();

                break;
            case "Add":

                AddMusicFragment fragment3 = (AddMusicFragment) getActivity().getSupportFragmentManager().findFragmentByTag("FRAG3");

                assert fragment1 != null;
                transaction.hide(fragment1);
                assert fragment3 != null;
                transaction.show(fragment3);
                transaction.commit();

                break;
        }
    }

    public boolean getDataFromFireStore() {
        try {
            loadingAlert.startAlert();
            firestore.collection("songs")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            musicList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Firestore", document.getId() + " => " + document.getData());
                                SongModel querySong = new SongModel(
                                        document.getId(),
                                        document.getData().get("title") != null ? document.getData().get("title").toString() : "",
                                        document.getData().get("artist") != null ? document.getData().get("artist").toString() : "",
                                        document.getData().get("duration") != null ? document.getData().get("duration").toString() : "",
                                        document.getData().get("image") != null ? document.getData().get("image").toString() : "",
                                        document.getData().get("url") != null ? document.getData().get("url").toString() : ""
                                );
                                musicList.add(querySong);
                            }
                            songRecyclerAdapter.notifyDataSetChanged();
                            loadingAlert.stopAlert();
                        } else {
                            Log.w("Firestore", "Error getting documents.", task.getException());
                        }
                    });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void refreshList() {
        firestore.collection("songs")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w("Firestore", "Listen failed.", e);
                        return;
                    }
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            Log.d("Firestore", "Document updated: " + document.getData());
                        }
                        getDataFromFireStore();
                    } else {
                        Log.d("Firestore", "No documents found.");
                    }
                });
    }
}