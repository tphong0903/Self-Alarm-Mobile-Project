package hcmute.edu.vn.selfalarmproject.views.activities;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.models.MessageModel;
import hcmute.edu.vn.selfalarmproject.utils.SharedPreferencesHelper;
import hcmute.edu.vn.selfalarmproject.views.adapters.ContactAdapter;

public class NewMessageActivity extends AppCompatActivity {
    private EditText tvTitle, etMessage;
    private ImageButton btnSend;
    private RecyclerView rvMessages;
    private ContactAdapter contactAdapter;
    private FirebaseAuth firebaseAuth;

    private List<String> contactList = new ArrayList<>();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Quyền danh bạ đã được cấp", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Quyền truy cập danh bạ bị từ chối!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_message);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvTitle = findViewById(R.id.tvTitle);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        rvMessages = findViewById(R.id.rvMessages);

        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(contactList, contact -> {
            tvTitle.setText(contact);
            rvMessages.setVisibility(View.GONE);
        });

        rvMessages.setAdapter(contactAdapter);

        checkPermissions();
        String googleUid = SharedPreferencesHelper.getGoogleUid(this);
        if (googleUid == null) {
            googleUid = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            googleUid = googleUid.replaceAll("[^0-9]", "");

        }

        DatabaseReference messagesRef = FirebaseDatabase.getInstance("https://week6-8ecb2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference(googleUid);

        btnSend.setOnClickListener(v -> {
            String selectedMessageId = tvTitle.getText().toString().replaceAll("[^0-9]", "");
            Log.d("SEND", "Selected message ID: " + tvTitle.getText().toString());
            String messageContent = etMessage.getText().toString().trim();
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            MessageModel message = new MessageModel(selectedMessageId, "Tôi", selectedMessageId, messageContent, true, time);
            if (!messageContent.isEmpty()) {
                Toast.makeText(this, "Tin nhắn: " + messageContent, Toast.LENGTH_SHORT).show();
                sendSMS(messageContent, selectedMessageId);
                messagesRef.push().setValue(message)
                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Gửi tin nhắn thành công!"))
                        .addOnFailureListener(e -> Log.e("Firebase", "Lỗi khi gửi tin nhắn", e));
                etMessage.setText("");
            } else {
                Toast.makeText(this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });

        tvTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    rvMessages.setVisibility(View.GONE);
                } else {
                    rvMessages.setVisibility(View.VISIBLE);
                }
                searchContacts(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS);
        }
    }

    private void searchContacts(String query) {
        contactList.clear();
        if (query.isEmpty()) {
            contactAdapter.notifyDataSetChanged();
            return;
        }

        ContentResolver contentResolver = getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ? OR " +
                ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};

        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
        int count = 0;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String contactNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                contactList.add(contactName + " - " + contactNumber);
                count++;
            }
            cursor.close();
        }

        Log.d("ContactSearch", "Found " + count + " matching contacts");
        contactAdapter.notifyDataSetChanged();
    }

    public void sendSMS(String messageText, String phone) {
        SmsManager smsManager = SmsManager.getDefault();
        try {
            smsManager.sendTextMessage(phone, null, messageText, null, null);
            Log.d("SMS", "Gửi tin nhắn SMS thành công!");
        } catch (Exception e) {
            Log.e("SMS", "Lỗi khi gửi tin nhắn SMS", e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
