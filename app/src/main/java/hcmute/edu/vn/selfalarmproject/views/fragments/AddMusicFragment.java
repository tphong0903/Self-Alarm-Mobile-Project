package hcmute.edu.vn.selfalarmproject.views.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.util.UnstableApi;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.models.SongModel;
import hcmute.edu.vn.selfalarmproject.utils.cloud.UploadFile;

@OptIn(markerClass = UnstableApi.class)
public class AddMusicFragment extends Fragment {
    Button selectAudio, submitBtn;
    ImageButton changeFragment;
    ImageView imageView;
    TextInputEditText titleInp, artistInp;
    TextInputLayout titleLayout, artistLayout;
    TextView songFile;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> audioPickerLauncher;
    Uri audioUri, imageURI;
    private static int imgCount = 1;
    FirebaseFirestore firestore;
    private SongModel songModel = new SongModel();
    Handler handler;
    LoadingAlert loadingAlert;
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

            titleInp.setText("");
            artistInp.setText("");
            imageView.setImageResource(R.drawable.banned);

            switchFragment();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            if (titleInp.getText().toString().isEmpty()) {
                titleLayout.setError("Title can't be empty");
                return;
            }
            if (artistInp.getText().toString().isEmpty()) {
                artistLayout.setError("Artists can't be empty");
                return;
            }

            songModel.setTitle(titleInp.getText().toString());
            songModel.setAuthor(artistInp.getText().toString());

            loadingAlert.startAlert();
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

            loadingAlert.stopAlert();
        });

        titleInp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    titleLayout.setError("Title can't be empty");
                } else {
                    titleLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        artistInp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    artistLayout.setError("Artist can't be empty");
                } else {
                    artistLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return v;
    }

    private void componentInit(View v) {
        selectAudio = v.findViewById(R.id.selectAudio);
        changeFragment = v.findViewById(R.id.fstFragment);
        titleInp = v.findViewById(R.id.titleInp);
        artistInp = v.findViewById(R.id.artistInp);
        submitBtn = v.findViewById(R.id.submitBtn);
        titleLayout = v.findViewById(R.id.titleInpLayout);
        artistLayout = v.findViewById(R.id.artistInpLayout);
        imageView = v.findViewById(R.id.albumImage);

        firestore = FirebaseFirestore.getInstance();
        handler = new Handler();
        loadingAlert = new LoadingAlert(getActivity());

        titleInp.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus && getActivity() != null) {
                getActivity().getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            } else if (getActivity() != null) {
                getActivity().getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        });

        artistInp.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus && getActivity() != null) {
                getActivity().getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            } else if (getActivity() != null) {
                getActivity().getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        });
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
        } catch (Exception e) {
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