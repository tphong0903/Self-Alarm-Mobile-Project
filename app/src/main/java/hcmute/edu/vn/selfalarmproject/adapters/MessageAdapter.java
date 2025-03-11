package hcmute.edu.vn.selfalarmproject.adapters;

import static android.content.ContentValues.TAG;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.models.Message;
import hcmute.edu.vn.selfalarmproject.utils.SharedPreferencesHelper;
import hcmute.edu.vn.selfalarmproject.views.ChatActivity;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList;
    private Context context;
    private String googleUid;


    public static interface OnMessageClickListener {
        void onMessageClick(String messageId);
    }

    private OnMessageClickListener listener;


    public MessageAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        Log.d("MessageAdapter", "onBindViewHolder: " + message.getId());
        String sender = message.getId();
        String contactName = getContactName(context, sender);
        googleUid = SharedPreferencesHelper.getGoogleUid(context);
        if (contactName != null) {
            holder.tvSender.setText(contactName);
        } else {
            holder.tvSender.setText(sender);
        }
        holder.tvMessageContent.setText(message.getContent());
        holder.tvTimestamp.setText(message.getTime());

        if (message.isRead()) {
            holder.tvSender.setTextColor(Color.GRAY);
            holder.tvMessageContent.setTextColor(Color.GRAY);
        } else {
            holder.tvSender.setTextColor(Color.BLACK);
            holder.tvMessageContent.setTextColor(Color.BLACK);
        }


        holder.itemView.setOnClickListener(v -> {
            if (!message.isRead()) {
                message.setRead(true);
                updateMessagesById(message.getId());
                notifyItemChanged(position);
                Bundle bundle = new Bundle();
                bundle.putString("messageId", message.getId());

                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("messageId", message.getId());
                context.startActivity(intent);
            } else {
                notifyItemChanged(position);
                Bundle bundle = new Bundle();
                bundle.putString("messageId", message.getId());

                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("messageId", message.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvMessageContent, tvTimestamp;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }


    public void removeMessage(int position) {
        Message message = messageList.get(position);
        deleteMessagesById(message.getId());

        messageList.remove(position);
        notifyItemRemoved(position);
    }

    private String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = cr.query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String contactNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        .replaceAll("[^0-9]", "");

                String normalizedInput = phoneNumber.replaceAll("[^0-9]", "");

                if (contactNumber.equals(normalizedInput)) {
                    cursor.close();
                    return contactName;
                }
            }
            cursor.close();
        }
        return null;
    }


    private void deleteMessagesById(String messageId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://week6-8ecb2-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference messagesRef = database.getReference(googleUid);

        messagesRef.orderByChild("id").equalTo(messageId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    messageSnapshot.getRef().removeValue()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Đã xóa tin nhắn có ID: " + messageId))
                            .addOnFailureListener(e -> Log.e(TAG, "❌ Lỗi khi xóa tin nhắn: " + e.getMessage(), e));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    private void updateMessagesById(String messageId) {

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://week6-8ecb2-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference messagesRef = database.getReference(googleUid);

        messagesRef.orderByChild("id").equalTo(messageId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    messageSnapshot.getRef().child("read").setValue(true)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Đã cập nhật tin nhắn có ID: " + messageId))
                            .addOnFailureListener(e -> Log.e(TAG, "❌ Lỗi khi cập nhật tin nhắn: " + e.getMessage(), e));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }


}
