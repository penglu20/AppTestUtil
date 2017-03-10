package com.pl.testutil.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import com.pl.testutil.TestApplication;

public class FloatViewUtil 
{
	private Context mContext;
	private WindowManager windowManager = null;
	private WindowManager.LayoutParams wmParams = null;
	private View viFloatingWindow;
	private float mTouchStartX;
	private float mTouchStartY;
	private float x;
	private float y;
	public boolean isFloating;
	public FloatViewUtil(Context context,View view)
	{
		mContext=context;
		viFloatingWindow=view;
	}
	public View createFloatingWindow() {		
		SharedPreferences shared = mContext.getSharedPreferences("float_flag", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		editor.putInt("float", 1);
		editor.commit();
		windowManager = (WindowManager) mContext.getSystemService("window");
		wmParams = ((TestApplication)mContext).getMywmParams();
		wmParams.type = 2002;
		wmParams.flags |= 8;
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		wmParams.x = 0;
		wmParams.y = 0;
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.format = 1;
		windowManager.addView(viFloatingWindow, wmParams);
		viFloatingWindow.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				x = event.getRawX();
				y = event.getRawY() - 25;
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mTouchStartX = event.getX();
					mTouchStartY = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					updateViewPosition();
					break;
				case MotionEvent.ACTION_UP:
					updateViewPosition();
					// showImg();
					mTouchStartX = mTouchStartY = 0;
					break;
				}
				return true;
			}
		});
		isFloating=true;
		return viFloatingWindow;
		
	}
	
	private void updateViewPosition() {
		wmParams.x = (int) (x - mTouchStartX);
		wmParams.y = (int) (y - mTouchStartY);
		if (viFloatingWindow != null) {
			windowManager.updateViewLayout(viFloatingWindow, wmParams);
		}
	}
	
	public void removeFloatView()
	{
		if (windowManager != null) {
			if (isFloating) {
				windowManager.removeView(viFloatingWindow);
				isFloating=false;
			}
		}
	}
}