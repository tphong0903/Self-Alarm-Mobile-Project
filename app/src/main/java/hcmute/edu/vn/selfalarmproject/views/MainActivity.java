package hcmute.edu.vn.selfalarmproject.views;

import android.Manifest;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.controllers.GoogleCalendarManager;
import hcmute.edu.vn.selfalarmproject.controllers.GoogleSignInManager;
import hcmute.edu.vn.selfalarmproject.controllers.service.MusicService;
import hcmute.edu.vn.selfalarmproject.utils.SharedPreferencesHelper;
import hcmute.edu.vn.selfalarmproject.views.activities.BlacklistActivity;
import hcmute.edu.vn.selfalarmproject.views.fragments.HomeFragment;
import hcmute.edu.vn.selfalarmproject.views.fragments.MessageFragment;
import hcmute.edu.vn.selfalarmproject.views.fragments.MusicFragment;
import hcmute.edu.vn.selfalarmproject.views.fragments.PhoneFragment;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;

    NavigationView navigationView;


    private Bundle savedInstanceState;

    MenuItem loginItem;
    MenuItem logoutItem;


    public static GoogleCalendarManager googleCalendarManager;
    public static GoogleSignInAccount account;
    private GoogleSignInManager googleSignInManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();


        this.savedInstanceState = savedInstanceState;
        googleSignInManager = new GoogleSignInManager(this);
        account = googleSignInManager.getLastSignedInAccount(this);
        if (account != null) {
            SharedPreferencesHelper.saveGoogleUid(this, account.getId());
            googleCalendarManager = new GoogleCalendarManager(this);
        }
        updateUI(savedInstanceState);


    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MusicService.class));
    }

    private void signIn() {
        googleSignInManager.signIn(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GoogleSignInManager.GOOGLE_SIGN_IN_REQUEST_CODE) {
            googleSignInManager.handleSignInResult(data, new GoogleSignInManager.SignInCallback() {
                @Override
                public void onSuccess(GoogleSignInAccount account) {
                    MainActivity.account = account;
                    googleCalendarManager = new GoogleCalendarManager(MainActivity.this);
                    updateUI(savedInstanceState);
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(MainActivity.this, "Login failed: ", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void signOut() {
        googleSignInManager.signOut(() -> {
            Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
            account = null;
            SharedPreferencesHelper.clearGoogleUid(MainActivity.this);
            updateMenuVisibility();
            updateUI(savedInstanceState);
        });
    }

    private void updateMenuVisibility() {
        if (loginItem != null && logoutItem != null) {
            loginItem.setVisible(account == null);
            logoutItem.setVisible(account != null);
        }
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
            } else if (id == R.id.nav_login) {
                signIn();
            } else if (id == R.id.nav_blacklist) {
                Intent intent = new Intent(MainActivity.this, BlacklistActivity.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        Menu menu = navigationView.getMenu();
        LinearLayout navHeader = (LinearLayout) navigationView.getHeaderView(0);
        TextView name = navHeader.findViewById(R.id.nav_name);
        TextView email = navHeader.findViewById(R.id.nav_email);
        loginItem = menu.findItem(R.id.nav_login);
        logoutItem = menu.findItem(R.id.nav_logout);
        if (account == null) {
            navHeader.setVisibility(View.GONE);
            loginItem.setVisible(true);
            logoutItem.setVisible(false);
        } else {
            navHeader.setVisibility(View.VISIBLE);
            name.setText(account.getDisplayName());
            email.setText(account.getEmail());
            loginItem.setVisible(false);
            logoutItem.setVisible(true);
        }


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
        String[] allPermissions = {
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.ANSWER_PHONE_CALLS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_MMS,
                Manifest.permission.POST_NOTIFICATIONS
        };
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : allPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    1);
        }
        checkCallScreeningRole();
    }

    private void checkCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
            if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
                Log.d("TAG", "Requesting ROLE_CALL_SCREENING...");
            } else {
                Log.d("TAG", "Already has ROLE_CALL_SCREENING.");
            }
        }
    }

}
