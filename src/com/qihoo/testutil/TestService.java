package com.qihoo.testutil;


import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qihoo.testutil.utils.CPUUtil;
import com.qihoo.testutil.utils.CSVUtil;
import com.qihoo.testutil.utils.FloatViewUtil;
import com.qihoo.testutil.utils.MemUtil;
import com.qihoo.testutil.utils.PowerUtil;
import com.qihoo.testutil.utils.TrafficUtil;

/**
 * @author penglu
 * 核心功能模块
 */
public class TestService extends Service {
	private final IBinder mBinder = new LocalBinder();
	private Handler myHandler = null;
	private DecimalFormat format;

	//广播接收器
	private SaveDataBroadCast mBroadCast;

	//各个工具类
	private FloatViewUtil mFloatViewUtil;
	private TrafficUtil mTrafficUtil;
	private PowerUtil mPowerUtil;
	private CSVUtil mLogInCSVUtil;
	
	//悬浮窗内的View
	private View viFloatingWindow;
	private TextView mMemTextView;
	private TextView mCPUTextView;
	private TextView mTrafficTextView;
	private TextView mPowerTextView;
	private Button btnStop;
	private Button btnRecord;
	private Button btnDismiss;
	public boolean isFloating=true;
	private PowerManager.WakeLock mWakeLock;
	
	private List<RunningAppProcessInfo> mRunApp;
	public ApplicationInfo mApp;
	private int[] pid;
	
	private int uid=-1;
	
	//记录检测结果的变量
	private String battetylevel="N/A";
	private String trafficUsage="N/A";
	private String cpuUsage="N/A";
	private String memUsage="N/A";
	private String memFree="N/A";
	private String currentUsage="N/A";
	private String temperature="N/A";
	private String voltage="N/A";
	private String costCurrent="N/A";
	private String costPower="N/A";
	private String totalCpuUsage="N/A";
	
	//与定时记录任务相关的便利
	private int span = 10000;
	private Timer mTimer;
	private TimerTask mTimerTask;
	
	//是否进行某项测试的flag标记,设置成public是为了方便MainActivity读取运行状态，如果为了更好的封装性可以设置setter和getter。
	public boolean mLogPower=false;
	public boolean mLogTraffic=false;
	public boolean mLogCPU=false;
	public boolean mLogMemory=false;
	
	
	private Notification mNotification;
	
	private Handler mHandler;
	
	public boolean mIsStart=false;
	private final int ONGOING_NOTIFICATION=10086;
	/*
	 * （非 Javadoc）
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		format=new DecimalFormat();
		format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		format.setGroupingUsed(false);
		format.setMaximumFractionDigits(2);
		format.setMinimumFractionDigits(0);
		initFloatView();
		mBroadCast=new SaveDataBroadCast();
		mLogInCSVUtil=new CSVUtil(getApplicationContext(), uid);
	}
	
	private void initFloatView()
	{
		if (isFloating) {
			viFloatingWindow = LayoutInflater.from(this).inflate(R.layout.floating, null);
			mCPUTextView = (TextView) viFloatingWindow.findViewById(R.id.used_mem);
			mMemTextView = (TextView) viFloatingWindow.findViewById(R.id.used_cpu);
			mTrafficTextView = (TextView) viFloatingWindow.findViewById(R.id.used_traffic);
			mPowerTextView = (TextView) viFloatingWindow.findViewById(R.id.used_prower);
			btnRecord = (Button) viFloatingWindow.findViewById(R.id.record);
			btnDismiss = (Button) viFloatingWindow.findViewById(R.id.dismiss);

			btnRecord.setText("立即刷新");
			mCPUTextView.setText(getString(R.string.calculating));
			mCPUTextView.setTextColor(android.graphics.Color.RED);
			mMemTextView.setTextColor(android.graphics.Color.RED);
			mTrafficTextView.setTextColor(android.graphics.Color.RED);
			mPowerTextView.setTextColor(android.graphics.Color.RED);
			btnStop = (Button) viFloatingWindow.findViewById(R.id.stop);
			btnStop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					stopTest();
				}
			});
			btnRecord.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
//					savingData("");
					Intent intent =new Intent();
					intent.setAction("com.qihoo.testutil.broadcast");
					intent.putExtra("label", "手动记录");
					sendBroadcast(intent);
				}
			});
			btnDismiss.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					isFloating=false;
					mFloatViewUtil.removeFloatView();
				}
			});
		}
		mFloatViewUtil=new FloatViewUtil(getApplicationContext(),viFloatingWindow);
	}
	public void setHandler(Handler handler)
	{
		mHandler=handler;		
	}

	public void setLogType( boolean mLogPower,boolean mLogTraffic,boolean mLogCPU,boolean mLogMemory)
	{
		this.mLogPower=mLogPower;
		this.mLogTraffic=mLogTraffic;
		this.mLogCPU=mLogCPU;
		this.mLogMemory=mLogMemory;
	}
	
	public void setApp(ApplicationInfo app)
	{
		if (app!=null) {
			mApp=app;			
			uid=mApp.uid;
		}
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mFloatViewUtil != null) {
			mFloatViewUtil.removeFloatView();
		}
	}

	public class LocalBinder extends Binder {
		TestService getService() {
			// 返回本service的实例到客户端，于是客户端可以调用本service的公开方法
			return TestService.this;
		}

		public void setHandler(Handler handler) {
			myHandler = handler;
		}
		
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	private void clearOldData()
	{
		battetylevel="N/A";
		currentUsage="N/A";
		cpuUsage="N/A";
		memUsage="N/A";
		memFree="N/A";
		trafficUsage="N/A";
		costCurrent="N/A";
		costPower="N/A";
		totalCpuUsage="N/A";
	}
	public void savingData(String tag)
	{
		mRunApp.clear();
		//找到被测app所开启的程序
		ActivityManager mActivityManager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> infos=mActivityManager.getRunningAppProcesses();
		for (RunningAppProcessInfo info:infos) {
			if (info.uid==mApp.uid) {
				mRunApp.add(info);
			}
		}
		pid=new int[mRunApp.size()];
		for (int i = 0; i < mRunApp.size(); i++) {
			pid[i]=mRunApp.get(i).pid;
		}
		//找不到程序在运行时停止测试
		if (mRunApp.isEmpty()) {
			mHandler.sendEmptyMessage(MainActivity.APPSTOP);
			stopTest();
			return;
		}
		if (mLogCPU) {
			List<String> cpu=CPUUtil.getProcessCpuRate(mRunApp);
			cpuUsage=cpu.get(0);
			totalCpuUsage=cpu.get(1);
			
			
			mCPUTextView.post(new Runnable() {
				
				@Override
				public void run() {
					mCPUTextView.setText("CPU占用率："+cpuUsage+"%"+"/ "+totalCpuUsage+"%");
				}
			});			
			
		}
		if (mLogPower) {
			if (!mLogCPU) {
				cpuUsage=100+"";
			}
			Map<String, String> map;
			if (!mLogCPU) {
				map=mPowerUtil.getBatteryInfo();
			}else {
				map=mPowerUtil.getBatteryInfo(Double.parseDouble(cpuUsage),Double.parseDouble(totalCpuUsage));
			}
			if (map==null) {
				currentUsage="---";
				battetylevel="---";
				temperature="---";
				voltage="---";
				costCurrent="---";
				costPower="---";
			}else {
				currentUsage=map.get("current");
				battetylevel=map.get("battery");
				temperature=map.get("temperature");
				voltage=map.get("voltage");
				costCurrent=map.get("currentcost");
				costPower=map.get("power");
			}
			
			mPowerTextView.post(new Runnable() {
				@Override
				public void run() {
					if (!mLogCPU) {
						mCPUTextView.setText("开启cpu检测，耗电检测结果更准确哦！");
					}
					mPowerTextView.setText("电流为"+currentUsage+"ma,电量为 "+battetylevel+"%"+",本程序消耗"+costCurrent);
				}
			});						
		}
		if (mLogMemory) {			
			memUsage=format.format(MemUtil.getProcessMemSize(getApplicationContext(), pid)/1024);
			memFree=format.format(MemUtil.getFreeMemorySize(getApplicationContext())/1024);
			mMemTextView.post(new Runnable() {
				
				@Override
				public void run() {
					mMemTextView.setText("内存使用量： "+memUsage+" MB,剩余内存:"+memFree);
				}
			});
						
		}
		
		if (mLogTraffic) {			
			trafficUsage=format.format((double)mTrafficUtil.getTraffic()/1024);
			mTrafficTextView.post(new Runnable() {
				@Override
				public void run() {
					mTrafficTextView.setText("总共消耗流量："+trafficUsage+" KB");
				}
			});			
		}
		StringBuilder sb=new StringBuilder();
		sb.append(memUsage);sb.append(CSVUtil.COMMA);
		sb.append(memFree);sb.append(CSVUtil.COMMA);
		sb.append(totalCpuUsage);sb.append(CSVUtil.COMMA);
		sb.append(cpuUsage);sb.append(CSVUtil.COMMA);
		sb.append(trafficUsage);sb.append(CSVUtil.COMMA);
		sb.append(battetylevel);sb.append(CSVUtil.COMMA);
		sb.append(currentUsage);sb.append(CSVUtil.COMMA);
		sb.append(temperature);sb.append(CSVUtil.COMMA);
		sb.append(voltage);sb.append(CSVUtil.COMMA);
		sb.append(costCurrent);sb.append(CSVUtil.COMMA);
		sb.append(costPower);sb.append(CSVUtil.COMMA);
		sb.append(tag);sb.append(CSVUtil.COMMA);
		mLogInCSVUtil.saveLog(sb.toString());
	}
	public void setFloatViewVisible(boolean visible)
	{
		isFloating=visible;
		if (mIsStart) {
			if (!mFloatViewUtil.isFloating&&isFloating) {
				mFloatViewUtil.createFloatingWindow();
				isFloating=true;
			}else if (mFloatViewUtil.isFloating&&!isFloating) {
				mFloatViewUtil.removeFloatView();
				isFloating=false;
			}
		}
	}
	public void startTest() throws IOException{
		if (!(mLogPower||mLogTraffic||mLogCPU||mLogMemory)) {
			Toast.makeText(getApplication(), "没有选定监控项目", Toast.LENGTH_SHORT).show();
			return;
		}
		
		//此wakelock是保持屏幕最大亮度显示的，目前测试地图才设置的，测试其他产品应该去掉
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag"); 
		
		mWakeLock.acquire(); 
		
		mRunApp=new ArrayList<ActivityManager.RunningAppProcessInfo>();
		mTrafficUtil=new TrafficUtil(uid);
		mPowerUtil=new PowerUtil(getApplicationContext());
		mIsStart=true;
		
		mLogInCSVUtil.createResultCsv(mApp.loadLabel(getPackageManager()).toString());
		mTimer=new Timer();
		mTimerTask=new TimerTask() {			
			@Override
			public void run() {
				savingData("");
				if (myHandler!=null) {
					myHandler.sendEmptyMessage(0);
				}
				
			}
		};
		mTimer.schedule(mTimerTask, span,span);
		
		//通知栏显示测试状态，并将service设置成前台服务，防止被杀死
		mNotification=new Notification();
		mNotification.icon=R.drawable.ic_launcher;
		mNotification.tickerText="测试开始啦！";
		mNotification.when=System.currentTimeMillis();
		Intent notificationIntent = new Intent(this, MainActivity.class);  
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);  
		mNotification.setLatestEventInfo(this, "测试运行中",  
		        "testing", pendingIntent);  
		startForeground(ONGOING_NOTIFICATION, mNotification); 
		NotificationManager notificationManager;
		registBroadcastRcv();
		if (isFloating) {
			mFloatViewUtil.createFloatingWindow();
		}		
	}
	
	public void stopTest() {
		try {
			mIsStart=false;
			mRunApp=null;
			mTimerTask.cancel();
			mTimer.cancel();
			mLogInCSVUtil.close();
			mCPUTextView.setText("");
			mMemTextView.setText("");
			mTrafficTextView.setText("");
			mPowerTextView.setText("");
			stopForeground(true);
			clearOldData();
			if (mFloatViewUtil != null) {
				mFloatViewUtil.removeFloatView();
			}
			if (mWakeLock!=null) {
				mWakeLock.release();
			}
			unregisterReceiver(mBroadCast);
			if (mHandler!=null) {
				mHandler.sendEmptyMessage(MainActivity.STOPTEST);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setSpan(int span)
	{
		this.span=span;
	}
	
	private void registBroadcastRcv()
	{
		IntentFilter intentFilter =new IntentFilter();
		intentFilter.addAction("com.qihoo.testutil.broadcast");
		registerReceiver(mBroadCast, intentFilter);
	}
	
	private class SaveDataBroadCast extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO 自动生成的方法存根
			if (intent.getAction().equals("com.qihoo.testutil.broadcast")) {
				String tag=intent.getStringExtra("label");
				savingData(tag);
			}
		}
		
	}
}