package hcmute.edu.vn.selfalarmproject.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.models.BlacklistedNumber;

public class BlacklistAdapter extends RecyclerView.Adapter<BlacklistAdapter.BlacklistViewHolder> {

    private List<BlacklistedNumber> blacklistedNumbers;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public BlacklistAdapter(List<BlacklistedNumber> blacklistedNumbers, OnItemClickListener listener) {
        this.blacklistedNumbers = blacklistedNumbers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BlacklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_black_list, parent, false);
        return new BlacklistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlacklistViewHolder holder, int position) {
        BlacklistedNumber number = blacklistedNumbers.get(position);
        holder.tvBlockedNumber.setText(number.getPhoneNumber());

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return blacklistedNumbers.size();
    }

    static class BlacklistViewHolder extends RecyclerView.ViewHolder {
        TextView tvBlockedNumber;
        ImageButton btnRemove;

        public BlacklistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBlockedNumber = itemView.findViewById(R.id.tvBlockedNumber);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}