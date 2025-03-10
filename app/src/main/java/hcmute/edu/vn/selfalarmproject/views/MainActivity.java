package hcmute.edu.vn.selfalarmproject.views;

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

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.controllers.GoogleCalendarManager;
import hcmute.edu.vn.selfalarmproject.controllers.GoogleSignInManager;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;

    NavigationView navigationView;


    private Bundle savedInstanceState;



    public static GoogleCalendarManager googleCalendarManager;
    public static GoogleSignInAccount account;
    private GoogleSignInManager googleSignInManager;
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

        googleSignInManager = new GoogleSignInManager(this);
        account = googleSignInManager.getLastSignedInAccount(this);
        checkExistingSignIn();


    }

    private void checkExistingSignIn() {
        if (account == null) {
            googleSignInManager.signIn(this);
        } else {
            googleCalendarManager = new GoogleCalendarManager(this);
            updateUI(savedInstanceState);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GoogleSignInManager.GOOGLE_SIGN_IN_REQUEST_CODE) {
            googleSignInManager.handleSignInResult(data, new GoogleSignInManager.SignInCallback() {
                @Override
                public void onSuccess(GoogleSignInAccount account) {
                    updateUI(savedInstanceState);
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(MainActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void signOut() {
        googleSignInManager.signOut(() -> {
            Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();

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
    private void updateUI(Bundle savedInstanceState) {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
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

    }





    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS);
        }
    }
}
