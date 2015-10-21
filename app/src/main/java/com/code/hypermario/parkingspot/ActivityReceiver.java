package com.code.hypermario.parkingspot;


import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ActivityReceiver extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private final IBinder mBinder = new LocalBinder();
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
    //private DownloadWebPageTask task;
    private TextView textView;
    PendingIntent mActivityRecongPendingIntent = null;
    private Integer images[] = {R.drawable.car, R.drawable.man,R.drawable.hole};
    private int currImage = 0;
    int width;
    int height;
    String previousActivity = "dead";
    boolean flag = true;

    private boolean running ;

    private File rootImage;

    static
    {
        FATEST_INTERVAL = 1000;
    }


    public ActivityReceiver() {
        super("ActivityReceiver");
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
               System.out.println("ERROR user recoverable***");
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }


    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text
            //btnStartLocationUpdates.setText(getString(R.string.btn_stop_location_updates));

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();

            /////activity service recognition start////////////
            System.out.println("**try to find Intent");
            Intent i = new Intent(this, ActivityRecognitionIntentService.class);
            mActivityRecongPendingIntent = PendingIntent
                    .getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

            Log.d(TAG, "connected to ActivityRecognition");
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 0, mActivityRecongPendingIntent);


            //Update the TextView
            //textView.setText("Connected to Google Play Services \nWaiting for Active Recognition... \n");
            /////activity service recognition start////////////


            System.out.println("**try Create Receiver");
            receiver  = new BroadcastReceiver() {
                @Override
                public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent) {
                    System.out.println("** ACTIVITYRECEIVER receive new Intent from activity recognition");
                    Calendar localCalendar = Calendar.getInstance();
                    String str1 = new SimpleDateFormat("h:mm:ss a").format(localCalendar.getTime());
                    String str2 = str1 + " " + paramAnonymousIntent.getStringExtra("activity") + " " + "Confidence : " + paramAnonymousIntent.getExtras().getInt("confidence") + "\n";
                    //String str3 = first_tab.this.textView.getText() + str2;
                    //first_tab.this.textView.setText(str3);
                    String newActivity = "dead";
                    if (paramAnonymousIntent.getStringExtra("activity").equals("dead"))
                    {
                        System.out.println(" ACTIVITYRECEIVER receiver dead");
                        flag = false;
                        return;
                    }
                    if (paramAnonymousIntent.getStringExtra("activity").equals("Walking") || paramAnonymousIntent.getStringExtra("activity").equals("On Foot")) {

                        if(paramAnonymousIntent.getIntExtra("confidence",0) > 40)
                        {
                            newActivity = new String("Walking");
                        }
                    }
                    if (paramAnonymousIntent.getStringExtra("activity").equals("In Vehicle")) {

                        previousActivity = new String("In Vehicle");
                    }
                    if (previousActivity.equals("In Vehicle") && newActivity.equals("Walking"))
                    //if(!paramAnonymousIntent.getStringExtra("activity").equals("Still"))
                    {
                        double d1 = mLastLocation.getLatitude();
                        double d2 = mLastLocation.getLongitude();
                        //textView.setText("Last Location lat : " + d1 + " long : " + d2);
                        writeLatestLocation(d1, d2);
                        //mGoogleApiClient.disconnect();
                        //mGoogleApiClient.connect();
                        stopLocationUpdates();
                        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, mActivityRecongPendingIntent);
                        mRequestingLocationUpdates=false;  //den exei nohma einai allh diergasia
                        unregisterReceiver(receiver);
                        receiver = null;


                        Intent localIntent = new Intent("ImActive");
                        localIntent.putExtra("activity", "DEAD");
                        localIntent.putExtra("confidence", 100);
                        sendBroadcast(localIntent);
                        return;
                    }
                }
            };
            System.out.println("**finish Create Receiver");
            IntentFilter localIntentFilter = new IntentFilter();
            localIntentFilter.addAction("ImActive");
            registerReceiver(receiver, localIntentFilter);

            Log.d(TAG, "Periodic location updates started!");

        } else {
            // Changing the button text
            //btnStartLocationUpdates.setText(getString(R.string.btn_start_location_updates));

            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, mActivityRecongPendingIntent);

            Intent localIntent = new Intent("ImActive");
            localIntent.putExtra("activity", "dead");
            localIntent.putExtra("confidence", 100);
            Log.d(TAG, "Most Probable Name : " + "dead");
            Log.d(TAG, "Confidence : " + 100);
            sendBroadcast(localIntent);

            unregisterReceiver(this.receiver);
            receiver = null;

            //this.task.cancel(true);

            Log.d(TAG, "Periodic location updates stopped!");
        }
    }

    protected void createLocationRequest()
    {
        this.mLocationRequest = new LocationRequest();
        this.mLocationRequest.setInterval(UPDATE_INTERVAL);
        this.mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        this.mLocationRequest.setPriority(100);
        this.mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        System.out.println("*** OnStartCommand ***");
        //mGoogleApiClient.connect();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /*@Override
    public void onCreate() {
        super.onCreate();

        if (checkPlayServices())
        {
            System.out.println("**try build API MAP");
            buildGoogleApiClient();
            System.out.println("**try build API ActivityRecognition");
            createLocationRequest();
            mGoogleApiClient.connect();
            System.out.println("**try connect google");
        }


        togglePeriodicLocationUpdates();


        System.out.println("**finish OnCreate");

    }*/

    /*@Override
    public void onDestroy()
    {
        System.out.println("ACTIVITY RECEIVER  On Destroy");
        super.onDestroy();
        if(mActivityRecongPendingIntent != null) ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, mActivityRecongPendingIntent);
        this.mGoogleApiClient.disconnect();
        if(receiver != null) unregisterReceiver(this.receiver);
    }*/

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (checkPlayServices())
        {
            System.out.println("**try build API MAP");
            buildGoogleApiClient();
            System.out.println("**try build API ActivityRecognition");
            createLocationRequest();
            mGoogleApiClient.connect();
            mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
            System.out.println("**try connect google");
        }


        togglePeriodicLocationUpdates();

        while(flag)
        {

        }

        //System.out.println("** ACTIVITY RECEIVE finish onHandleIntent");
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        //displayLocation();
    }

    @Override
    public void onConnected(Bundle paramBundle)
    {
        //displayLocation();
        System.out.println("*** " + "SUCCESS CONNECT" + " ****");
        if (this.mRequestingLocationUpdates) {
            startLocationUpdates();

            System.out.println("**try to find Intent");
            Intent i = new Intent(this, ActivityRecognitionIntentService.class);
            mActivityRecongPendingIntent = PendingIntent
                    .getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

            Log.d(TAG, "connected to ActivityRecognition");
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 0, mActivityRecongPendingIntent);


            //Update the TextView
            //textView.setText("Connected to Google Play Services \nWaiting for Active Recognition... \n");
        }

    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("*** fail connection ****");
        System.out.println("*** " + connectionResult.toString()+" ****");
    }

    @Override
    public void onConnectionSuspended(int paramInt)
    {
        this.mGoogleApiClient.connect();
    }



    public class LocalBinder extends Binder {
        ActivityReceiver getService() {
            return ActivityReceiver.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        System.out.println("ON DESTROY ACTIVITY RECEIVER");
        Toast.makeText(this, "MyService Stopped", Toast.LENGTH_LONG).show();
        super.onDestroy();
        if(mActivityRecongPendingIntent != null) ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, mActivityRecongPendingIntent);
        this.mGoogleApiClient.disconnect();
        if(receiver != null) unregisterReceiver(this.receiver);
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


    /*private class DownloadWebPageTask extends AsyncTask<String, Void, String>
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
                    //String str3 = first_tab.this.textView.getText() + str2;
                    //first_tab.this.textView.setText(str3);
                    String newActivity = "dead";
                    if (paramAnonymousIntent.getStringExtra("activity").equals("dead"))
                    {
                        System.out.println("receiver dead");
                        return;
                    }
                    if (paramAnonymousIntent.getStringExtra("activity").equals("Walking") || paramAnonymousIntent.getStringExtra("activity").equals("On Foot")) {

                        if(paramAnonymousIntent.getIntExtra("confidence",0) > 40)
                        {
                            newActivity = new String("Walking");
                        }
                    }
                    if (paramAnonymousIntent.getStringExtra("activity").equals("In Vehicle")) {

                        previousActivity = new String("In Vehicle");
                    }
                    //if (previousActivity.equals("In Vehicle") && newActivity.equals("Walking")) {
                    if(!paramAnonymousIntent.getStringExtra("activity").equals("Still"))
                    {
                        double d1 = mLastLocation.getLatitude();
                        double d2 = mLastLocation.getLongitude();
                        //textView.setText("Last Location lat : " + d1 + " long : " + d2);
                        writeLatestLocation(d1, d2);
                        //mGoogleApiClient.disconnect();
                        //mGoogleApiClient.connect();
                        stopLocationUpdates();
                        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, mActivityRecongPendingIntent);
                        mRequestingLocationUpdates=false;  //den exei nohma einai allh diergasia
                        unregisterReceiver(receiver);
                        receiver = null;


                        Intent localIntent = new Intent("ImActive");
                        localIntent.putExtra("activity", "DEAD");
                        sendBroadcast(localIntent);
                        return;
                    }
                }
            };
            System.out.println("**finish Create Receiver");
            IntentFilter localIntentFilter = new IntentFilter();
            localIntentFilter.addAction("ImActive");
            registerReceiver(receiver, localIntentFilter);
            return "kati einai";
        }

        protected void onPostExecute(String paramString)
        {
            //textView.setText(paramString);
        }
    }*/
}
