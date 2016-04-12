package com.qihoo.testutil.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.util.Log;

public class CPUUtil {
	public static List<RunningAppProcessInfo> mApp;
	public static long totalCpuTime=-1;
	public static long totalCpuBusyTime=-1;
	public static long appCpuTime=-1;
	private static long totalCpu;
	private static long totalCpuBusy;

	public static List<String> getProcessCpuRate(List<RunningAppProcessInfo> app) {
		mApp = app;
		long totalCpuTime2; 
		long totalCpuBusy2;
		long processCpuTime2; 
		if (totalCpuTime==-1) {
			getTotalCpuTime();
			totalCpuTime=totalCpu;
			totalCpuBusyTime=totalCpuBusy;
			appCpuTime=getAppCpuTime();		
			try {
				Thread.sleep(360);
			} catch (Exception e) {
			}
			getTotalCpuTime();
			totalCpuTime2 = totalCpu;
			totalCpuBusy2 = totalCpuBusy;
			processCpuTime2 = getAppCpuTime();
		}else {
			getTotalCpuTime();
			totalCpuTime2 = totalCpu;
			totalCpuBusy2 = totalCpuBusy;
			processCpuTime2 = getAppCpuTime();
		}		

		float cpuRate = 100.0f * (processCpuTime2 - appCpuTime)
				/ (totalCpuTime2 - totalCpuTime);
		if (cpuRate<0.0) {
			Log.d("hpp_pl", "processCpuTime2="+processCpuTime2+",appCpuTime="+appCpuTime+",totalCpuTime2="+totalCpuTime2+
					",totalCpuTime="+totalCpuTime);
			cpuRate=0;
		}
		float totalcpuRate= 100.0f*(totalCpuBusy2-totalCpuBusyTime)/(totalCpuTime2 - totalCpuTime);
		if (totalcpuRate<0.0) {
			totalcpuRate=0;
		}
		totalCpuTime=totalCpuTime2;
		totalCpuBusyTime=totalCpuBusy2;
		appCpuTime=processCpuTime2;
		List<String> rtnList=new ArrayList<String>();
		rtnList.add(String.format("%#.2f", cpuRate));
		rtnList.add(String.format("%#.2f", totalcpuRate));
		return rtnList;
	}

	public static void getTotalCpuTime() { // 获取系统总CPU使用时间
		String[] cpuInfos = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream("/proc/stat")), 1000);
			String load = reader.readLine();
			reader.close();
			cpuInfos = load.split(" ");
		totalCpu = Long.parseLong(cpuInfos[2])
				+ Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
				+ Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
				+ Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
		totalCpuBusy=Long.parseLong(cpuInfos[2])
				+ Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
				+ Long.parseLong(cpuInfos[6]) 
				+ Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static long getAppCpuTime() { // 获取应用占用的CPU时间
		String[] cpuInfos = null;
		long multyProcCpuTime=0;
		for (RunningAppProcessInfo app:mApp) {
			try {
				int pid = app.pid;
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						new FileInputStream("/proc/" + pid + "/stat")), 1000);
				String load = reader.readLine();
				reader.close();
				cpuInfos = load.split(" ");
			long appCpuTime = Long.parseLong(cpuInfos[13])
					+ Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
					+ Long.parseLong(cpuInfos[16]);
			multyProcCpuTime+=appCpuTime;
			} catch (Exception ex) {
				ex.printStackTrace();
				multyProcCpuTime+=0;
			}
		}
		return multyProcCpuTime;
	}
}