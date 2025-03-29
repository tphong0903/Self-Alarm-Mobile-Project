package hcmute.edu.vn.selfalarmproject.views;

import android.content.Intent;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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
    static ShareSongViewModel viewModel;
    static Handler handler;
    static TextView temp;
    TextView remain, musicBarTitle, musicBarArtist;
    RelativeLayout musicBar;
    RecyclerView recyclerView;
    FirebaseFirestore firestore;
    public List<SongModel> musicList = new ArrayList<>();;
    SongRecyclerAdapter songRecyclerAdapter;
    LoadingAlert loadingAlert;
    ImageButton musicBarBtn;
    private long lastClickTime;
    Intent serviceIntent;
    ImageView image;
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
        View v =  inflater.inflate(R.layout.fragment_musicmain_child, container, false);

        componentInit(v);

        songRecyclerAdapter = new SongRecyclerAdapter(this.getContext(), musicList,  position -> {
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
    private void songChange(int position){
        Log.i("Song change", "Song change");
        if(!musicList.isEmpty()){
            SongModel selected_song = null;
            if(position == musicList.size()){
                ShareSongViewModel.setPosition(0);
            } else if (position < 0) {
                ShareSongViewModel.setPosition(musicList.size() - 1);
            }
            else{
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

                viewModel.setStatus(true);
            }
        }
    }

    public void loadingMusicBar(SongModel selected_song, String title, String imageURL, String artist){
        if(selected_song != null){
            viewModel.setSong(selected_song);
            Log.i("Song not null", "Song not null");
            musicBar.setVisibility(View.VISIBLE);
            musicBarTitle.setText(selected_song.getTitle());
            musicBarArtist.setText(selected_song.getAuthor());
            Glide.with(this).load(selected_song.getImageURL()).into(image);
        }
        else{
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
        viewModel.getPosition().removeObservers(getViewLifecycleOwner());
    }

    private void componentInit(View v){
        viewModel = new ViewModelProvider(requireActivity()).get(ShareSongViewModel.class);
//        ShareSongViewModel.setPosition(-100);
        handler = new Handler();
        musicBar = v.findViewById(R.id.musicBar);
        temp = v.findViewById(R.id.timeDemo);
        musicBarBtn = v.findViewById(R.id.musicBar_btn);
        recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        firestore = FirebaseFirestore.getInstance();
        loadingAlert = new LoadingAlert(getActivity());
        exoPlayer = MusicService.exoPlayer;
        serviceIntent = new Intent(requireContext(), MusicService.class);

        musicBarTitle = (TextView) v.findViewById(R.id.musicBar_title);
        musicBarArtist = (TextView) v.findViewById(R.id.musicBar_artist);
        image = (ImageView) v.findViewById(R.id.musicBar_img);
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
                                        document.getData().get("duration").toString(),
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