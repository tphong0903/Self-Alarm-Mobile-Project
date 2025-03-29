package hcmute.edu.vn.selfalarmproject.views;

import android.content.Intent;
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
import hcmute.edu.vn.selfalarmproject.service.MusicService;
import hcmute.edu.vn.selfalarmproject.utils.ServiceUtils;


public class MusicFragment extends Fragment {

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_music, container, false);
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

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
//        if(ServiceUtils.isServiceRunning(requireContext(), MusicService.class)){
//            requireContext().stopService(new Intent(requireContext(), MusicService.class));
//        }
    }
}