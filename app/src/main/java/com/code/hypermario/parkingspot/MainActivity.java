package com.code.hypermario.parkingspot;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.code.hypermario.parkingspot.recyclerview.FeedListActivity;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity
{
  TabHost localTabHost;
  protected void onCreate(Bundle paramBundle)
  {
    AdBuddiz.setPublisherKey("eedac9b7-8460-43f2-ba05-d4a0ae4a9f8a");
    AdBuddiz.cacheAds(this); // this = current Activity
    super.onCreate(paramBundle);
    setContentView(R.layout.maintabbed);
    localTabHost = (TabHost)findViewById(android.R.id.tabhost);
    TabSpec localTabSpec1 = localTabHost.newTabSpec("current");
    localTabSpec1.setIndicator("Current");
    localTabSpec1.setContent(new Intent(this, first_tab.class));
    localTabHost.addTab(localTabSpec1);
    TabSpec localTabSpec2 = localTabHost.newTabSpec("history");
    localTabSpec2.setIndicator("History");
    localTabSpec2.setContent(new Intent(this, FeedListActivity.class));
    localTabHost.addTab(localTabSpec2);




  }

  @Override
  protected void onResume()
  {
    super.onResume();
    localTabHost = getTabHost();
    for(int i=0;i<localTabHost.getTabWidget().getChildCount();i++)
    {
      TextView tv = (TextView) localTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
      tv.setTextColor(Color.parseColor("#ffffff"));
      tv.setTextSize(20);
    }
  }
}
