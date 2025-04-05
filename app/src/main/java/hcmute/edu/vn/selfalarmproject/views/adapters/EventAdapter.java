package hcmute.edu.vn.selfalarmproject.views.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.models.TaskModel;

public class EventAdapter extends ArrayAdapter<TaskModel> {

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public EventAdapter(Context context, ArrayList<TaskModel> eventList) {
        super(context, R.layout.list_event_item, eventList);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        TaskModel listData = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_event_item, parent, false);
        }
        TextView listName = view.findViewById(R.id.listName);
        TextView listTime = view.findViewById(R.id.listTime);
        listName.setText(listData.getTitle());
        Date eventDateTime = new Date(listData.getStartDateTime());
        String formattedDate = sdf.format(eventDateTime);

        listTime.setText(formattedDate);
        Log.d("EventAdapter", "Event date: " + formattedDate);

        return view;
    }

}
