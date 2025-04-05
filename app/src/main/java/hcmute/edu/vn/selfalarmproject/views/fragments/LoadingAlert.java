package hcmute.edu.vn.selfalarmproject.views.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import hcmute.edu.vn.selfalarmproject.R;

public class LoadingAlert {
    Activity activity;
    AlertDialog alertDialog;

    public LoadingAlert(Activity activity) {
        this.activity = activity;
    }

    public void startAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        builder.setView(layoutInflater.inflate(R.layout.alert_dialog, null));

        builder.setCancelable(true);

        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void stopAlert() {
        alertDialog.dismiss();
    }
}
