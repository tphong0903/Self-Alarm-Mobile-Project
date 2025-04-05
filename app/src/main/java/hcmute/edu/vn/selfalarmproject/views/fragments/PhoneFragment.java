package hcmute.edu.vn.selfalarmproject.views.fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

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

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.views.adapters.PhoneAdapter;

public class PhoneFragment extends Fragment {
    private final List<String[]> contactList = new ArrayList<>();
    private final List<String[]> filteredContactList = new ArrayList<>();

    private PhoneAdapter phoneAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddContact;
    private EditText searchEditText;
    private ImageView clearSearchIcon;


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
        searchEditText = view.findViewById(R.id.searchEditText);
        clearSearchIcon = view.findViewById(R.id.clearSearchIcon);

        phoneAdapter = new PhoneAdapter(filteredContactList);
        recyclerView.setAdapter(phoneAdapter);

        checkPermissions();

        fabAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            v.getContext().startActivity(intent);
        });

        setupSearchFunctionality();
        return view;
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterContacts(s.toString());
                clearSearchIcon.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });

        clearSearchIcon.setOnClickListener(v -> {
            searchEditText.setText("");
            clearSearchIcon.setVisibility(View.GONE);
        });
    }

    private void filterContacts(String query) {
        filteredContactList.clear();

        if (query.isEmpty()) {
            filteredContactList.addAll(contactList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (String[] contact : contactList) {
                if (contact[0].toLowerCase().contains(lowerCaseQuery) ||
                        contact[1].toLowerCase().contains(lowerCaseQuery)) {
                    filteredContactList.add(contact);
                }
            }
        }

        phoneAdapter.notifyDataSetChanged();
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
        filteredContactList.clear();
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

        filteredContactList.addAll(contactList);
        phoneAdapter.notifyDataSetChanged();

        Log.d("PhoneFragment", "Số lượng danh bạ: " + contactList.size());
    }
}