package hcmute.edu.vn.selfalarmproject;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.Executors;

import hcmute.edu.vn.selfalarmproject.service.CalendarService;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton floatingActionButton;
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;

    NavigationView navigationView;


    private GoogleSignInAccount account;
    private Bundle savedInstanceState;
    private static HttpTransport transport;
    private static final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private GoogleSignInClient googleSignInClient;

    public static com.google.api.services.calendar.Calendar service;
    private static final int RC_SIGN_IN = 9001;

    private static GoogleAccountCredential credential;
    public static CalendarService calendarService = new CalendarService();
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("PhoneFragment", "Quyền danh bạ đã được cấp");
                } else {
                    Log.d("PhoneFragment", "Quyền danh bạ bị từ chối");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();

        this.savedInstanceState = savedInstanceState;
        try {
            transport = GoogleNetHttpTransport.newTrustedTransport();
            credential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Collections.singleton(CalendarScopes.CALENDAR));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR))
                .requestIdToken("634522600018-hljitpeajl1d02938trv731vqfab9th4.apps.googleusercontent.com")
                .build();

        googleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
        checkExistingSignIn();


    }

    private void checkExistingSignIn() {
        account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account == null) {
            triggerGoogleSignIn();
        } else {
            updateUI(savedInstanceState);
            credential.setSelectedAccount(account.getAccount());

            service = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential)
                    .setApplicationName("SelfAlarmProject")
                    .build();
        }
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
                updateUI(savedInstanceState);
            } else {
                handleSignInFailure();
            }
        } catch (ApiException e) {
            handleSignInFailure();
        }
    }

    private void handleSignInFailure() {
        Toast.makeText(getApplicationContext(), "Bạn phải đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
    }

    private void updateUI(Bundle savedInstanceState) {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        floatingActionButton = findViewById(R.id.fab);
        drawerLayout = findViewById(R.id.drawer_layout);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(v -> {

            int id = v.getItemId();

            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.nav_settings) {
                replaceFragment(new MusicFragment());
            } else if (id == R.id.nav_logout) {
                Toast.makeText(MainActivity.this, "Log out", Toast.LENGTH_SHORT).show();
                signOut();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.nav_home);
        }

        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.menu_music) {
                replaceFragment(new MusicFragment());
            } else if (itemId == R.id.menu_phone) {
                replaceFragment(new PhoneFragment());
            } else if (itemId == R.id.menu_message) {
                replaceFragment(new MessageFragment());

            }

            return true;
        });

        floatingActionButton.setOnClickListener(v -> {
            showBottomDialog();
        });
    }

    private void triggerGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            replaceFragment(new MusicFragment());

        });
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_layout, fragment)
                    .commit();
        }
    }

    private void showBottomDialog() {

        Dialog dialog = new Dialog(this);
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
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();

    }

    private void addEvent(Dialog dialog, String title, String description, String startTime, String endTime, String date) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                calendarService.addEvent(title, description, startTime, endTime, date);
                Thread.sleep(1000);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Thêm sự kiện thành công", Toast.LENGTH_SHORT).show();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_layout);
                    if (homeFragment != null) {
                        homeFragment.loadEvents();
                    }
                });
            } catch (Exception e) {

                runOnUiThread(() -> {
                    Log.d("Event", e.getMessage());
                    Toast.makeText(MainActivity.this, "Lỗi khi thêm sự kiện", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS);
        }
    }
}
