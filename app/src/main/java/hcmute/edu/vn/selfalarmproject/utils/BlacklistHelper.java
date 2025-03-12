package hcmute.edu.vn.selfalarmproject.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import hcmute.edu.vn.selfalarmproject.models.BlacklistedNumber;

public class BlacklistHelper {
    private static final String TAG = "BlacklistHelper";

    public interface BlacklistCallback {
        void onResult(boolean isBlacklisted);
    }

    public static void isNumberBlacklisted(Context context, String phoneNumber, BlacklistCallback callback) {
        String normalizedNumber = phoneNumber.replaceAll("[^0-9]", "");
        String googleUid = SharedPreferencesHelper.getGoogleUid(context);

        if (googleUid == null) {
            callback.onResult(false);
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://week6-8ecb2-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference blacklistRef = database.getReference(googleUid + "blacklist").child("blacklist");

        blacklistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isBlacklisted = false;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    BlacklistedNumber number = dataSnapshot.getValue(BlacklistedNumber.class);
                    if (number != null && number.getPhoneNumber().equals(normalizedNumber)) {
                        isBlacklisted = true;
                        break;
                    }
                }
                callback.onResult(isBlacklisted);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error", error.toException());
                callback.onResult(false);
            }
        });
    }
}
