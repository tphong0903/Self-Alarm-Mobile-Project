package hcmute.edu.vn.selfalarmproject;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {

    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;
    private int selectedDay = -1;
    private final Set<Integer> eventDays = new HashSet<>();

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String dayText = daysOfMonth.get(position);
        holder.dayOfMonth.setText(dayText);

        if (!dayText.isEmpty()) {
            int day = Integer.parseInt(dayText);

            if (day == selectedDay) {
                holder.dayOfMonth.setBackgroundResource(R.drawable.circle_selected); // Ngày đang chọn
                holder.dayOfMonth.setTextColor(Color.WHITE);
            } else if (eventDays.contains(day)) {
                holder.dayOfMonth.setBackgroundResource(R.drawable.circle_event); // Ngày có sự kiện
                holder.dayOfMonth.setTextColor(Color.WHITE);
            } else {
                holder.dayOfMonth.setBackgroundResource(R.drawable.circle_background); // Mặc định
                holder.dayOfMonth.setTextColor(Color.BLACK);
            }

            holder.dayOfMonth.setOnClickListener(v -> {
                selectedDay = day;
                notifyDataSetChanged(); // Cập nhật lại RecyclerView
                onItemListener.onItemClick(holder.getAdapterPosition(), dayText);
            });
        } else {
            holder.dayOfMonth.setBackgroundResource(R.drawable.circle_background);
            holder.dayOfMonth.setTextColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, String dayText);
    }
}
