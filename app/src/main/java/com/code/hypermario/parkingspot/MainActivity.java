package com.code.hypermario.parkingspot;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import com.code.hypermario.parkingspot.recyclerview.FeedListActivity;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity
{
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.maintabbed);
    TabHost localTabHost = (TabHost)findViewById(android.R.id.tabhost);
    TabSpec localTabSpec1 = localTabHost.newTabSpec("current");
    localTabSpec1.setIndicator("Current");
    localTabSpec1.setContent(new Intent(this, first_tab.class));
    localTabHost.addTab(localTabSpec1);
    TabSpec localTabSpec2 = localTabHost.newTabSpec("history");
    localTabSpec2.setIndicator("History");
    localTabSpec2.setContent(new Intent(this, FeedListActivity.class));
    localTabHost.addTab(localTabSpec2);
  }
}
