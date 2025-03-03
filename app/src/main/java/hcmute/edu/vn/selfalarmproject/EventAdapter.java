package hcmute.edu.vn.selfalarmproject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EventAdapter extends ArrayAdapter<Event> {

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public EventAdapter(Context context, ArrayList<Event> eventList) {
        super(context, R.layout.list_event_item, eventList);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        Event listData = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_event_item, parent, false);
        }
        TextView listName = view.findViewById(R.id.listName);
        TextView listTime = view.findViewById(R.id.listTime);
        listName.setText(listData.getSummary());
        EventDateTime eventDateTime = listData.getStart();
        if (eventDateTime != null) {
            String formattedDate = formatEventDate(eventDateTime);
            listTime.setText(formattedDate);
            Log.d("EventAdapter", "Event date: " + formattedDate);
        } else {
            listTime.setText("N/A");
            Log.d("EventAdapter", "Event date is null");
        }
        return view;
    }

    private String formatEventDate(EventDateTime eventDateTime) {
        try {
            if (eventDateTime.getDate() != null) {
                return sdf.format(new Date(eventDateTime.getDate().getValue()));
            } else if (eventDateTime.getDateTime() != null) {
                return sdf.format(new Date(eventDateTime.getDateTime().getValue()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }

}
