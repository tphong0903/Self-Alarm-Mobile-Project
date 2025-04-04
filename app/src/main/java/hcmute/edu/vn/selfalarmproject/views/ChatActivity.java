package hcmute.edu.vn.selfalarmproject.views;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.adapters.ChatAdapter;
import hcmute.edu.vn.selfalarmproject.models.MessageModel;
import hcmute.edu.vn.selfalarmproject.utils.SharedPreferencesHelper;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText etMessage;
    private Button btnSend;
    private ChatAdapter chatAdapter;
    private List<MessageModel> messageList = new ArrayList<>();
    private DatabaseReference messagesRef;
    private String selectedMessageId;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(messageList, this);
        recyclerView.setAdapter(chatAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            selectedMessageId = intent.getStringExtra("messageId");
            Log.d(TAG, "Received message id: " + selectedMessageId);
        }
        String googleUid = SharedPreferencesHelper.getGoogleUid(this);
        if (googleUid == null) {
            googleUid = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            googleUid = googleUid.replaceAll("[^0-9]", "");

        }
        messagesRef = FirebaseDatabase.getInstance("https://week6-8ecb2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference(googleUid);

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    MessageModel message = data.getValue(MessageModel.class);
                    if (message != null) {
                        boolean isMessageFromSelectedUser = message.getSender().equals(selectedMessageId) && message.getReceiver().equals("Tôi");
                        boolean isMessageFromMeToSelectedUser = message.getSender().equals("Tôi") && message.getReceiver().equals(selectedMessageId);

                        if (isMessageFromSelectedUser || isMessageFromMeToSelectedUser) {
                            messageList.add(message);
                        }
                    }
                }
                chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });

        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                sendSMS(messageText, selectedMessageId);
                MessageModel message = new MessageModel(selectedMessageId, "Tôi", selectedMessageId, messageText, true, time);
                messagesRef.push().setValue(message)
                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Gửi tin nhắn thành công!"))
                        .addOnFailureListener(e -> Log.e("Firebase", "Lỗi khi gửi tin nhắn", e));

                etMessage.setText("");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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


}
