package com.code.hypermario.parkingspot;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import java.io.PrintStream;

public class ActivityRecognitionIntentService extends IntentService
{
  final static int RQS_1 = 1;
  private static final String TAG = ActivityRecognitionIntentService.class.getSimpleName();
  
  public ActivityRecognitionIntentService()
  {
    super("ActivityRecognitionIntentService");
  }
  
  private String getActivityName(int paramInt)
  {
    switch (paramInt)
    {
    case 6: 
    default: 
      return "N/A";
    case 0: 
      return "In Vehicle";
    case 1: 
      return "On Bicycle";
    case 2: 
      return "On Foot";
    case 7: 
      return "Walking";
    case 3: 
      return "Still";
    case 5: 
      return "Tilting";
    case 8: 
      return "Running";
    }
  }
  
  protected void onHandleIntent(Intent paramIntent)
  {
    System.out.println("***receive Intent");
    if (ActivityRecognitionResult.hasResult(paramIntent))
    {
      DetectedActivity localDetectedActivity = ActivityRecognitionResult.extractResult(paramIntent).getMostProbableActivity();
      int i = localDetectedActivity.getConfidence();
      String str = getActivityName(localDetectedActivity.getType());

      //Intent localIntent = new Intent(getBaseContext(), first_tab.class);
      Intent localIntent = new Intent("ImActive");
      localIntent.putExtra("activity", str);
      localIntent.putExtra("confidence", i);
      System.out.println("***new Intent");
      Log.d(TAG, "Most Probable Name : " + str);
      Log.d(TAG, "Confidence : " + i);
      System.out.println("***send back Intent");


      /*PendingIntent pendingIntent =
              PendingIntent.getBroadcast(getBaseContext(),
                      RQS_1, localIntent, PendingIntent.FLAG_ONE_SHOT);
      AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

      if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
      {
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                0, pendingIntent);
        Toast.makeText(getBaseContext(),
                "call alarmManager.set()",
                Toast.LENGTH_LONG).show();
      }else{
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                0, pendingIntent);
        Toast.makeText(getBaseContext(),
                "call alarmManager.setExact()",
                Toast.LENGTH_LONG).show();
      }*/


      sendBroadcast(localIntent);
      System.out.println("***sent back Intent");
      return;
    }
    Log.d(TAG, "Intent had no data returned");
    System.out.println("***null Intent");
  }
}
