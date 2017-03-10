package com.pl.testutil.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

public class MemUtil
{
	public static double getFreeMemorySize(Context context) {
		ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		am.getMemoryInfo(outInfo);
		double avaliMem = outInfo.availMem;
		return avaliMem / 1024;
	}
	public static double getProcessMemSize(Context context, int[] pid)
	{
		ActivityManager mActivityManager=(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);			
		Debug.MemoryInfo[] outInfo=mActivityManager.getProcessMemoryInfo(pid);
		long mem=0;
		for (int i = 0; i < outInfo.length; i++) {
			mem+=outInfo[i].getTotalPss();				
		}
		return mem;
	}
}