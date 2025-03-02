package hcmute.edu.vn.selfalarmproject;

import static hcmute.edu.vn.selfalarmproject.HomeFragment.service;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executors;

import mobel.EventModel;

public class EventDetailActivity extends AppCompatActivity {
    private ArrayList<EventModel> listEvent;
    private EventAdapter eventAdapter;
    private ListView listView;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        listEvent = (ArrayList<EventModel>) getIntent().getSerializableExtra("listEvent");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hiển thị nút quay lại (nếu cần)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        listView = findViewById(R.id.listview);
        if (listEvent != null) {
            eventAdapter = new EventAdapter(this, listEvent);
            listView.setAdapter(eventAdapter);
            listView.setClickable(true);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showBottomDialog(listEvent.get(i));
            }
        });
    }

    private void showBottomDialog(EventModel eventModel) {

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.detail_event_dialog);
        AutoCompleteTextView startTimeTextView = dialog.findViewById(R.id.setStartTimeCalender);
        AutoCompleteTextView endTimeTextView = dialog.findViewById(R.id.setEndTimeCalender);
        LinearLayout linearLayout = dialog.findViewById(R.id.layoutTime);
        EditText titleCalendar = dialog.findViewById(R.id.editTextTitleCalender);
        EditText descriptionCalendar = dialog.findViewById(R.id.editTextDescriptionCalender);
        ImageView deleteBtn = dialog.findViewById(R.id.deleteButton);
        ImageView editBtn = dialog.findViewById(R.id.editButton);
        Button btnSaveTime = dialog.findViewById(R.id.btnSaveTime);
        AutoCompleteTextView dateTextView = dialog.findViewById(R.id.setDateCalender);

        titleCalendar.setText(eventModel.getSummary());

        descriptionCalendar.setText(eventModel.getDescription());

        deleteBtn.setOnClickListener(v -> {
            deleteEvent(eventModel.getID(), dialog);
        });
        editBtn.setOnClickListener(v -> {
            btnSaveTime.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
            dateTextView.setEnabled(true);
            titleCalendar.setFocusableInTouchMode(true);
            titleCalendar.setFocusable(true);
            titleCalendar.requestFocus(); // Đảm bảo nhận focus
            titleCalendar.setCursorVisible(true);

            descriptionCalendar.setFocusableInTouchMode(true);
            descriptionCalendar.setFocusable(true);
            descriptionCalendar.setCursorVisible(true);
            startTimeTextView.setEnabled(true);
            endTimeTextView.setEnabled(true);
        });
        btnSaveTime.setOnClickListener(v -> {
            editEvent(eventModel.getID(), dialog, titleCalendar.getText().toString(), descriptionCalendar.getText().toString(), startTimeTextView.getText().toString(), endTimeTextView.getText().toString(), dateTextView.getText().toString());
        });
        EventDateTime eventStartDateTime = eventModel.getStartTimeAsEventDateTime();
        if (eventStartDateTime != null) {
            String formattedDate = formatEventDate(eventStartDateTime);
            dateTextView.setText(formattedDate);
            Log.d("EventAdapter", "Event date: " + formattedDate);
        }
        dateTextView.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(dialog.getContext(), (view, selectedYear, selectedMonth, selectedDay) -> {

                String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);

                dateTextView.setText(selectedDate);
            }, year, month, day);

            datePickerDialog.show();
        });
        startTimeTextView.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(dialog.getContext(), (view, selectedHour, selectedMinute) -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                startTimeTextView.setText(selectedTime);
            }, hour, minute, true);

            timePickerDialog.show();
        });
        endTimeTextView.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(dialog.getContext(), (view, selectedHour, selectedMinute) -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                endTimeTextView.setText(selectedTime);
            }, hour, minute, true);

            timePickerDialog.show();
        });
        if (eventModel.getTypeDateTime() != 1) {
            linearLayout.setVisibility(View.VISIBLE);
            startTimeTextView.setText(formatEventTime(eventStartDateTime));
            EventDateTime eventEndDateTime = eventModel.getEndTimeAsEventDateTime();
            endTimeTextView.setText(formatEventTime(eventEndDateTime));

        } else {
            startTimeTextView.setText("N/A");
            endTimeTextView.setText("N/A");
        }


        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        dialog.show();

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

    private String formatEventTime(EventDateTime eventDateTime) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        try {
            if (eventDateTime.getDateTime() != null) {
                return timeFormat.format(new Date(eventDateTime.getDateTime().getValue()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private void deleteEvent(String id, Dialog dialog) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                service.events().delete("primary", id).execute();

                runOnUiThread(() -> {
                    Toast.makeText(EventDetailActivity.this, "Xóa sự kiện thành công", Toast.LENGTH_SHORT).show();
                    listEvent.removeIf(event -> event.getID().equals(id));
                    eventAdapter.notifyDataSetChanged();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(EventDetailActivity.this, "Lỗi khi xóa sự kiện", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void editEvent(String id, Dialog dialog, String title, String description, String startTime, String endTime, String date) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Boolean changeDate;
                Event event = service.events().get("primary", id).execute();
                event.setSummary(title);
                event.setDescription(description);
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                Date parsedDate = inputFormat.parse(date);
                String formattedDate = outputFormat.format(parsedDate);
                if (!startTime.equals("N/A") && !endTime.equals("N/A")) {
                    if (!event.getStart().getDateTime().toString().contains(formattedDate))
                        changeDate = true;
                    else
                        changeDate = false;
                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

                    Date startDateTime = dateTimeFormat.parse(formattedDate + " " + startTime);
                    Date endDateTime = dateTimeFormat.parse(formattedDate + " " + endTime);

                    EventDateTime startEventDateTime = new EventDateTime()
                            .setDateTime(new com.google.api.client.util.DateTime(startDateTime))
                            .setTimeZone("UTC");

                    EventDateTime endEventDateTime = new EventDateTime()
                            .setDateTime(new com.google.api.client.util.DateTime(endDateTime))
                            .setTimeZone("UTC");

                    event.setStart(startEventDateTime);
                    event.setEnd(endEventDateTime);
                } else {
                    EventDateTime startEventDateTime = new EventDateTime()
                            .setDate(new com.google.api.client.util.DateTime(formattedDate))
                            .setTimeZone("UTC");
                    changeDate = true;
                    event.setStart(startEventDateTime);
                    event.setEnd(startEventDateTime);
                }
                service.events().update("primary", id, event).execute();
                runOnUiThread(() -> {
                    Toast.makeText(EventDetailActivity.this, "Cập nhật sự kiện thành công", Toast.LENGTH_SHORT).show();
                    if (changeDate)
                        listEvent.removeIf(event1 -> event1.getID().equals(id));
                    eventAdapter.notifyDataSetChanged();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                });
            } catch (IOException e) {

                runOnUiThread(() -> {
                    Log.d("Event", e.getMessage());
                    Toast.makeText(EventDetailActivity.this, "Lỗi khi cập nhật sự kiện", Toast.LENGTH_SHORT).show();
                });
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }
}