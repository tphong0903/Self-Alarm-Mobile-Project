package hcmute.edu.vn.selfalarmproject.views;


import static hcmute.edu.vn.selfalarmproject.views.MainActivity.account;
import static hcmute.edu.vn.selfalarmproject.views.MainActivity.googleCalendarManager;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.services.calendar.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executors;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.adapters.CalendarAdapter;
import hcmute.edu.vn.selfalarmproject.models.TaskContentProvider;
import hcmute.edu.vn.selfalarmproject.models.TaskModel;
import hcmute.edu.vn.selfalarmproject.service.TaskReminderReceiver;

public class HomeFragment extends Fragment implements CalendarAdapter.OnItemListener {
    private AutoCompleteTextView setDateCalender;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private View rootView;
    private List<TaskModel> listEvent = new ArrayList<>();
    private FloatingActionButton floatingActionButton;
    private CalendarAdapter calendarAdapter;
    private int selectedMonth;

    private int selectedYear;

    private static final String TAG = "GoogleCalendarDebug";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        updateUI();
        return rootView;
    }

    private void updateUI() {
        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();
        loadEvents();
    }

    private void initWidgets() {
        calendarRecyclerView = rootView.findViewById(R.id.calendarRecyclerView);
        setDateCalender = rootView.findViewById(R.id.setDateCalender);
        setDateCalender.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(setDateCalender.getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, day);
                        this.selectedMonth = selectedMonth + 1;
                        this.selectedYear = selectedYear;
                        setMonthView();
                        setDateCalender.setText(monthYearFromDate(selectedDate));
                        fetchCalendarEvents(selectedMonth + 1, selectedYear);
                    }, year, month, day);

            datePickerDialog.show();
        });
        floatingActionButton = rootView.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> {
            showBottomDialog();
        });
    }

    public void loadEvents() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);
        fetchCalendarEvents(currentMonth, currentYear);
    }

    private void setMonthView() {
        setDateCalender.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);
        calendarAdapter = new CalendarAdapter(daysInMonth, this, listEvent, selectedDate.getMonthValue(), selectedDate.getYear());
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(getActivity().getApplicationContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);

    }

    private void fetchCalendarEvents(int month, int year) {
        listEvent.clear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        long startOfMonth = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, 1);
        long endOfMonth = calendar.getTimeInMillis();

        if (account != null) {
            Executors.newCachedThreadPool().execute(() -> {
                try {
                    List<Event> events = googleCalendarManager.fetchCalendarEvents(-99, month - 1, year);
                    Log.d("MainActivity", "Số sự kiện lấy được: " + events.size());
                    requireActivity().runOnUiThread(() -> {
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
                            listEvent.add(new TaskModel(id, title, description, startDateTime, endDateTime));
                        }
                        calendarAdapter.notifyDataSetChanged();
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi lấy sự kiện từ Google Calendar", e);
                }
            });
        } else {
            String selection = "startDateTime >= ? AND startDateTime < ?";
            String[] selectionArgs = {String.valueOf(startOfMonth), String.valueOf(endOfMonth)};

            Cursor cursor = requireContext().getContentResolver().query(
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
            calendarAdapter.notifyDataSetChanged();
        }
    }


    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    @Override
    public void onItemClick(int position, String dayText, int month, int year) {
        if (!dayText.isEmpty()) {
            Intent intent = new Intent(this.getContext(), EventDetailActivity.class);
            intent.putExtra("dayText", Integer.parseInt(dayText));
            intent.putExtra("month", month);
            intent.putExtra("year", year);
            startActivity(intent);
        }
    }


    private void showBottomDialog() {

        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_dialog);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);
        AutoCompleteTextView dateTextView = dialog.findViewById(R.id.setDateCalender);
        EditText titleCalendar = dialog.findViewById(R.id.editTextTitleCalender);
        EditText descriptionCalendar = dialog.findViewById(R.id.editTextDescriptionCalender);

        Calendar calendar1 = Calendar.getInstance();
        int year1 = calendar1.get(Calendar.YEAR);
        int month1 = calendar1.get(Calendar.MONTH);
        int day1 = calendar1.get(Calendar.DAY_OF_MONTH);
        int hour1 = calendar1.get(Calendar.HOUR_OF_DAY);
        String selectedDate1 = String.format(Locale.getDefault(), "%02d/%02d/%04d", day1, month1 + 1, year1);

        dateTextView.setText(selectedDate1);
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

        AutoCompleteTextView startTimeTextView = dialog.findViewById(R.id.setStartTimeCalender);

        AutoCompleteTextView endTimeTextView = dialog.findViewById(R.id.setEndTimeCalender);

        String selectedTime1 = String.format(Locale.getDefault(), "%02d:%02d", hour1 + 1, 0);
        startTimeTextView.setText(selectedTime1);
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

        selectedTime1 = String.format(Locale.getDefault(), "%02d:%02d", hour1 + 2, 0);
        endTimeTextView.setText(selectedTime1);
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

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Button addCalendar = dialog.findViewById(R.id.btnSetTime);
        addCalendar.setOnClickListener(v -> {
            try {
                if (account != null) {
                    addEventGGCalendar(dialog, titleCalendar.getText().toString(), descriptionCalendar.getText().toString(), startTimeTextView.getText().toString(), endTimeTextView.getText().toString(), dateTextView.getText().toString());
                } else {
                    addEvent(titleCalendar.getText().toString(), descriptionCalendar.getText().toString(), startTimeTextView.getText().toString(), endTimeTextView.getText().toString(), dateTextView.getText().toString());
                }
                dialog.dismiss();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();

    }

    private void addEventGGCalendar(Dialog dialog, String title, String description, String startTime, String endTime, String date) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                googleCalendarManager.addEvent(title, description, startTime, endTime, date);
                Thread.sleep(1000);
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Thêm sự kiện thành công", Toast.LENGTH_SHORT).show();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    loadEvents();
                });
            } catch (Exception e) {

                getActivity().runOnUiThread(() -> {
                    Log.d("Event", e.getMessage());
                    Toast.makeText(getActivity(), "Lỗi khi thêm sự kiện", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void addEvent(String title, String description, String startTime, String endTime, String date) throws ParseException {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("description", description);
        Date startDateTime = dateTimeFormat.parse(date + " " + startTime);
        Date endDateTime = dateTimeFormat.parse(date + " " + endTime);

        values.put("startDateTime", startDateTime.getTime());
        values.put("endDateTime", endDateTime.getTime());
        Uri newTaskUri = requireContext().getContentResolver().insert(TaskContentProvider.CONTENT_URI, values);
        if (newTaskUri != null) {
            scheduleTaskReminder(getActivity(), title, description, startDateTime.getTime());
            Toast.makeText(getActivity(), "Đã thêm sự kiện", Toast.LENGTH_SHORT).show();
            loadEvents();
        }
    }

    public static void scheduleTaskReminder(Context context, String title, String description, long dueTimeMillis) {
        long reminderTimeMillis = dueTimeMillis - (30 * 60 * 1000);

        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra("task_title", title);
        intent.putExtra("task_description", description);
        int notificationId = (int) dueTimeMillis;
        intent.putExtra("notificationId", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);
        }
    }
}
