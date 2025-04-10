package hcmute.edu.vn.selfalarmproject.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.api.services.calendar.CalendarScopes;

import hcmute.edu.vn.selfalarmproject.BuildConfig;

public class GoogleSignInManager {
    private static final String TAG = "GoogleSignInManager";
    private final GoogleSignInClient googleSignInClient;
    public static final int GOOGLE_SIGN_IN_REQUEST_CODE = 9001;
    private final Context context;


    public GoogleSignInManager(Context context) {
        this.context = context;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR))
                .requestIdToken(BuildConfig.GOOGLE_API_KEY)
                .build();

        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public void signIn(Activity activity) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE);
    }

    public void handleSignInResult(Intent data, SignInCallback callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                SharedPreferencesHelper.saveGoogleUid(context, account.getId());
                callback.onSuccess(account);
            } else {
                callback.onFailure("Vui lòng thử lại.");
            }
        } catch (ApiException e) {
            Log.e(TAG, "Lỗi đăng nhập: " + e.getStatusCode());
            callback.onFailure("Đăng nhập thất bại: " + e.getMessage());
        }
    }


    public void signOut(SignOutCallback callback) {
        googleSignInClient.signOut().addOnCompleteListener(task -> callback.onSuccess());
    }

    public GoogleSignInAccount getLastSignedInAccount(Context context) {
        return GoogleSignIn.getLastSignedInAccount(context);
    }

    public interface SignInCallback {
        void onSuccess(GoogleSignInAccount account);

        void onFailure(String error);
    }

    public interface SignOutCallback {
        void onSuccess();
    }


}
