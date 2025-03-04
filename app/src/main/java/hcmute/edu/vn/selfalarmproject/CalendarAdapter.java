package hcmute.edu.vn.selfalarmproject;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {

    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;
    private List<Event> listEvent;
    private int selectedDay = -1;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener, List<Event> listEvent) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.listEvent = listEvent;
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
            if (hasEventOnDay(day)) {
                holder.dayOfMonth.setBackgroundResource(R.drawable.circle_event);
                holder.dayOfMonth.setTextColor(Color.WHITE);
            } else {
                holder.dayOfMonth.setBackgroundResource(R.drawable.circle_background);
            }
            // Nếu người dùng chọn ngày, thay đổi màu sắc
            if (day == selectedDay) {
                holder.dayOfMonth.setBackgroundResource(R.drawable.circle_selected);
                holder.dayOfMonth.setTextColor(Color.WHITE);
            }

            holder.dayOfMonth.setOnClickListener(v -> {
                selectedDay = day;
                notifyDataSetChanged();
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

    private boolean hasEventOnDay(int day) {
        for (Event event : listEvent) {
            if (event.getStart().getDateTime() != null) {
                int eventDay = new Date(event.getStart().getDateTime().getValue()).getDate();
                if (eventDay == day) {
                    return true;
                }
            } else if (event.getStart().getDate() != null) {
                // Nếu sự kiện chỉ có Date (cả ngày)
                String eventDateStr = event.getStart().getDate().toString();
                int eventDay = Integer.parseInt(eventDateStr.split("-")[2]); // Lấy ngày từ chuỗi yyyy-MM-dd
                if (eventDay == day) {
                    return true;
                }
            }
        }
        return false;
    }
}
