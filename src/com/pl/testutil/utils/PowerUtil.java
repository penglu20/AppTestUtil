package com.pl.testutil.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;


public class PowerUtil {
	private Context mContext;
//	private PowerProfile mPowerProfile;
	private Class mPowerProfile; //mPowerProfile是com.android.internal.os.PowerProfile类的实例，在源码中是hide的，因此只能通过反射调用。
	private PowerManager powerManager;
	private LocationManager locationManager;
//	private BluetoothService bluetoothService;
	private TelephonyManager telephonyManager;
	private ConnectivityManager connectivityManager;
	private HashMap<String, String> powerProfileFields=new HashMap<String, String>();
	

	private boolean isWifiConnect = false;
	private boolean isMobileConnect = false;
	
	private long lastTime;

	public PowerUtil(Context context) {
		mContext = context;
		lastTime=System.currentTimeMillis();
		try {
			mPowerProfile = Class.forName("com.android.internal.os.PowerProfile");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (mPowerProfile==null) {
			return;
		}
//		mPowerProfile = new PowerProfile(context);
		locationManager = (LocationManager) mContext
				.getSystemService(Service.LOCATION_SERVICE);
//		bluetoothService = new BluetoothService(mContext);
		telephonyManager = (TelephonyManager) mContext
				.getSystemService(Service.TELEPHONY_SERVICE);
		connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Service.CONNECTIVITY_SERVICE);
		powerManager = (PowerManager) mContext
				.getSystemService(Service.POWER_SERVICE);
		Field[] fields = mPowerProfile.getClass().getFields();
		for (Field field : fields) {
			if (Modifier.isStatic(fields[3].getModifiers())) {
				double screen;
				try {
					screen = getAveragePower((String) field
							.get(mPowerProfile));
					Log.d("hpp_pl", field.getName() + "=" + screen);
					powerProfileFields.put(field.getName(), (String) field.get(mPowerProfile));
				} catch (IllegalAccessException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}

		}
	}

	public Map<String, String> getBatteryInfo() 
	{
		return getBatteryInfo(1, 1);
	}
	public Map<String, String> getBatteryInfo(double cpuRate,double totalCpuUsage) {
		if (mPowerProfile==null) {
			return null;
		}
		long time=System.currentTimeMillis()-lastTime;
		lastTime=System.currentTimeMillis();
		long current=CurrentInfo.getCurrentValue();
		Map<String, String> map = new HashMap<String, String>();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		Intent intent = mContext.registerReceiver(null, intentFilter);
		if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
			// 获取当前电量
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			// 电量的总刻度
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
			// 把它转成百分比
			String voltage = String.valueOf(intent.getIntExtra(
					BatteryManager.EXTRA_VOLTAGE, -1) * 1.0);
			String temperature = String.valueOf(intent.getIntExtra(
					BatteryManager.EXTRA_TEMPERATURE, -1) * 1.0 / 10);
			map.put("battery", ((level * 100) / scale) + "");
			map.put("voltage", voltage + "");
			map.put("temperature", temperature + "");
			map.put("current", current + "");
			double currentcost=processPower(current, cpuRate/totalCpuUsage);
			double power;
			map.put("currentcost", String.format("%#.2f",currentcost));
			power=currentcost*Double.parseDouble(voltage)*time/1000;
			map.put("power", String.format("%#.6f",power));

			return map;
		}
		return map;
	}

	private double processPower(double current,double cpuRate) {
		double cost=0;
		// 如果开启gps，则在耗电中除去
		if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
			cost+=getAveragePower(powerProfileFields.get("PowerProfile.POWER_GPS_ON"));
		}	
		// 如果开启wifi，则在耗电中除去
		if (connectivityManager != null) {
			NetworkInfo[] networkInfos = connectivityManager
					.getAllNetworkInfo();
			for (int i = 0; i < networkInfos.length; i++) {
				if (networkInfos[i].getState() == NetworkInfo.State.CONNECTED) {
					if (networkInfos[i].getType() == ConnectivityManager.TYPE_MOBILE) {
						isMobileConnect = true;
					}
					if (networkInfos[i].getType() == ConnectivityManager.TYPE_WIFI) {
						isWifiConnect = true;
					}
				}
			}
		}
		// 如果开启wifi，则在耗电中除去
		if (isWifiConnect) {
			cost+=getAveragePower(powerProfileFields.get("PowerProfile.POWER_WIFI_ON"))+
					getAveragePower(powerProfileFields.get("PowerProfile.POWER_WIFI_ACTIVE"));
		}
		// 如果使用手机流量，则在耗电中除去
		if (isMobileConnect) {
			cost+=getAveragePower(powerProfileFields.get("PowerProfile.POWER_RADIO_ACTIVE"));
		}
		// 如果接入了手机网络，则在耗电中除去
		if (telephonyManager.getNetworkType() != TelephonyManager.PHONE_TYPE_NONE) {
			cost+=getAveragePower(powerProfileFields.get("PowerProfile.POWER_RADIO_ON"));
		}
//		// 如果开启蓝牙，则在耗电中除去
//		if (bluetoothService.isEnabled()) {
//			cost+=mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON);
//		}
		//如果开启屏幕，则在耗电中除去
		if (powerManager.isScreenOn()) {
			cost+=getAveragePower(powerProfileFields.get("PowerProfile.POWER_SCREEN_FULL"));
		}
		double cpuCost;		
		if (current>0) {
			cpuCost=current-cost;		
		}else {
			cpuCost=current+cost;		
			
		}
		return cpuCost*cpuRate;
	}	
	private Method mGetAveragePower;
	private double getAveragePower(String type){
		if (mGetAveragePower==null) {
			try {
				mGetAveragePower=mPowerProfile.getDeclaredMethod("getAveragePower", String.class);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (mGetAveragePower==null) {
			return 0;
		}
		try {
			return (Double) mGetAveragePower.invoke(mPowerProfile, type);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	
	
}