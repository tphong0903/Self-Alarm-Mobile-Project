package hcmute.edu.vn.selfalarmproject.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hcmute.edu.vn.selfalarmproject.models.Message;
import hcmute.edu.vn.selfalarmproject.utils.SharedPreferencesHelper;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        String format = bundle.getString("format");
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);

                        String sender = smsMessage.getDisplayOriginatingAddress();
                        String messageBody = smsMessage.getMessageBody();
                        long timestamp = smsMessage.getTimestampMillis();

                        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));

                        Log.d(TAG, "Tin nhắn mới từ: " + sender + ", Nội dung: " + messageBody);
                        Toast.makeText(context, "Tin nhắn từ " + sender + ": " + messageBody, Toast.LENGTH_LONG).show();

                        Message newMessage = new Message(sender, sender, "Tôi", messageBody, false, time);

                        saveMessageToFirebase(newMessage, context);

                        Intent updateUIIntent = new Intent("hcmute.edu.vn.selfalarmproject.NEW_SMS");
                        updateUIIntent.putExtra("sender", sender);
                        updateUIIntent.putExtra("message", messageBody);
                        updateUIIntent.putExtra("time", time);
                        context.sendBroadcast(updateUIIntent);
                    }
                }
            }
        }
    }

    private void saveMessageToFirebase(Message message, Context context) {
        String googleUid = SharedPreferencesHelper.getGoogleUid(context);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://week6-8ecb2-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference messagesRef = database.getReference(googleUid);

        messagesRef.push().setValue(message)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Lưu tin nhắn thành công!"))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi khi lưu tin nhắn", e));
    }
}
