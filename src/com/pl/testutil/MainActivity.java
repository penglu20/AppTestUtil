package com.pl.testutil;


import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pl.testutil.TestService.LocalBinder;

/*
 * x=longtitude,经度
 * y=latitude,维度
 * */
public class MainActivity extends Activity {
	private static final int CHOOSEAPP=10010;
	
	public static final int APPSTOP=10086;
	public static final int STOPTEST=20086;
	
	public ApplicationInfo mApp;
	
	private TestService mService;
	private MyServiceConnection myServiceConnection;
	private boolean mBound = false;

	
	private TextView mRunInfromTextView;
	private CheckBox mPowerCheckBox;
	private CheckBox mTrafficCheckBox;
	private CheckBox mCPUCheckBox;
	private CheckBox mMemoryCheckBox;
	private CheckBox mFloatCheckBox;
	private EditText mSpanEditText;
	private Button mStartButton;
	private Button mChooseButton;

	private boolean isStart = false;
	
	public boolean mLogPower=true;
	public boolean mLogTraffic=true;
	public boolean mLogCPU=true;
	public boolean mLogMemory=true;
	public boolean mIsFloat=true;
	
	private int mSpan=3000;
	

	private Handler mHandler=new Handler()
	{
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case APPSTOP:
				Toast.makeText(getApplication(), "被测程序已经停止运行", Toast.LENGTH_SHORT).show();
				refresh();
				break;
			case STOPTEST:
				refresh();
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		myServiceConnection=new MyServiceConnection();
		Intent intent = new Intent(this, TestService.class);
		startService(intent);
		initView();
		initButton();		
	}
	
	private void initView()
	{
		mRunInfromTextView=(TextView) findViewById(R.id.run_inform);
		mPowerCheckBox = (CheckBox) findViewById(R.id.power);
		mTrafficCheckBox = (CheckBox) findViewById(R.id.traffic);
		mCPUCheckBox = (CheckBox) findViewById(R.id.cpu);
		mMemoryCheckBox = (CheckBox) findViewById(R.id.memorry);
		mFloatCheckBox = (CheckBox) findViewById(R.id.is_float);
		mStartButton=(Button) findViewById(R.id.start);		
		mChooseButton=(Button) findViewById(R.id.choose);
		mSpanEditText=(EditText) findViewById(R.id.span);

	}
	private void initButton()
	{
		mChooseButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this,ChooseAppActivity.class);
				startActivityForResult(intent, CHOOSEAPP);
				//上面的方法会调起intent中设置的activity，而在调起的activity中可以设置setresult，
				//结束新activity后，则会回到本activity的onactivityresult中处理。
			}
		});
		mPowerCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mLogPower=isChecked;
			}
		});
		mTrafficCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mLogTraffic=isChecked;
			}
		});
		mCPUCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mLogCPU=isChecked;
			}
		});
		mMemoryCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mLogMemory=isChecked;
			}
		});
		mFloatCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mIsFloat=isChecked;
				if (mService.mIsStart) {
					mService.setFloatViewVisible(mIsFloat);
				}
			}
		});
		mStartButton.setOnClickListener(new OnClickListener() {
			
			@TargetApi(Build.VERSION_CODES.GINGERBREAD)
			@Override
			public void onClick(View v) {
				if (mService!=null) {
					isStart = mService.mIsStart;
					if (isStart) {//如果有测试正在运行，则停止测试，并恢复flag
						mService.stopTest();
						mStartButton.setText("开始测试");
						mService.setLogType(false, false, false, false);
						isStart=mService.mIsStart;
						refresh();
					}else {//如果当前没有测试正在运行，则设置参数，并启动测试
						mService.setLogType(mLogPower, mLogTraffic, mLogCPU, mLogMemory);
						if (!mSpanEditText.getText().toString().isEmpty()) {
							mSpan=Integer.valueOf(mSpanEditText.getText().toString());
						}
						mService.setSpan(mSpan);							
						if (mApp==null) {
							Toast.makeText(getApplication(), "请选择一个应用", Toast.LENGTH_SHORT).show();
							return;
						}
						mService.setApp(mApp);
						mService.setFloatViewVisible(mIsFloat);
						try {
							
							mService.startTest();
							isStart=mService.mIsStart;
							if (isStart) {
								refresh();
								doStartApplicationWithPackageName(mApp.packageName);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==CHOOSEAPP) {
			if (data!=null) {
				ApplicationInfo applicationInfo=data.getExtras().getParcelable("app");
				if (applicationInfo!=null) {
					mApp=applicationInfo;
					refresh();
				}
			}
		}
	}
	
	
	private void doStartApplicationWithPackageName(String packagename) {

		Intent intent=getPackageManager().getLaunchIntentForPackage(mApp.packageName);		
		startActivity(intent);
		
		//下面这种方法也可以调起app，保留做学习用
		// 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
//		PackageInfo packageinfo = null;
//		try {
//			packageinfo = getPackageManager().getPackageInfo(packagename, 0);
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
//		if (packageinfo == null) {
//			return;
//		}
//
//		// 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
//		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
//		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//		resolveIntent.setPackage(packageinfo.packageName);
//
//		// 通过getPackageManager()的queryIntentActivities方法遍历
//		List<ResolveInfo> resolveinfoList = getPackageManager()
//				.queryIntentActivities(resolveIntent, 0);
//
//		ResolveInfo resolveinfo = resolveinfoList.iterator().next();
//		if (resolveinfo != null) {
//			// packagename = 参数packname
//			String packageName = resolveinfo.activityInfo.packageName;
//			// 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
//			String className = resolveinfo.activityInfo.name;
//			// LAUNCHER Intent
//			Intent intent = new Intent(Intent.ACTION_MAIN);
//			intent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//			// 设置ComponentName参数1:packagename参数2:MainActivity路径
//			ComponentName cn = new ComponentName(packageName, className);
//
//			intent.setComponent(cn);
//			startActivity(intent);
//		}
		
	}
	private void refresh()
	{
		if (mService.mIsStart) {
			mRunInfromTextView.setText("目前正在测试：" + mService.mApp.loadLabel(getPackageManager())+"的"
					+ (mService.mLogPower?"耗电 ":"")
					+(mService.mLogCPU?"CPU ":"")
					+(mService.mLogMemory?"内存 ":"")
					+(mService.mLogTraffic?"流量 ":""));
			mStartButton.setText("测试进行中，点击停止");
			mPowerCheckBox.setChecked(mService.mLogPower);
			mTrafficCheckBox.setChecked(mService.mLogTraffic);
			mCPUCheckBox.setChecked(mService.mLogCPU);
			mMemoryCheckBox.setChecked(mService.mLogMemory);
			mFloatCheckBox.setChecked(mService.isFloating);
		}else {
			mRunInfromTextView.setText("目前没有任务在执行");
			mStartButton.setText("开始测试");
		}
		if (mApp!=null) {
			mChooseButton.setText("已选择: "+mApp.loadLabel(getPackageManager()));
		}		
	}
	@Override
	protected void onPause() {
		unBindService();
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		bindService();
	}
	@Override
	protected void onDestroy()
	{
		//activity可能被销毁，但是在测试运行的过程中，不应该杀死测试后台service
		if (!mService.mIsStart) {
			mService.stopSelf();
		}
		super.onDestroy();
	}
	private void bindService()
	{
		Intent intent = new Intent(this, TestService.class);  
        bindService(intent, myServiceConnection, Context.BIND_AUTO_CREATE); 
	}
	private void unBindService()
	{
		if (mBound) {
			unbindService(myServiceConnection);
		}
	}
	class MyServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
			mService.setHandler(mHandler);
			isStart = mService.mIsStart;
			refresh();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			mBound = false;
		}

	}
    
}
