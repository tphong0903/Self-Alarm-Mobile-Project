package hcmute.edu.vn.selfalarmproject;

import static hcmute.edu.vn.selfalarmproject.MainActivity.calendarService;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.services.calendar.model.Event;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HomeFragment extends Fragment implements CalendarAdapter.OnItemListener {
    private AutoCompleteTextView setDateCalender;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private View rootView;
    private List<Event> listEvent = new ArrayList<>();


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
                Future<List<Event>> futureEvents = calendarService.fetchCalendarEvents(-99, Month, Year);
                List<Event> events = futureEvents.get();
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

    public void loadEvents() {
        fetchCalendarEvents(0, 0);
    }
}
