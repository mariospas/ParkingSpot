package com.code.hypermario.parkingspot.location;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;



public class LocationListener implements android.location.LocationListener{
    private static final String TAG = "LocationListener";
    Location mLastLocation;
    public LocationListener(String provider)
    {
        Log.e(TAG, "LocationListener " + provider);
        mLastLocation = new Location(provider);
    }
    @Override
    public void onLocationChanged(Location location)
    {
        Log.e(TAG, "onLocationChanged: " + location);
        mLastLocation.set(location);
    }
    @Override
    public void onProviderDisabled(String provider)
    {
        Log.e(TAG, "onProviderDisabled: " + provider);
    }
    @Override
    public void onProviderEnabled(String provider)
    {
        Log.e(TAG, "onProviderEnabled: " + provider);
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        Log.e(TAG, "onStatusChanged: " + provider);
    }

    public Location getLocation()
    {
        return mLastLocation;
    }
}