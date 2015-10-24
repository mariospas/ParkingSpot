package com.code.hypermario.parkingspot.alarm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context arg0, Intent arg1) {

        if(arg1.getStringExtra("activity").equals("alarm"))
        {
            // For our recurring task, we'll just display a message
            //Toast.makeText(arg0, "I'm running", Toast.LENGTH_SHORT).show();
            Intent localIntent = new Intent("ImActive");
            localIntent.putExtra("activity", "Still");
            localIntent.putExtra("confidence", 100);

            arg0.sendBroadcast(localIntent);
        }


    }

}