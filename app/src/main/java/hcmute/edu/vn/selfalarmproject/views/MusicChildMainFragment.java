package hcmute.edu.vn.selfalarmproject.views;

import android.content.BroadcastReceiver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.SimpleExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.adapters.SongRecyclerAdapter;
import hcmute.edu.vn.selfalarmproject.models.SongModel;
import hcmute.edu.vn.selfalarmproject.adapters.ShareSongViewModel;
import hcmute.edu.vn.selfalarmproject.service.MusicService;
import hcmute.edu.vn.selfalarmproject.utils.ServiceUtils;


@UnstableApi
public class MusicChildMainFragment extends Fragment {
//    public static MediaPlayer mediaPlayer;
    public static SimpleExoPlayer exoPlayer;
    ShareSongViewModel viewModel;
    Handler handler;
    TextView temp, remain;
    RelativeLayout musicBar;
    RecyclerView recyclerView;
    FirebaseFirestore firestore;
    public static List<SongModel> musicList = new ArrayList<>();;
    SongRecyclerAdapter songRecyclerAdapter;
    LoadingAlert loadingAlert;
    ImageButton musicBarBtn;
    private long lastClickTime;
    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (exoPlayer != null) {
                int currentPosition = (int) exoPlayer.getCurrentPosition();
                int duration = (int) exoPlayer.getDuration();

                int remainTime = duration - currentPosition;

                viewModel.setPassTime(currentPosition);
                viewModel.setRemainTime(remainTime);

                temp.setText(formatDuration(currentPosition));
//                remain.setText(formatDuration(remainTime));

                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_musicmain_child, container, false);

        componentInit(v);

        songRecyclerAdapter = new SongRecyclerAdapter(this.getContext(), musicList,  position -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 1000) {
                return;
            }
            lastClickTime = currentTime;
            songChange(v, position);
//            Intent serviceIntent = new Intent(requireContext(), MusicService.class);
//            SongModel songModel = musicList.get(position);
//            serviceIntent.putExtra("title", songModel.getTitle());
//            serviceIntent.putExtra("songURL", songModel.getSongURL());


//            requireContext().stopService(serviceIntent);
//            requireContext().startService(serviceIntent);

        });

        recyclerView.setAdapter(songRecyclerAdapter);
        refreshList();

        if(ServiceUtils.isServiceRunning(requireContext(), MusicService.class)){
            Log.d("Service running", "Service running");
            songChange(v, viewModel.getPosition().getValue());
        }
        else {
            Log.d("Service not running", "Service not running");
        }

        viewModel.getPosition().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer position) {
                if(viewModel.getPosition().getValue() != -1){
                    songChange(v, position);
                    Log.i("Info", "It runs");
                }
            }
        });


        musicBar.setOnClickListener(view -> {
            switchFragment();
        });

        viewModel.getPlayStatus().observe(getViewLifecycleOwner(), status -> {
            if(status){
                musicBarBtn.setImageResource(R.drawable.baseline_pause_24);
            }
            else{
                musicBarBtn.setImageResource(R.drawable.baseline_play_arrow_24);
            }
        });

        musicBarBtn.setOnClickListener(view -> {
            if(exoPlayer != null){
                if(exoPlayer.isPlaying()){
                    viewModel.setStatus(false);
                    exoPlayer.pause();
                    musicBarBtn.setImageResource(R.drawable.baseline_play_arrow_24);
                }
                else {
                    viewModel.setStatus(true);
                    exoPlayer.play();
                    musicBarBtn.setImageResource(R.drawable.baseline_pause_24);
                }
            }
        });

        return v;
    }
    private void songChange(View v, int position){
        if(!musicList.isEmpty()){
            SongModel selected_song = null;
            if(position == musicList.size()){
                viewModel.setPosition(0);
            } else if (position < 0) {
                viewModel.setPosition(musicList.size() - 1);
            }
            else{
                selected_song = musicList.get(position);
                viewModel.setSong(selected_song);

                TextView musicBarTitle = (TextView) v.findViewById(R.id.musicBar_title);
                TextView musicBarArtist = (TextView) v.findViewById(R.id.musicBar_artist);
                ImageView image = (ImageView) v.findViewById(R.id.musicBar_img);

                musicBar.setVisibility(View.VISIBLE);
                musicBarTitle.setText(selected_song.getTitle());
                musicBarArtist.setText(selected_song.getAuthor());
                Glide.with(this).load(selected_song.getImageURL()).into(image);

                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(selected_song.getSongURL()));
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.play();
                exoPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int state) {
                        if (state == Player.STATE_READY) { // Ensures duration is available
                            long duration = exoPlayer.getDuration();
                            if (duration != C.TIME_UNSET) {
                                viewModel.setSongDuration((int) exoPlayer.getDuration());
                            }
                        }
                    }
                });
                viewModel.setSongDuration((int) exoPlayer.getDuration());
                startUpdatingTime();
                viewModel.setStatus(true);

//                try {
//                    if(mediaPlayer.isPlaying()){
//                        mediaPlayer.release();
//                        mediaPlayer = null;
//                    }
//                    mediaPlayer = new MediaPlayer();
//                    mediaPlayer.setDataSource(selected_song.getSongURL());
//                    mediaPlayer.prepareAsync();
//                    mediaPlayer.setOnPreparedListener(MediaPlayer::start);
//
//                    viewModel.setStatus(true);
//                    startUpdatingTime();
//                }
//                catch (Exception e){
//                    e.printStackTrace();
//                }
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d("Fragment Destroy", "Fragment Destroy");
        super.onDestroy();
        exoPlayer.stop();
        exoPlayer.release();
        exoPlayer = null;
    }

    private void componentInit(View v){
        viewModel = new ViewModelProvider(requireActivity()).get(ShareSongViewModel.class);
        viewModel.setPosition(-1);
        handler = new Handler();
//        mediaPlayer = new MediaPlayer();
        musicBar = v.findViewById(R.id.musicBar);
        temp = v.findViewById(R.id.timeDemo);
        musicBarBtn = v.findViewById(R.id.musicBar_btn);
        recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        firestore = FirebaseFirestore.getInstance();
        loadingAlert = new LoadingAlert(getActivity());
        exoPlayer = new SimpleExoPlayer.Builder(requireContext()).build();
    }

    private void startUpdatingTime() {
        handler.post(updateTimeRunnable);
    }
    private void stopUpdatingTime() {
        handler.removeCallbacks(updateTimeRunnable);
    }
    @NonNull
    private String formatDuration(int durationMs) {
        int seconds = (durationMs / 1000) % 60;
        int minutes = (durationMs / 1000) / 60;
        return minutes + ":" + String.format("%02d", seconds);
    }

    public void switchFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);

        MusicChildMainFragment fragment1 = (MusicChildMainFragment) getActivity().getSupportFragmentManager().findFragmentByTag("FRAG1");
        MusicDetailChildFragment fragment2 = (MusicDetailChildFragment) getActivity().getSupportFragmentManager().findFragmentByTag("FRAG2");

        transaction.hide(fragment1);
        transaction.show(fragment2);
        transaction.commit();
    }

    public boolean getDataFromFireStore(){
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
                                        document.getData().get("title").toString(),
                                        document.getData().get("artist").toString(),
                                        null,
                                        document.getData().get("image").toString(),
                                        document.getData().get("url").toString()
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
        }
        catch (Exception e){
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