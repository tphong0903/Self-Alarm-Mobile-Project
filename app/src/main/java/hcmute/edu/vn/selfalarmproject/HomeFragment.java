package hcmute.edu.vn.selfalarmproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements CalendarAdapter.OnItemListener {
    private AutoCompleteTextView setDateCalender;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private View rootView;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient googleSignInClient;
    private List<Event> listEvent = new ArrayList<>();
    private String heheh = null;
    private static final String TAG = "GoogleCalendarDebug";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR_READONLY))
                .requestServerAuthCode("634522600018-hljitpeajl1d02938trv731vqfab9th4.apps.googleusercontent.com", true)
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        checkExistingSignIn();
        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();

        return rootView;
    }

    private void checkExistingSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account == null) {
            // Chưa đăng nhập, tự động chuyển hướng
            triggerGoogleSignIn();
        } else {
            // Đã đăng nhập, tiếp tục hiển thị giao diện
            updateUI(account);
        }
        fetchCalendarEvents(account, null);
    }

    private void fetchCalendarEvents(GoogleSignInAccount account, DateTime time) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        requireContext(), Collections.singleton(CalendarScopes.CALENDAR_READONLY));
                credential.setSelectedAccount(account.getAccount());

                com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential)
                        .setApplicationName("SelfAlarmProject")
                        .build();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                DateTime firstDayOfMonth = new DateTime(calendar.getTime());

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                DateTime lastDayOfMonth = new DateTime(calendar.getTime());

                Boolean checkTime = time == null ? false : true;
                Events events = service.events().list("primary")
                        .setTimeMin(firstDayOfMonth)
                        .setTimeMax(lastDayOfMonth)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();

                List<Event> items = events.getItems();

                if (!items.isEmpty()) {
                    listEvent.addAll(items);
                }
                requireActivity().runOnUiThread(this::setMonthView);

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy sự kiện", e);
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Lỗi khi lấy sự kiện", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void triggerGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                updateUI(account);
            } else {
                handleSignInFailure();
            }
        } catch (ApiException e) {
            handleSignInFailure();
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        // Xử lý khi đăng nhập thành công
        String email = account.getEmail();
        String displayName = account.getDisplayName();
        Toast.makeText(getContext(), "Welcome " + displayName, Toast.LENGTH_SHORT).show();

        // Hiển thị giao diện chính
        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();
    }

    private void handleSignInFailure() {
        // Xử lý khi đăng nhập thất bại
        Toast.makeText(getContext(), "Bạn phải đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
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

                        setMonthView();
                    }, year, month, day);

            datePickerDialog.show();
        });
    }

    private void setMonthView() {
        setDateCalender.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this, listEvent);
        calendarAdapter.setEvents(listEvent);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
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
        if (!dayText.equals("")) {
            String message = "Selected Date " + dayText + " " + setDateCalender.getText();
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
    }
}
