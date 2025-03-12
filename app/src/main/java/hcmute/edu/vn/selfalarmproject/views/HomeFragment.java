package hcmute.edu.vn.selfalarmproject.views;


import static hcmute.edu.vn.selfalarmproject.views.MainActivity.googleCalendarManager;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.api.services.calendar.model.Event;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import hcmute.edu.vn.selfalarmproject.adapters.CalendarAdapter;
import hcmute.edu.vn.selfalarmproject.R;

public class HomeFragment extends Fragment implements CalendarAdapter.OnItemListener {
    private AutoCompleteTextView setDateCalender;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private View rootView;
    private List<Event> listEvent = new ArrayList<>();
    private FloatingActionButton floatingActionButton;

    private static final String TAG = "GoogleCalendarDebug";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        updateUI();
        return rootView;
    }


    private void fetchCalendarEvents(int Month, int Year) {
        Executors.newCachedThreadPool().execute(() -> {
            try {
                List<Event> events = googleCalendarManager.fetchCalendarEvents(-99, Month, Year);
                requireActivity().runOnUiThread(() -> {
                    listEvent.clear();
                    listEvent.addAll(events);
                    setMonthView();
                });


            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy sự kiện", e);

            }
        });
    }


    private void updateUI() {
        initWidgets();
        selectedDate = LocalDate.now();
        loadEvents();
        setMonthView();
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

                        setDateCalender.setText(monthYearFromDate(selectedDate));
                        fetchCalendarEvents(selectedMonth, selectedYear);
                    }, year, month, day);

            datePickerDialog.show();
        });
        floatingActionButton = rootView.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> {
            showBottomDialog();
        });
    }

    private void setMonthView() {
        setDateCalender.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);
        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this, listEvent);
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(getActivity().getApplicationContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);

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
    public void onItemClick(int position, String dayText) {
        if (!dayText.isEmpty()) {
            Intent intent = new Intent(this.getContext(), EventDetailActivity.class);
            intent.putExtra("dayText", Integer.parseInt(dayText));
            startActivity(intent);
        }
    }
    private void addEvent(Dialog dialog, String title, String description, String startTime, String endTime, String date) {
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
    public void loadEvents() {
        fetchCalendarEvents(0, 0);
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
        int minute1 = calendar1.get(Calendar.MINUTE);
        String selectedDate1 = String.format(Locale.getDefault(), "%02d/%02d/%04d", day1, month1 + 1, year1);
        dateTextView.setText(selectedDate1);
        TextInputLayout dateInputLayout = dialog.findViewById(R.id.dateCalender);
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
            addEvent(dialog, titleCalendar.getText().toString(), descriptionCalendar.getText().toString(), startTimeTextView.getText().toString(), endTimeTextView.getText().toString(), dateTextView.getText().toString());
        });
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();

    }
}
