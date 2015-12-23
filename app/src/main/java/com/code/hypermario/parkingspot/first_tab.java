package com.code.hypermario.parkingspot;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionApi;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class first_tab extends AppCompatActivity
{
    private static int DISPLACEMENT = 10;
    private static int FATEST_INTERVAL = 0;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static int UPDATE_INTERVAL = 10000;
    private Button btnShowLocation;
    private Button btnStartLocationUpdates;
    private LinearLayout lView;
    private TextView lblLocation;
    private Context mContext;
    private GoogleApiClient mGApiClient;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = false;
    private TextView myText = null;
    private BroadcastReceiver receiver;
    private DownloadWebPageTask task;
    private TextView textView;
    PendingIntent mActivityRecongPendingIntent = null;
    private Integer images[] = {R.drawable.car, R.drawable.man,R.drawable.hole};
    private int currImage = 0;
    int width;
    int height;
    String previousActivity = "dead";
    Intent active;
  
  static
  {
    FATEST_INTERVAL = 5000;
  }



  /**
   * Method to toggle periodic location updates
   * */

  private boolean isMyServiceRunning(Class<?> serviceClass) {
      ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
      for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
          if (serviceClass.getName().equals(service.service.getClassName())) {
              return true;
          }
      }
      return false;
  }

  private void togglePeriodicLocationUpdates() {
    if (!isMyServiceRunning(ActivityReceiver.class)) {
        // Changing the button text
        btnStartLocationUpdates.setText(getString(R.string.btn_stop_location_updates));

        mRequestingLocationUpdates = true;


        active = new Intent(this, ActivityReceiver.class);
        startService(active);



        this.task = new DownloadWebPageTask();
        this.task.execute(new String[]{"http://www.vogella.com"});

        Log.d(TAG, "Periodic location updates started!");

    }
    else
    {
        // Changing the button text
        btnStartLocationUpdates.setText(getString(R.string.btn_start_location_updates));

        mRequestingLocationUpdates = false;

        Intent i = new Intent(this, ActivityReceiver.class);
        stopService(i);

        unregisterReceiver(this.receiver);
        receiver = null;

        Intent localIntent = new Intent("ImActive");
        localIntent.putExtra("activity", "dead");
        localIntent.putExtra("confidence", 100);
        Log.d(TAG, "Most Probable Name : " + "dead");
        Log.d(TAG, "Confidence : " + 100);
        sendBroadcast(localIntent);



        //this.task.cancel(true);

        Log.d(TAG, "Periodic location updates stopped!");
    }
    AdBuddiz.showAd(this); // this = current Activity
  }



  
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);

      final Dialog dialog = new Dialog(this);;

      File f = new File(getFilesDir(), "NewUser.txt");
      if(!f.exists() && !f.isDirectory())
      {
          dialog.setContentView(R.layout.dialog);
          dialog.setTitle("Intro");
          TextView text = (TextView) dialog.findViewById(R.id.TextView01);
          text.setText("- Press start tracking button before start driving.\n" +
                  "- When you park your car and start walking the app will save your parking location automatically.\n" +
                  "- After that if you want to see your last parking location just click on History tab");
          try {
              f.createNewFile();
          } catch (IOException e) {
              e.printStackTrace();
          }

          Button button = (Button) dialog.findViewById(R.id.Button01);
          button.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View view) {
                  dialog.dismiss();
              }
          });

          dialog.show();
      }




    setContentView(R.layout.content_main);
    //this.lView = ((LinearLayout)findViewById(R.id.dynamicTxT));
    this.lblLocation = new TextView(this);
    //this.btnShowLocation = ((Button)findViewById(R.id.buttonStart));
    this.btnStartLocationUpdates = ((Button)findViewById(R.id.buttonLoop));
    this.mContext = this;



    // Get Location Manager and check for GPS & Network location services
    LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
    if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    {
      // Build the alert dialog
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Location Services Not Active");
      builder.setMessage("Please enable Location Services and GPS");
      builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialogInterface, int i) {
              // Show location settings when the user acknowledges the alert dialog
              Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
              startActivity(intent);
          }
      });
      Dialog alertDialog = builder.create();
      alertDialog.setCanceledOnTouchOutside(false);
      alertDialog.show();
    }


    boolean running = isMyServiceRunning(ActivityReceiver.class);
    if(running)
    {
        btnStartLocationUpdates.setText(getString(R.string.btn_stop_location_updates));
        this.task = new DownloadWebPageTask();
        this.task.execute(new String[]{"http://www.vogella.com"});
    }
    this.btnStartLocationUpdates.setOnClickListener(new OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        togglePeriodicLocationUpdates();
      }
    });

      Display display = getWindowManager().getDefaultDisplay();
      Point size = new Point();
      display.getSize(size);
      width = size.x;
      height = size.y;

      currImage = 2;
      setCurrentImage();

    System.out.println("**finish OnCreate");
  }


    private void setInitialImage() {
        setCurrentImage();
    }

    private void setCurrentImage() {

        final ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);
        //imageView.setImageResource(images[currImage]);
        Picasso.with(this).load(images[currImage]).resize((int) (width * 0.9), (int) (width * 0.9)).into(imageView);

    }
  
  protected void onDestroy()
  {
    super.onDestroy();
    /*if(mActivityRecongPendingIntent != null) ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, mActivityRecongPendingIntent);
    this.mGoogleApiClient.disconnect();*/
    if(receiver != null) unregisterReceiver(this.receiver);
  }


  
  protected void onPause()
  {
    super.onPause();
  }
  
  protected void onResume()
  {
    super.onResume();

  }
  
  protected void onStart()
  {
    super.onStart();
  }
  
  protected void onStop()
  {
    super.onStop();
  }

  
  private class DownloadWebPageTask extends AsyncTask<String, Void, String>
  {
    private DownloadWebPageTask() {}
    
    protected String doInBackground(String... paramVarArgs)
    {
        System.out.println("** FirstTAB try Create Receiver");
        receiver  = new BroadcastReceiver() {
            @Override
            public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent) {
                System.out.println("** FirstTAB receive new Intent from activity recognition");
                Calendar localCalendar = Calendar.getInstance();
                String str1 = new SimpleDateFormat("h:mm:ss a").format(localCalendar.getTime());
                String str2 = str1 + " " + paramAnonymousIntent.getStringExtra("activity") + " " + "Confidence : " + paramAnonymousIntent.getExtras().getInt("confidence") + "\n";
                String newActivity = "dead";
                if (paramAnonymousIntent.getStringExtra("activity").equals("dead"))
                {
                    System.out.println("FirstTAB receiver dead");
                    return;
                }
                if (paramAnonymousIntent.getStringExtra("activity").equals("Walking") || paramAnonymousIntent.getStringExtra("activity").equals("On Foot")) {
                    currImage = 1;
                    setCurrentImage();

                    if(paramAnonymousIntent.getIntExtra("confidence",0) > 40)
                    {
                        newActivity = new String("Walking");
                    }
                }
                if (paramAnonymousIntent.getStringExtra("activity").equals("In Vehicle")) {
                    currImage = 0;
                    setCurrentImage();
                    previousActivity = new String("In Vehicle");
                }
                if (previousActivity.equals("In Vehicle") && newActivity.equals("Walking"))
                //if(!paramAnonymousIntent.getStringExtra("activity").equals("Still"))
                {
                    btnStartLocationUpdates.setText(getString(R.string.btn_start_location_updates));
                    mRequestingLocationUpdates=false;  //den exei nohma einai allh diergasia
                    unregisterReceiver(receiver);
                    receiver = null;
                    return;
                }
            }
        };
        System.out.println("** FirstTAB finish Create Receiver");
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction("ImActive");
        registerReceiver(receiver, localIntentFilter);
        return "kati einai";
    }
    
    protected void onPostExecute(String paramString)
    {
      //textView.setText(paramString);
    }
  }
}
