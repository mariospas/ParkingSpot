package com.code.hypermario.parkingspot.recyclerview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.code.hypermario.parkingspot.R;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FeedListActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener
{
  private static final String TAG = "RecyclerViewExample";
  private MyRecyclerAdapter adapter;
  private ArrayList feedItemList = new ArrayList();
  private LinkedList<String> list_string = new LinkedList();
  private RecyclerView mRecyclerView;
  private SwipeRefreshLayout mSwipeRefreshLayout;
  private AppBarLayout appBarLayout;
  
  private void parseResult(LinkedList<String> paramLinkedList)
  {
    if (this.feedItemList == null) {
      this.feedItemList = new ArrayList();
    }
    for(String elem : paramLinkedList)
    {
      String[] arrayOfString = elem.split(";");
      if(arrayOfString.length < 3) break;
      String str1 = arrayOfString[0];
      String str2 = arrayOfString[1];
      String str3 = arrayOfString[2];
      String str4 = "Latitude " + str1 + "\nLongitude : " + str2;
      System.out.println("***" + str3 + " " + str1 + " " + str2);
      FeedItem localFeedItem = new FeedItem();
      localFeedItem.setTitle(str3+"\n"+str4);
      localFeedItem.setThumbnail("Latitude " + str1 + " Longitude : " + str2);
      this.feedItemList.add(localFeedItem);
      System.out.println("***Inserted");
    }

  }
  
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    //requestWindowFeature(5);
    setContentView(R.layout.activity_history);
    this.mRecyclerView = ((RecyclerView)findViewById(R.id.recycler_view));
    mRecyclerView.addItemDecoration(
            new HorizontalDividerItemDecoration.Builder(this)
                    .color(Color.rgb(89,211,225))
                    .sizeResId(R.dimen.cardview_compat_inset_shadow)
                    .marginResId(R.dimen.activity_horizontal_margin, R.dimen.activity_horizontal_margin)
                    .build());
    this.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    this.mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
      public void onItemClick(View paramAnonymousView, int paramAnonymousInt) {
        System.out.println("//**//" + paramAnonymousInt + "//**//");
        String[] arrayOfString = FeedListActivity.this.returnLatLong(paramAnonymousInt, FeedListActivity.this.list_string);
        System.out.println("//**//" + arrayOfString[0] + " " + arrayOfString[1] + "//**//");
        Intent localIntent = new Intent("android.intent.action.VIEW", Uri.parse("geo:0,0?q=" + arrayOfString[0] + "," + arrayOfString[1]));
        localIntent.setPackage("com.google.android.apps.maps");
        FeedListActivity.this.startActivity(localIntent);
      }
    }));


    mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.contentView);
    mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
        feedItemList = new ArrayList();
        list_string = new LinkedList();
        new AsyncHttpTask().execute(new String[]{"parking_locations.txt"});
      }
    });

    appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);


    File localFile = new File(getFilesDir(), "parking_locations.txt");
    if (!localFile.exists()) {
      try {
        localFile.createNewFile();
        return;
      } catch (IOException localIOException) {
        localIOException.printStackTrace();
      }
    }
    new AsyncHttpTask().execute(new String[]{"parking_locations.txt"});
  }

  @Override
  public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
    if (i == 0) {
      mSwipeRefreshLayout.setEnabled(true);
    } else {
      mSwipeRefreshLayout.setEnabled(false);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    appBarLayout.addOnOffsetChangedListener(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    appBarLayout.removeOnOffsetChangedListener(this);
  }
  
  public String[] returnLatLong(int paramInt, LinkedList<String> paramLinkedList)
  {
    String[] arrayOfString1 = new String[2];
    int i = 0;
    for(String elem : paramLinkedList)
    {
      if(i == paramInt) {
        String[] arrayOfString2 = elem.split(";");
        if (arrayOfString2.length < 3) break;
        String str2 = arrayOfString2[0];
        String str3 = arrayOfString2[1];
        arrayOfString1[0] = new String(str2);
        arrayOfString1[1] = new String(str3);
        break;
      }
      i++;
    }
    return arrayOfString1;
  }
  
  public class AsyncHttpTask extends AsyncTask<String, Void, Integer>
  {
    public AsyncHttpTask() {}
    
    protected Integer doInBackground(String... paramVarArgs)
    {
      Integer localInteger = Integer.valueOf(0);
      BufferedReader localBufferedReader;
      try
      {
        File localFile = new File(getFilesDir(), paramVarArgs[0]);
        StringBuilder localStringBuilder = new StringBuilder();
        localBufferedReader = new BufferedReader(new FileReader(localFile));
        for (;;)
        {
          String str = localBufferedReader.readLine();
          if (str == null) {
            break;
          }
          localStringBuilder.append(str);
          localStringBuilder.append('\n');
          list_string.add(str);
        }
        System.out.println("***Finish with LIST");
      }
      catch (Exception localException)
      {
        Log.d("RecyclerViewExample", localException.getLocalizedMessage());
        localException.printStackTrace();
        return localInteger;
      }
      Collections.reverse(list_string);
      System.out.println("***Reverse the LIST");
      parseResult(list_string);
      System.out.println("***Parsed the LIST");
      localInteger = Integer.valueOf(1);
      try {
        localBufferedReader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      System.out.println("***Finish doInBack");
      return localInteger;
    }
    
    protected void onPostExecute(Integer paramInteger)
    {
      setProgressBarIndeterminateVisibility(false);
      if (paramInteger.intValue() == 1)
      {
        System.out.println("***Einai result 1");
        adapter = new MyRecyclerAdapter(FeedListActivity.this, feedItemList);
        mRecyclerView.setAdapter(adapter);
        return;
      }
      System.out.println("***Einai result 0");
      Log.e("RecyclerViewExample", "Failed to fetch data!");
    }
    
    protected void onPreExecute()
    {
      FeedListActivity.this.setProgressBarIndeterminateVisibility(true);
    }
  }
}

