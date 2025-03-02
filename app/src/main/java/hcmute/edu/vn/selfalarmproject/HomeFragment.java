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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import mobel.EventModel;

public class HomeFragment extends Fragment implements CalendarAdapter.OnItemListener {
    private AutoCompleteTextView setDateCalender;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private View rootView;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient googleSignInClient;
    private List<Event> listEvent = new ArrayList<>();
    private GoogleSignInAccount account;
    private static HttpTransport transport;
    private static final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    public static com.google.api.services.calendar.Calendar service;
    private static GoogleAccountCredential credential;


    private static final String TAG = "GoogleCalendarDebug";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        try {
            transport = GoogleNetHttpTransport.newTrustedTransport();
            credential = GoogleAccountCredential.usingOAuth2(
                    requireContext(), Collections.singleton(CalendarScopes.CALENDAR_READONLY));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR_READONLY))
                .requestServerAuthCode("634522600018-hljitpeajl1d02938trv731vqfab9th4.apps.googleusercontent.com", true)
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        checkExistingSignIn();
        return rootView;
    }

    private void checkExistingSignIn() {
        account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account == null) {
            triggerGoogleSignIn();
        } else {
            updateUI(account);
        }
        fetchCalendarEvents(account, 0, 0);
    }

    private void fetchCalendarEvents(GoogleSignInAccount account, int Month, int Year) {
        Executors.newCachedThreadPool().execute(() -> {
            try {
                credential.setSelectedAccount(account.getAccount());

                service = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential)
                        .setApplicationName("SelfAlarmProject")
                        .build();

                Boolean checkTime = Month == 0 ? true : false;
                Calendar calendar = Calendar.getInstance();
                LocalDate selectedDate2 = LocalDate.now();
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.MONTH, checkTime ? selectedDate2.getMonthValue() - 1 : Month);
                calendar.set(Calendar.YEAR, checkTime ? selectedDate2.getYear() : Year);
                DateTime firstDayOfMonth = new DateTime(calendar.getTime());

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                DateTime lastDayOfMonth = new DateTime(calendar.getTime());
                Log.e(TAG, "thang " + Month);
                Log.e(TAG, "ngay dau tien " + firstDayOfMonth);
                Log.e(TAG, "ngay cuoi " + lastDayOfMonth);
                Events events = service.events().list("primary")
                        .setTimeMin(firstDayOfMonth)
                        .setTimeMax(lastDayOfMonth)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();

                List<Event> items = events.getItems();
                Log.e(TAG, "so luong event-1 " + items.size());
                if (!items.equals(listEvent)) {
                    Log.e(TAG, "so luong event-2 " + items.size());
                    listEvent.clear();
                    listEvent.addAll(items);
                    requireActivity().runOnUiThread(this::setMonthView);
                }

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy sự kiện", e);

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
        String displayName = account.getDisplayName();
        Toast.makeText(getContext(), "Welcome " + displayName, Toast.LENGTH_SHORT).show();
        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();
    }

    private void handleSignInFailure() {
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
                        fetchCalendarEvents(account, selectedMonth, selectedYear);
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
        if (Integer.parseInt(dayText) <= 9)
            dayText = "0" + dayText;
        if (!dayText.isEmpty()) {
            ArrayList<EventModel> eventModelList = new ArrayList<>();
            for (Event event : listEvent) {
                String eventDate;

                if (event.getStart().getDateTime() != null) {
                    eventDate = event.getStart().getDateTime().toStringRfc3339().substring(8, 10);
                } else {
                    eventDate = event.getStart().getDate().toString().substring(8, 10);
                }
                if (eventDate.equals(dayText)) {
                    EventModel a = new EventModel(
                            event.getId(),
                            event.getSummary(),
                            event.getDescription(),
                            event.getEventType(),
                            event.getStart(),
                            event.getEnd(),
                            event.getOriginalStartTime(),
                            event.getStart().getDate() != null ? 1 : 0
                    );
                    eventModelList.add(a);
                }
            }
            Intent intent = new Intent(this.getContext(), EventDetailActivity.class);
            intent.putParcelableArrayListExtra("listEvent", eventModelList);
            startActivity(intent);
        }
    }

    public void loadEvents() {
        fetchCalendarEvents(account, 0, 0);
    }
}
