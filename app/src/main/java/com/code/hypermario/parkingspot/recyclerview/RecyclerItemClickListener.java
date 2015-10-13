package com.code.hypermario.parkingspot.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener
{
  GestureDetector mGestureDetector;
  private OnItemClickListener mListener;
  
  public RecyclerItemClickListener(Context paramContext, OnItemClickListener paramOnItemClickListener)
  {
    this.mListener = paramOnItemClickListener;
    this.mGestureDetector = new GestureDetector(paramContext, new SimpleOnGestureListener()
    {
      public boolean onSingleTapUp(MotionEvent paramAnonymousMotionEvent)
      {
        return true;
      }
    });
  }
  
  public boolean onInterceptTouchEvent(RecyclerView paramRecyclerView, MotionEvent paramMotionEvent)
  {
    View localView = paramRecyclerView.findChildViewUnder(paramMotionEvent.getX(), paramMotionEvent.getY());
    if ((localView != null) && (this.mListener != null) && (this.mGestureDetector.onTouchEvent(paramMotionEvent)))
    {
      this.mListener.onItemClick(localView, paramRecyclerView.getChildPosition(localView));
      return true;
    }
    return false;
  }
  
  public void onRequestDisallowInterceptTouchEvent(boolean paramBoolean) {}
  
  public void onTouchEvent(RecyclerView paramRecyclerView, MotionEvent paramMotionEvent) {}
  
  public static abstract interface OnItemClickListener
  {
    public abstract void onItemClick(View paramView, int paramInt);
  }
}

