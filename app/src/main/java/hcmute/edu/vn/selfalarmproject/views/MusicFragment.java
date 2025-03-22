package hcmute.edu.vn.selfalarmproject.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.util.UnstableApi;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.adapters.ShareSongViewModel;


public class MusicFragment extends Fragment {


    @OptIn(markerClass = UnstableApi.class)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_music, container, false);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        MusicChildMainFragment mainFragment = new MusicChildMainFragment();
        MusicDetailChildFragment detailFragment = new MusicDetailChildFragment();

        transaction.add(R.id.music_child_fragment, mainFragment, "FRAG1");
        transaction.add(R.id.music_child_fragment, detailFragment, "FRAG2");
        transaction.hide(detailFragment);
        transaction.commit();

        return v;
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(MusicChildMainFragment.exoPlayer != null && MusicChildMainFragment.exoPlayer.isPlaying()){
            MusicChildMainFragment.exoPlayer.stop();
            MusicChildMainFragment.exoPlayer.release();
            MusicChildMainFragment.exoPlayer = null;
        }
        ShareSongViewModel.setPosition(-1);
    }
}