package hcmute.edu.vn.selfalarmproject.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.models.Message;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Message> messageList;
    private Context context;

    public ChatAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatAdapter.ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sender, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_receiver, parent, false);
        }
        return new ChatAdapter.ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ChatViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.tvSender.setText(message.getSender());
        String sender = message.getSender();
        String contactName = getContactName(context, sender);

        if (contactName != null) {
            holder.tvSender.setText(contactName);
        } else {
            holder.tvSender.setText(sender);
        }
        holder.tvMessage.setText(message.getContent());
        String formattedTime = message.getTime().substring(0, 5);
        holder.tvTime.setText(formattedTime);

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvMessage, tvTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTimestamp);
        }
    }
    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        return message.getSender().equals("Tôi") ? 1 : 0;
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

}
