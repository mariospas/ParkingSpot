package com.code.hypermario.parkingspot;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity
  extends FragmentActivity
  implements OnMapReadyCallback
{
  private GoogleMap mMap;
  private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener()
  {
    public void onMyLocationChange(Location paramAnonymousLocation)
    {
      LatLng localLatLng = new LatLng(paramAnonymousLocation.getLatitude(), paramAnonymousLocation.getLongitude());
      MapsActivity.this.mMap.addMarker(new MarkerOptions().position(localLatLng));
      if (MapsActivity.this.mMap != null) {
        MapsActivity.this.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(localLatLng, 16.0F));
      }
    }
  };
  
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.activity_maps);
    ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    this.mMap.setOnMyLocationChangeListener(this.myLocationChangeListener);
  }
  
  public void onMapReady(GoogleMap paramGoogleMap)
  {
    this.mMap = paramGoogleMap;
    LatLng localLatLng = new LatLng(-34.0D, 151.0D);
    this.mMap.addMarker(new MarkerOptions().position(localLatLng).title("Marker in Sydney"));
    this.mMap.moveCamera(CameraUpdateFactory.newLatLng(localLatLng));
  }
}
