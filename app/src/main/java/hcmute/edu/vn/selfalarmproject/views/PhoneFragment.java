package hcmute.edu.vn.selfalarmproject.views;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.selfalarmproject.adapters.PhoneAdapter;
import hcmute.edu.vn.selfalarmproject.R;

public class PhoneFragment extends Fragment {
    private final List<String[]> contactList = new ArrayList<>();
    private PhoneAdapter phoneAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddContact;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("PhoneFragment", "Quyền danh bạ đã được cấp");
                    loadContacts();
                } else {
                    Log.d("PhoneFragment", "Quyền danh bạ bị từ chối");
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        fabAddContact = view.findViewById(R.id.fabAddContact);
        phoneAdapter = new PhoneAdapter(contactList);
        recyclerView.setAdapter(phoneAdapter);

        checkPermissions();

        fabAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            v.getContext().startActivity(intent);
        });


        return view;
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS);
        } else {
            loadContacts();
        }
    }

    private void loadContacts() {
        contactList.clear();
        ContentResolver contentResolver = requireContext().getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = contentResolver.query(uri, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                contactList.add(new String[]{name, phone});
            }
            cursor.close();
        }

        Log.d("PhoneFragment", "Số lượng danh bạ: " + contactList.size());

    }
}
