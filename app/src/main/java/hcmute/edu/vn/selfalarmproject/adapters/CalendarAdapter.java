package hcmute.edu.vn.selfalarmproject.adapters;

import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.models.CalendarViewHolder;
import hcmute.edu.vn.selfalarmproject.models.TaskModel;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {

    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;
    private final List<TaskModel> listEvent;
    private int selectedDay = -1;
    private final int month;
    private final int year;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener, List<TaskModel> listEvent, int month, int year) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.listEvent = listEvent;
        this.month = month;
        this.year = year;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_cell, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String dayText = daysOfMonth.get(position);
        holder.dayOfMonth.setText(dayText);

        int nightModeFlags = holder.itemView.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES);

        if (!dayText.isEmpty()) {
            int day = Integer.parseInt(dayText);

            if (hasEventOnDay(day)) {
                holder.dayOfMonth.setBackgroundResource(R.drawable.circle_event);
                holder.dayOfMonth.setTextColor(Color.WHITE);
            } else {
                holder.dayOfMonth.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
            }

            if (day == selectedDay) {
                holder.dayOfMonth.setBackgroundResource(R.drawable.circle_selected);
                holder.dayOfMonth.setTextColor(Color.WHITE);
            }

            holder.itemView.setOnClickListener(v -> {
                int prevSelected = selectedDay;
                selectedDay = day;

                // Chỉ cập nhật các item cần thiết thay vì toàn bộ RecyclerView
                notifyItemChanged(prevSelected - 1);
                notifyItemChanged(selectedDay - 1);

                onItemListener.onItemClick(holder.getAdapterPosition(), dayText, month, year);
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
        void onItemClick(int position, String dayText, int month, int year);
    }

    private boolean hasEventOnDay(int day) {
        for (TaskModel event : listEvent) {
            if (event.getStartDateTime() != 0) {
                LocalDateTime eventDate = Instant.ofEpochMilli(event.getStartDateTime())
                        .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                        .toLocalDateTime();
                if (eventDate.getDayOfMonth() == day && eventDate.getMonthValue() == month && eventDate.getYear() == year) {
                    return true;
                }
            }
        }
        return false;
    }
}
