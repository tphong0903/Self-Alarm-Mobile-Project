package hcmute.edu.vn.selfalarmproject.models;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import hcmute.edu.vn.selfalarmproject.R;

public class CalendarViewHolder extends RecyclerView.ViewHolder {
    public final TextView dayOfMonth;

    public CalendarViewHolder(@NonNull View itemView) {
        super(itemView);
        dayOfMonth = itemView.findViewById(R.id.cellDayText);
    }
}
