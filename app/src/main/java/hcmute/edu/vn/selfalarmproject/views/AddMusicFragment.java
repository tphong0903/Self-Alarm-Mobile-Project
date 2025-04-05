package hcmute.edu.vn.selfalarmproject.views;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.util.UnstableApi;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.cloud.CloudinaryManager;
import hcmute.edu.vn.selfalarmproject.cloud.UploadFile;
import hcmute.edu.vn.selfalarmproject.models.SongModel;
import hcmute.edu.vn.selfalarmproject.utils.AudioUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddMusicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@OptIn(markerClass = UnstableApi.class)
public class AddMusicFragment extends Fragment {
    Button selectAudio, submitBtn;
    ImageButton changeFragment;
    TextInputEditText titleInp, artistInp;
    TextView songFile;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> audioPickerLauncher;
    Uri audioUri, imageURI;
    private static int imgCount = 1;
    FirebaseFirestore firestore;
    private SongModel songModel = new SongModel();
    Handler handler;
    private final Runnable uploadCloud = new Runnable() {
        @Override
        public void run() {
            Map<String, Object> songCloud = new HashMap<>();
            songCloud.put("title", songModel.getTitle());
            songCloud.put("artist", songModel.getAuthor());
            songCloud.put("duration", songModel.getDuration());
            songCloud.put("image", songModel.getImageURL());
            songCloud.put("url", songModel.getSongURL());


            firestore.collection("songs")
                    .add(songCloud)
                    .addOnSuccessListener(documentReference ->
                            Log.d("Firestore", "Document added with ID: " + documentReference.getId()))
                    .addOnFailureListener(e ->
                            Log.w("Firestore", "Error adding document", e));

            switchFragment();
        }
    };


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddMusicFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddMusicFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddMusicFragment newInstance(String param1, String param2) {
        AddMusicFragment fragment = new AddMusicFragment();
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
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_music, container, false);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {

                });

        audioPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK) {
                        assert result.getData() != null;
                        audioUri = result.getData().getData();
                        Log.d("AudioUri", audioUri + "");
                        if (audioUri != null) {
                            getAudioMetadata(v, audioUri);
                        }
                    }
                });

        checkAndRequestPermission();

        componentInit(v);

        changeFragment.setOnClickListener(view -> {
            switchFragment();
        });

        selectAudio.setOnClickListener(view -> {
            openAudioPicker();
        });

        submitBtn.setOnClickListener(view -> {
            UploadFile.uploadFile(requireContext(), audioUri, new UploadFile.UploadCallback() {
                @Override
                public void onSuccess(String url) {
                    Log.d("UploadResult", "File uploaded to: " + url);
                    songModel.setSongURL(url);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("UploadResult", "Upload failed", e);
                }
            });

            UploadFile.uploadFile(requireContext(), imageURI, new UploadFile.UploadCallback() {
                @Override
                public void onSuccess(String url) {
                    Log.d("UploadResult", "File uploaded to: " + url);
                    songModel.setImageURL(url);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("UploadResult", "Upload failed", e);
                }
            });

            handler.postDelayed(uploadCloud, 7500);
        });

        return v;
    }

    private void componentInit(View v) {
        selectAudio = v.findViewById(R.id.selectAudio);
        changeFragment = v.findViewById(R.id.fstFragment);
        titleInp = v.findViewById(R.id.titleInp);
        artistInp = v.findViewById(R.id.artistInp);
        submitBtn = v.findViewById(R.id.submitBtn);
        songFile = v.findViewById(R.id.songFile);
        firestore = FirebaseFirestore.getInstance();
        handler = new Handler();
    }

    public void switchFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);

        MusicChildMainFragment fragment1 = (MusicChildMainFragment) getActivity().getSupportFragmentManager().findFragmentByTag("FRAG1");
        AddMusicFragment fragment2 = (AddMusicFragment) getActivity().getSupportFragmentManager().findFragmentByTag("FRAG3");

        assert fragment2 != null;
        transaction.hide(fragment2);
        assert fragment1 != null;
        transaction.show(fragment1);
        transaction.commit();
    }
    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireActivity().checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_AUDIO);
            }
        }
    }

    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        audioPickerLauncher.launch(intent);
    }
    private void getAudioMetadata(View v, Uri uri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(requireContext(), uri);

            byte[] artBytes = retriever.getEmbeddedPicture();
            if (artBytes != null) {
                Bitmap albumArt = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);

                File albumArtFile = saveBitmapToFile(albumArt);
                imageURI = Uri.fromFile(albumArtFile);

                ImageView imageView = v.findViewById(R.id.albumImage);
                imageView.setImageBitmap(albumArt);
            } else {
                Log.d("AudioMeta", "No embedded album art found");
            }

            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            retriever.release();

            songModel.setTitle(title);
            songModel.setAuthor(artist);
            songModel.setDuration(formatDuration(Integer.parseInt(duration)));

            Log.d("AudioMeta", "Title: " + title);
            Log.d("AudioMeta", "Artist: " + artist);
            Log.d("AudioMeta", "Duration: " + duration);

            titleInp.setText(title);
            artistInp.setText(artist);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private String formatDuration(int durationMs) {
        int seconds = (durationMs / 1000) % 60;
        int minutes = (durationMs / 1000) / 60;
        return minutes + ":" + String.format("%02d", seconds);
    }

    private File saveBitmapToFile(Bitmap bitmap) throws IOException {
        File albumArtFile = new File(requireContext().getCacheDir(), String.format("img%s.jpg", imgCount));
        imgCount++;
        FileOutputStream fos = new FileOutputStream(albumArtFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.close();
        return albumArtFile;
    }

}