package com.example.callcalendar;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            Log.d("CallReceiver", "통화 종료 감지됨");

            Intent serviceIntent = new Intent(context, RecordingFinderService.class);
            context.startService(serviceIntent);
        }
    }
}

