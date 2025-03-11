package hcmute.edu.vn.selfalarmproject.views;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.adapters.BlacklistAdapter;
import hcmute.edu.vn.selfalarmproject.models.BlacklistedNumber;
import hcmute.edu.vn.selfalarmproject.utils.SharedPreferencesHelper;

public class BlacklistActivity extends AppCompatActivity {

    private static final String TAG = "BlacklistActivity";
    private RecyclerView rvBlacklist;
    private TextView tvEmptyList;
    private EditText etPhoneNumber;
    private Button btnAdd;
    private BlacklistAdapter adapter;
    private List<BlacklistedNumber> blacklistedNumbers;
    private DatabaseReference blacklistRef;
    private String googleUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvBlacklist = findViewById(R.id.rvBlacklist);
        tvEmptyList = findViewById(R.id.tvEmptyList);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnAdd = findViewById(R.id.btnAdd);

        blacklistedNumbers = new ArrayList<>();
        rvBlacklist.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BlacklistAdapter(blacklistedNumbers, position -> {
            // Delete
            if (position != RecyclerView.NO_POSITION) {
                removeBlacklistedNumber(position);
            }
        });
        rvBlacklist.setAdapter(adapter);

        googleUid = SharedPreferencesHelper.getGoogleUid(this);
        if (googleUid != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://week6-8ecb2-default-rtdb.asia-southeast1.firebasedatabase.app");
            blacklistRef = database.getReference(googleUid).child("blacklist");
            loadBlacklist();
        } else {
            Toast.makeText(this, "Không thể xác định người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnAdd.setOnClickListener(v -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            if (!TextUtils.isEmpty(phoneNumber)) {
                addToBlacklist(phoneNumber);
            } else {
                Toast.makeText(BlacklistActivity.this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBlacklist() {
        blacklistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                blacklistedNumbers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    BlacklistedNumber number = dataSnapshot.getValue(BlacklistedNumber.class);
                    if (number != null) {
                        blacklistedNumbers.add(number);
                    }
                }
                adapter.notifyDataSetChanged();
                updateEmptyView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi khi đọc danh sách chặn: ", error.toException());
                Toast.makeText(BlacklistActivity.this, "Lỗi khi tải danh sách chặn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToBlacklist(String phoneNumber) {
        String normalizedNumber = phoneNumber.replaceAll("[^0-9]", "");

        for (BlacklistedNumber number : blacklistedNumbers) {
            if (number.getPhoneNumber().equals(normalizedNumber)) {
                Toast.makeText(this, "Số điện thoại này đã có trong danh sách chặn", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String id = blacklistRef.push().getKey();
        if (id != null) {
            BlacklistedNumber newNumber = new BlacklistedNumber(id, normalizedNumber);
            blacklistRef.child(id).setValue(newNumber)
                    .addOnSuccessListener(aVoid -> {
                        etPhoneNumber.setText("");
                        Toast.makeText(BlacklistActivity.this, "Đã thêm vào danh sách chặn", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi thêm số vào danh sách chặn", e);
                        Toast.makeText(BlacklistActivity.this, "Lỗi khi thêm vào danh sách chặn", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void removeBlacklistedNumber(int position) {
        BlacklistedNumber number = blacklistedNumbers.get(position);
        blacklistRef.child(number.getId()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(BlacklistActivity.this, "Đã xóa khỏi danh sách chặn", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(BlacklistActivity.this, "Lỗi khi xóa khỏi danh sách chặn", Toast.LENGTH_SHORT).show());
    }

    private void updateEmptyView() {
        if (blacklistedNumbers.isEmpty()) {
            tvEmptyList.setVisibility(View.VISIBLE);
            rvBlacklist.setVisibility(View.GONE);
        } else {
            tvEmptyList.setVisibility(View.GONE);
            rvBlacklist.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}