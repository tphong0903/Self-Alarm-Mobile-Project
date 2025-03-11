package hcmute.edu.vn.selfalarmproject.service;

import android.net.Uri;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import hcmute.edu.vn.selfalarmproject.utils.BlacklistHelper;

public class CallScreenerService extends CallScreeningService {
    private static final String TAG = "CallScreenerService";

    @Override
    public void onScreenCall(Call.Details callDetails) {
        Log.d(TAG, "onScreenCall called");

        Uri handle = callDetails.getHandle();
        if (handle == null) {
            Log.d(TAG, "Call handle is null");
            CallResponse.Builder response = new CallResponse.Builder();
            response.setDisallowCall(false).setRejectCall(false);
            respondToCall(callDetails, response.build());
            return;
        }

        String phoneNumber = handle.getSchemeSpecificPart();
        Log.d(TAG, "Screening call from: " + phoneNumber);

        BlacklistHelper.isNumberBlacklisted(this, phoneNumber, isBlacklisted -> {
            CallResponse.Builder response = new CallResponse.Builder();

            if (isBlacklisted) {
                Log.d(TAG, "Rejecting blacklisted call from: " + phoneNumber);
                response.setDisallowCall(true)
                        .setRejectCall(true)
                        .setSkipCallLog(false)
                        .setSkipNotification(false);
            } else {
                Log.d(TAG, "Allowing call from: " + phoneNumber);
                response.setDisallowCall(false)
                        .setRejectCall(false);
            }

            respondToCall(callDetails, response.build());
        });
    }
}