package hcmute.edu.vn.selfalarmproject.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.selfalarmproject.R;

public class PhoneAdapter extends RecyclerView.Adapter<PhoneAdapter.ViewHolder> {
    private List<String[]> PhoneList;

    public PhoneAdapter(List<String[]> phoneList) {
        PhoneList = phoneList;
    }

    @NonNull
    @Override
    public PhoneAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhoneAdapter.ViewHolder holder, int position) {
        String[] phoneContact = PhoneList.get(position);
        String contactName = phoneContact[0];
        String contactPhone = phoneContact[1];

        holder.txtName.setText(contactName);
        holder.txtPhone.setText(contactPhone);

        holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contactPhone));
                v.getContext().startActivity(intent);
            });
    }

    @Override
    public int getItemCount() {
        return PhoneList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPhone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtPhone=itemView.findViewById(R.id.txtPhone);
        }
    }
}
