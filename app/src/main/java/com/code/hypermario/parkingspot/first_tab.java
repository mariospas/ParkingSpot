package com.code.hypermario.parkingspot;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class first_tab
  extends AppCompatActivity
  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
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
  
  static
  {
    FATEST_INTERVAL = 5000;
  }

  private boolean checkPlayServices() {
    int resultCode = GooglePlayServicesUtil
            .isGooglePlayServicesAvailable(this);
    if (resultCode != ConnectionResult.SUCCESS) {
      if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
        GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                PLAY_SERVICES_RESOLUTION_REQUEST).show();
      } else {
        Toast.makeText(getApplicationContext(),
                "This device is not supported.", Toast.LENGTH_LONG)
                .show();
        finish();
      }
      return false;
    }
    return true;
  }

  private void displayLocation() {

    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

    if (mLastLocation != null) {
      double latitude = mLastLocation.getLatitude();
      double longitude = mLastLocation.getLongitude();

      lblLocation.setText(latitude + ", " + longitude);
      lView.removeAllViews();
      lView.addView(lblLocation);

    } else {

      lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
      lView.removeAllViews();
      lView.addView(lblLocation);
    }
  }

  /**
   * Method to toggle periodic location updates
   * */
  private void togglePeriodicLocationUpdates() {
    if (!mRequestingLocationUpdates) {
      // Changing the button text
      btnStartLocationUpdates.setText(getString(R.string.btn_stop_location_updates));

      mRequestingLocationUpdates = true;

      // Starting the location updates
      startLocationUpdates();

      Log.d(TAG, "Periodic location updates started!");

    } else {
      // Changing the button text
      btnStartLocationUpdates.setText(getString(R.string.btn_start_location_updates));

      mRequestingLocationUpdates = false;

      // Stopping the location updates
      stopLocationUpdates();

      Log.d(TAG, "Periodic location updates stopped!");
    }
  }

  /**
   * Creating google api client object
   * */
  protected synchronized void buildGoogleApiClient() {
      this.mGoogleApiClient = new GoogleApiClient.Builder(this)
              .addConnectionCallbacks(this)
              .addOnConnectionFailedListener(this)
              .addApi(LocationServices.API)
              .addApi(ActivityRecognition.API)
              .build();
  }

  
  protected void createLocationRequest()
  {
    this.mLocationRequest = new LocationRequest();
    this.mLocationRequest.setInterval(UPDATE_INTERVAL);
    this.mLocationRequest.setFastestInterval(FATEST_INTERVAL);
    this.mLocationRequest.setPriority(100);
    this.mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
  }
  
  public void onConnected(Bundle paramBundle)
  {
      displayLocation();
      if (this.mRequestingLocationUpdates) {
        startLocationUpdates();
      }
      System.out.println("**try to find Intent");
      Intent i = new Intent(this, ActivityRecognitionIntentService.class);
      PendingIntent mActivityRecongPendingIntent = PendingIntent
              .getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

      Log.d(TAG, "connected to ActivityRecognition");
      ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 0, mActivityRecongPendingIntent);

      //Update the TextView
      textView.setText("Connected to Google Play Services \nWaiting for Active Recognition... \n");
  }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Not connected to ActivityRecognition");
    }
  
  public void onConnectionSuspended(int paramInt)
  {
    this.mGoogleApiClient.connect();
  }
  
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.content_main);
    this.lView = ((LinearLayout)findViewById(R.id.dynamicTxT));
    this.lblLocation = new TextView(this);
    this.btnShowLocation = ((Button)findViewById(R.id.buttonStart));
    this.btnStartLocationUpdates = ((Button)findViewById(R.id.buttonLoop));
    this.textView = ((TextView)findViewById(R.id.msg));
    this.textView.setMovementMethod(new ScrollingMovementMethod());
    this.mContext = this;
    if (checkPlayServices())
    {
      System.out.println("**try build API MAP");
      buildGoogleApiClient();
      System.out.println("**try build API ActivityRecognition");
      createLocationRequest();
    }
    this.btnShowLocation.setOnClickListener(new OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        displayLocation();
      }
    });
    this.btnStartLocationUpdates.setOnClickListener(new OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        togglePeriodicLocationUpdates();
      }
    });
    this.task = new DownloadWebPageTask();
      mRequestingLocationUpdates=false; //gt den ginete na enhmerothei apo thn allh diergasia
      this.task.execute(new String[] { "http://www.vogella.com" });
    System.out.println("**finish OnCreate");
  }
  
  protected void onDestroy()
  {
    super.onDestroy();
    this.mGoogleApiClient.disconnect();
    unregisterReceiver(this.receiver);
    this.task.cancel(true);
  }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();
    }
  
  protected void onPause()
  {
    super.onPause();
  }
  
  protected void onResume()
  {
    super.onResume();
    checkPlayServices();
    if ((this.mGoogleApiClient.isConnected()) && (this.mRequestingLocationUpdates)) {
      startLocationUpdates();
    }
  }
  
  protected void onStart()
  {
    super.onStart();
    this.mGoogleApiClient.connect();
  }
  
  protected void onStop()
  {
    super.onStop();
  }

  
  protected void startLocationUpdates()
  {
    LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, this.mLocationRequest, this);
  }
  
  protected void stopLocationUpdates()
  {
    LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, this);
  }
  
  public void writeLatestLocation(double paramDouble1, double paramDouble2)
  {
      System.out.println("***Write***");
    File localFile = new File(getFilesDir(), "parking_locations.txt");
    if (!localFile.exists()) {
        try {
            localFile.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
      Calendar localCalendar = Calendar.getInstance();
      int i = localCalendar.get(Calendar.DAY_OF_MONTH);
      int j = localCalendar.get(Calendar.MONTH);
      int k = localCalendar.get(Calendar.YEAR);
      int m = localCalendar.get(Calendar.HOUR);
      int n = localCalendar.get(Calendar.MINUTE);
      String str1 = i + "/" + j + "/" + k + " " + m + ":" + n;
      String str2 = paramDouble1 + ";" + paramDouble2 + ";" + str1 + "\n";

      System.out.println("***Write " + str2);

      try{
          FileOutputStream localFileOutputStream = openFileOutput("parking_locations.txt", 32768);
          localFileOutputStream.write(str2.getBytes());
          localFileOutputStream.close();
      }
      catch (IOException e)
      {
          e.printStackTrace();
      }

  }
  
  private class DownloadWebPageTask extends AsyncTask<String, Void, String>
  {
    private DownloadWebPageTask() {}
    
    protected String doInBackground(String... paramVarArgs)
    {
      System.out.println("**try Create Receiver");
        receiver  = new BroadcastReceiver() {
            @Override
            public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent) {
                System.out.println("**receive new Intent from activity recognition");
                Calendar localCalendar = Calendar.getInstance();
                String str1 = new SimpleDateFormat("h:mm:ss a").format(localCalendar.getTime());
                String str2 = str1 + " " + paramAnonymousIntent.getStringExtra("activity") + " " + "Confidence : " + paramAnonymousIntent.getExtras().getInt("confidence") + "\n";
                String str3 = first_tab.this.textView.getText() + str2;
                first_tab.this.textView.setText(str3);
                if (!paramAnonymousIntent.getStringExtra("activity").equals("Still")) {
                    double d1 = mLastLocation.getLatitude();
                    double d2 = mLastLocation.getLongitude();
                    textView.setText("Last Location lat : " + d1 + " long : " + d2);
                    writeLatestLocation(d1, d2);
                    mGoogleApiClient.disconnect();
                    btnStartLocationUpdates.setText(getString(R.string.btn_start_location_updates));
                    mRequestingLocationUpdates=false;  //den exei nohma einai allh diergasia
                }
            }
        };
      System.out.println("**finish Create Receiver");
      IntentFilter localIntentFilter = new IntentFilter();
      localIntentFilter.addAction("ImActive");
      registerReceiver(receiver, localIntentFilter);
      return "";
    }
    
    protected void onPostExecute(String paramString)
    {
      textView.setText(paramString);
    }
  }
}
