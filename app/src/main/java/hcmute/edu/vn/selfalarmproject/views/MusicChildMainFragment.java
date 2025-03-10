package hcmute.edu.vn.selfalarmproject.views;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.models.SongModel;
import hcmute.edu.vn.selfalarmproject.adapters.ShareSongViewModel;
import hcmute.edu.vn.selfalarmproject.adapters.ListViewAdapter;


public class MusicChildMainFragment extends Fragment {


    MediaPlayer mediaPlayer;
    ShareSongViewModel viewModel;
    Handler handler;
    TextView temp, remain;
    RelativeLayout musicBar;
    ListView listView;
    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();

                int remainTime = duration - currentPosition;

                viewModel.setPassTime(currentPosition);
                viewModel.setRemainTime(remainTime);

//                temp.setText(formatDuration(currentPosition));
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

        ComponentInit(v);

        List<SongModel> musicList = new ArrayList<>();

        int[] rawFiles = {
                R.raw.duchotanthe,
                R.raw.vothuong,
                R.raw.anhmatbietcuoi,
                R.raw.daudetruongthanh,
                R.raw.emkhongthe,
                R.raw.hetthuongcannhoremix,
                R.raw.yeulathathu,
                R.raw.matketnoi,
                R.raw.macarong,
                R.raw.kheuoc,
                R.raw.honcayeu
        };

        int[] songImg = {
                R.drawable.duchotanthe,
                R.drawable.vothuong,
                R.drawable.anhmatbietcuoi,
                R.drawable.daudetruongthanh,
                R.drawable.emkhongthe,
                R.drawable.hetthuongcannhoremix,
                R.drawable.yeulathathu,
                R.drawable.matketnoi,
                R.drawable.macarong,
                R.drawable.macarong,
                R.drawable.honcayeu
        };

        // Debug
//        Log.i("Info", rawFiles.length + "");

        for (int i = 0; i < rawFiles.length; i++) {
            SongModel music = getMusicMetadata(rawFiles[i]);
            if (music != null) {
                musicList.add(new SongModel(rawFiles[i], songImg[i], music.getTitle(), music.getAuthor(),  music.getDuration()));
            }
            else {
                Log.d("Debug", "Empty raw file");
            }
        }

        // Debug
//        for(Song s : musicList){
//            Log.i("Info", s.toString());
//        }

        listView.setAdapter(new ListViewAdapter(musicList));

        viewModel.getPosition().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer position) {
                songChange(v, position);
                Log.i("Info", "It runs");
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            viewModel.setPosition(position);
            songChange(v, position);
        });


        musicBar.setOnClickListener(view -> {
            switchFragment();
        });

        return v;
    }
    private void songChange(View v, int position){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        SongModel selected_song;

        if(position == listView.getCount()){
            selected_song = (SongModel) listView.getItemAtPosition(0);
            viewModel.setPosition(0);
        }
        else if(position < 0){
            int curr_pos = listView.getCount() - 1;
            selected_song = (SongModel) listView.getItemAtPosition(curr_pos);
            viewModel.setPosition(curr_pos);
        }
        else {
            selected_song = (SongModel) listView.getItemAtPosition(position);
        }

        mediaPlayer = MediaPlayer.create(getContext(), selected_song.getSongFileID());
        viewModel.setSongMeta(mediaPlayer);
        mediaPlayer.start();

        viewModel.setSong(selected_song);
        viewModel.setSongDuration(mediaPlayer.getDuration());

        TextView musicBarTitle = (TextView) v.findViewById(R.id.musicBar_title);
        TextView musicBarArtist = (TextView) v.findViewById(R.id.musicBar_artist);
        ImageView image = (ImageView) v.findViewById(R.id.musicBar_img);

        musicBar.setVisibility(View.VISIBLE);
        musicBarTitle.setText(selected_song.getTitle());
        musicBarArtist.setText(selected_song.getAuthor());
        image.setImageResource(selected_song.getImgID());

        startUpdatingTime();
    }
    private void ComponentInit(View v){
        viewModel = new ViewModelProvider(requireActivity()).get(ShareSongViewModel.class);
        handler = new Handler();

        musicBar = v.findViewById(R.id.musicBar);
//        temp = (TextView) v.findViewById(R.id.timeDemo);
//        remain = (TextView) v.findViewById(R.id.remainDemo);
        listView = v.findViewById(R.id.listView);
    }

    private void startUpdatingTime() {
        handler.post(updateTimeRunnable);
    }
    private void stopUpdatingTime() {
        handler.removeCallbacks(updateTimeRunnable);
    }
    private SongModel getMusicMetadata(int resId) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this.getContext(), Uri.parse("android.resource://" + this.getContext().getPackageName() + "/" + resId));
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String author = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();

            if (title == null) title = "Unknown Title";
            if (author == null) author = "Unknown Artist";
            if (durationMs == null) durationMs = "0";

            int duration = Integer.parseInt(durationMs);
            String formattedDuration = formatDuration(duration);

            return new SongModel(title, author, formattedDuration);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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
}