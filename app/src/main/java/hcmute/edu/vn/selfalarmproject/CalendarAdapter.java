package hcmute.edu.vn.selfalarmproject;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.Calendar;
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

    public void setEvents(List<Event> newEvents) {
        this.listEvent = newEvents;
        notifyDataSetChanged(); // Cập nhật lại RecyclerView
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
            } else if (hasEventOnDay(day)) {
                holder.dayOfMonth.setBackgroundResource(R.drawable.circle_event); // Ngày có sự kiện
                holder.dayOfMonth.setTextColor(Color.WHITE);
            } else {
                holder.dayOfMonth.setBackgroundResource(R.drawable.circle_background); // Mặc định
                holder.dayOfMonth.setTextColor(Color.BLACK);
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
                // Nếu sự kiện có DateTime (giờ cụ thể)
                Calendar eventCalendar = Calendar.getInstance();
                eventCalendar.setTimeInMillis(event.getStart().getDateTime().getValue());
                int eventDay = eventCalendar.get(Calendar.DAY_OF_MONTH);
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
