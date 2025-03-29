package hcmute.edu.vn.selfalarmproject.views;


import static hcmute.edu.vn.selfalarmproject.views.MainActivity.account;
import static hcmute.edu.vn.selfalarmproject.views.MainActivity.googleCalendarManager;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executors;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.adapters.EventAdapter;
import hcmute.edu.vn.selfalarmproject.models.TaskContentProvider;
import hcmute.edu.vn.selfalarmproject.models.TaskModel;

public class EventDetailActivity extends AppCompatActivity {
    private ArrayList<TaskModel> listEvent = new ArrayList<>();
    private EventAdapter eventAdapter;
    private ListView listView;
    private Integer dayText;
    private Integer monthText;
    private Integer yearText;
    private static final String TAG = "GoogleCalendarDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        dayText = getIntent().getIntExtra("dayText", -99);
        monthText = getIntent().getIntExtra("month", -99);
        yearText = getIntent().getIntExtra("year", -99);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        listView = findViewById(R.id.listview);
        eventAdapter = new EventAdapter(EventDetailActivity.this, listEvent);
        listView.setAdapter(eventAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showBottomDialog(listEvent.get(i));
            }
        });

        fetchCalendarEvents(monthText, yearText);
    }

    private void fetchCalendarEvents(int month, int year) {
        listEvent.clear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, dayText, 0, 0, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.set(year, month - 1, dayText, 23, 59, 59);
        long endOfDay = calendar.getTimeInMillis();

        if (account != null) {
            Executors.newCachedThreadPool().execute(() -> {
                try {
                    List<Event> events = googleCalendarManager.fetchCalendarEvents(dayText, month - 1, year);
                    Log.d("MainActivity", "Số sự kiện lấy được: " + events.size());
                    runOnUiThread(() -> {
                        for (Event event : events) {
                            Log.d("MainActivity", "Sự kiện: " + event.getSummary() + " - " + event.getStart().toString());
                            String id = event.getId();
                            String title = event.getSummary() != null ? event.getSummary() : "No Title";
                            String description = event.getDescription() != null ? event.getDescription() : "";

                            long startDateTime = 0;
                            long endDateTime = 0;

                            if (event.getStart() != null) {
                                if (event.getStart().getDateTime() != null) {
                                    startDateTime = event.getStart().getDateTime().getValue();
                                } else if (event.getStart().getDate() != null) {
                                    startDateTime = event.getStart().getDate().getValue();
                                }
                            }

                            if (event.getEnd() != null) {
                                if (event.getEnd().getDateTime() != null) {
                                    endDateTime = event.getEnd().getDateTime().getValue();
                                } else if (event.getEnd().getDate() != null) {
                                    endDateTime = event.getEnd().getDate().getValue();
                                }
                            }
                            listEvent.add(new TaskModel(id, title, description, startDateTime, endDateTime, 1));

                        }
                        eventAdapter.notifyDataSetChanged();
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi lấy sự kiện từ Google Calendar", e);
                }
            });
        } else {
            String selection = "startDateTime >= ? AND startDateTime < ?";
            String[] selectionArgs = {String.valueOf(startOfDay), String.valueOf(endOfDay)};

            Cursor cursor = getContentResolver().query(
                    TaskContentProvider.CONTENT_URI, null, selection, selectionArgs, "startDateTime ASC"
            );
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    long startDateTime = cursor.getLong(cursor.getColumnIndexOrThrow("startDateTime"));
                    long endDateTime = cursor.getLong(cursor.getColumnIndexOrThrow("endDateTime"));
                    listEvent.add(new TaskModel(String.valueOf(id), title, description, startDateTime, endDateTime));
                }
                cursor.close();
            }
            eventAdapter.notifyDataSetChanged();
        }
    }

    private void deleteEvent(String id, Dialog dialog) {
        if (account != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    googleCalendarManager.deleteEvent(id);
                    Thread.sleep(1000);
                    runOnUiThread(() -> {
                        Toast.makeText(EventDetailActivity.this, "Xóa sự kiện thành công", Toast.LENGTH_SHORT).show();
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        fetchCalendarEvents(monthText, yearText);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(EventDetailActivity.this, "Lỗi khi xóa sự kiện", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            Uri taskUri = Uri.withAppendedPath(TaskContentProvider.CONTENT_URI, id);
            getContentResolver().delete(taskUri, null, null);
            listEvent.removeIf(v -> v.getId().equals(id));
            eventAdapter.notifyDataSetChanged();
        }
    }

    private void editEvent(String id, Dialog dialog, String title, String description, String startTime, String endTime, String date) throws ParseException {
        if (account != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    googleCalendarManager.editEvent(id, title, description, startTime, endTime, date);
                    Thread.sleep(1000);
                    runOnUiThread(() -> {
                        Toast.makeText(EventDetailActivity.this, "Cập nhật sự kiện thành công", Toast.LENGTH_SHORT).show();
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        fetchCalendarEvents(monthText, yearText);
                    });
                } catch (Exception e) {

                    runOnUiThread(() -> {
                        Log.d("Event", e.getMessage());
                        Toast.makeText(EventDetailActivity.this, "Lỗi khi cập nhật sự kiện", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            dateTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            ContentValues values = new ContentValues();
            values.put("title", title);
            values.put("description", description);
            Date startDateTime = dateTimeFormat.parse(date + " " + startTime);
            Date endDateTime = dateTimeFormat.parse(date + " " + endTime);

            values.put("startDateTime", startDateTime.getTime());
            values.put("endDateTime", endDateTime.getTime());
            Uri taskUri = Uri.withAppendedPath(TaskContentProvider.CONTENT_URI, id);
            int rowsUpdated = getContentResolver().update(taskUri, values, null, null);
            if (rowsUpdated > 0) {
//            scheduleTaskReminder(this, title, description, dateTime.getTime());
                fetchCalendarEvents(monthText, yearText);
            }
        }

    }

    private void showBottomDialog(TaskModel eventModel) {

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

        titleCalendar.setText(eventModel.getTitle());
        descriptionCalendar.setText(eventModel.getDescription());

        deleteBtn.setOnClickListener(v -> {
            deleteEvent(eventModel.getId(), dialog);
            dialog.dismiss();
        });
        editBtn.setOnClickListener(v -> {
            btnSaveTime.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
            dateTextView.setEnabled(true);
            titleCalendar.setFocusableInTouchMode(true);
            titleCalendar.setFocusable(true);
            titleCalendar.requestFocus();
            titleCalendar.setCursorVisible(true);

            descriptionCalendar.setFocusableInTouchMode(true);
            descriptionCalendar.setFocusable(true);
            descriptionCalendar.setCursorVisible(true);
            startTimeTextView.setEnabled(true);
            endTimeTextView.setEnabled(true);
        });
        btnSaveTime.setOnClickListener(v -> {
            try {
                editEvent(eventModel.getId(), dialog, titleCalendar.getText().toString(), descriptionCalendar.getText().toString(), startTimeTextView.getText().toString(), endTimeTextView.getText().toString(), dateTextView.getText().toString());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            dialog.dismiss();
        });
        dateTextView.setText(formatEventDate(eventModel.getStartDateTime()));
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
        startTimeTextView.setText(formatEventTime(eventModel.getStartDateTime()));
        endTimeTextView.setText(formatEventTime(eventModel.getEndDateTime()));

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        dialog.show();

    }

    private String formatEventDate(long date) {
        try {
            Date eventStartDateTime = new Date(date);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(eventStartDateTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private String formatEventTime(long date) {
        try {
            Date eventStartDateTime = new Date(date);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return timeFormat.format(eventStartDateTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }
}