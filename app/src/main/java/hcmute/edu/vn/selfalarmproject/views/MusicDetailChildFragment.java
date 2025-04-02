package hcmute.edu.vn.selfalarmproject.views;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.SimpleExoPlayer;

import com.bumptech.glide.Glide;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.adapters.ShareSongViewModel;
import hcmute.edu.vn.selfalarmproject.models.SongModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MusicDetailChildFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@UnstableApi
public class MusicDetailChildFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private long lastClickTime = 0;
    ShareSongViewModel viewModel;
//    MediaPlayer mediaPlayer;
    SimpleExoPlayer exoPlayer;
    ImageView songImg;
    ProgressBar progressBar;
    TextView songPassTime, songRemainTime, titleTextView, artistTextView;
    ImageButton prev, play, next, fst_fragment;

    public MusicDetailChildFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MusicDetailChildFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MusicDetailChildFragment newInstance(String param1, String param2) {
        MusicDetailChildFragment fragment = new MusicDetailChildFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

            String receivedData = getArguments().getString("key");
            Toast.makeText(getContext(), receivedData, Toast.LENGTH_SHORT).show();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music_detail_child, container, false);

        ComponentInit(view);

        fst_fragment.setOnClickListener(v -> {
            switchFragment();
        });

        play.setOnClickListener(v -> {
            exoPlayer = MusicChildMainFragment.exoPlayer;
            if(exoPlayer != null){
                if(exoPlayer.isPlaying()){
                    ShareSongViewModel.setStatus(false);
                    exoPlayer.pause();
                    play.setImageResource(R.drawable.baseline_play_arrow_24);
                }
                else {
                    ShareSongViewModel.setStatus(true);
                    exoPlayer.play();
                    play.setImageResource(R.drawable.baseline_pause_24);
                }
            }
        });

        prev.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTime < 1000) {
                return;
            }
            lastClickTime = System.currentTimeMillis();
            ShareSongViewModel.setPosition(viewModel.getPosition().getValue() - 1);
            play.setImageResource(R.drawable.baseline_pause_24);
        });

        next.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTime < 1000) {
                return;
            }
            lastClickTime = System.currentTimeMillis();
            ShareSongViewModel.setPosition(viewModel.getPosition().getValue() + 1);
            play.setImageResource(R.drawable.baseline_pause_24);
        });

        viewModel.getSong().observe(getViewLifecycleOwner(), new Observer<SongModel>() {
            @Override
            public void onChanged(SongModel value) {
                titleTextView.setText(value.getTitle());
                artistTextView.setText(value.getAuthor());
                Glide.with(requireContext()).load(value.getImageURL()).into(songImg);
            }
        });

        viewModel.getPlayStatus().observe(getViewLifecycleOwner(), status -> {
            if(status){
                play.setImageResource(R.drawable.baseline_pause_24);
            }
            else{
                play.setImageResource(R.drawable.baseline_play_arrow_24);
            }
        });

        viewModel.getPassTime().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                songPassTime.setText(formatDuration(s));
            }
        });

        viewModel.getRemainTime().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                songRemainTime.setText(formatDuration(s));

                Integer durationValue = viewModel.getSongDuration().getValue();
                int duration = (durationValue != null) ? durationValue : 0;

                progressBar.setProgress(duration - s);
            }
        });

        viewModel.getSongDuration().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                progressBar.setMax(integer);
            }
        });

//        viewModel.getSongMeta().observe(getViewLifecycleOwner(), new Observer<MediaPlayer>() {
//            @Override
//            public void onChanged(MediaPlayer mediaPlayer) {
//                play.setImageResource(R.drawable.baseline_pause_24);
//            }
//        });

        return view;
    }

    private void ComponentInit(View view) {
        fst_fragment = view.findViewById(R.id.fstFragment);
        titleTextView = view.findViewById(R.id.songTitle);
        artistTextView = view.findViewById(R.id.songArtist);
        songImg = view.findViewById(R.id.songImage);
        progressBar = view.findViewById(R.id.progressBar);
        songPassTime = view.findViewById(R.id.songPassTime);
        songRemainTime = view.findViewById(R.id.songRemainTime);
        prev = view.findViewById(R.id.previousBtn);
        play = view.findViewById(R.id.playBtn);
        next = view.findViewById(R.id.nextBtn);

        viewModel = new ViewModelProvider(requireActivity()).get(ShareSongViewModel.class);
    }

    public void switchFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);

        MusicChildMainFragment fragment1 = (MusicChildMainFragment) getActivity().getSupportFragmentManager().findFragmentByTag("FRAG1");
        MusicDetailChildFragment fragment2 = (MusicDetailChildFragment) getActivity().getSupportFragmentManager().findFragmentByTag("FRAG2");

        transaction.hide(fragment2);
        transaction.show(fragment1);
        transaction.commit();
    }

    @NonNull
    private String formatDuration(int durationMs) {
        int seconds = (durationMs / 1000) % 60;
        int minutes = (durationMs / 1000) / 60;
        return minutes + ":" + String.format("%02d", seconds);
    }
}