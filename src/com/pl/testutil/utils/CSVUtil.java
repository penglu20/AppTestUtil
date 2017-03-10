package com.pl.testutil.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Environment;

import com.pl.testutil.R;

public class CSVUtil {
	public static final String NA = "N/A";
	public static final String COMMA = ",";
	public static final String LINE_END = "\r\n";
	public static final String COLON = ":";
	
	private Context mContext;

	private FileOutputStream out;
	private OutputStreamWriter osw;
	private BufferedWriter bw;
	private String resultFilePath;
	
	public CSVUtil(Context context, int uid) {
		mContext=context;
		resultFilePath=Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/testutil";
		
		
	}
	public void close()
	{
		try {
			bw.close();
			osw.close();
			out.close();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	public void createResultCsv(String name) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		Date date = new Date(System.currentTimeMillis());
		String mFileTime=df.format(date);
		if (!new File(resultFilePath).exists()) {
			new File(resultFilePath).mkdirs();
		}
		File resultFile = new File(resultFilePath);
		resultFile=new File(resultFilePath,"result"+mFileTime+"_"+name+".csv");
		try {
			resultFile.createNewFile();
			out = new FileOutputStream(resultFile);
			osw = new OutputStreamWriter(out,"GBK");
			bw = new BufferedWriter(osw);			
//			bw.write(mContext.getString(R.string.process_package) + COMMA
//					+ packageName + LINE_END
//					+ mContext.getString(R.string.process_name) + COMMA
//					+ processName + LINE_END
//					+ mContext.getString(R.string.process_pid) + COMMA + pid
//					+ LINE_END + mContext.getString(R.string.mem_size)
//					+ COMMA + totalMemory + "MB" + LINE_END
//					+ mContext.getString(R.string.cpu_type) + COMMA
//					+ cpuInfo.getCpuName() + LINE_END
//					+ mContext.getString(R.string.android_system_version)
//					+ COMMA + memoryInfo.getSDKVersion()
//					+ LINE_END + mContext.getString(R.string.mobile_type)
//					+ COMMA + memoryInfo.getPhoneType()
//					+ LINE_END + "UID" + COMMA + uid
//					+ LINE_END);

			bw.write(mContext.getString(R.string.timestamp) 
					+ COMMA	+ mContext.getString(R.string.used_mem_PSS)
					+ COMMA	+ mContext.getString(R.string.mobile_free_mem)
					+ COMMA + mContext.getString(R.string.total_used_cpu_ratio)					
					+ COMMA + mContext.getString(R.string.app_used_cpu_ratio)					
					+ COMMA + mContext.getString(R.string.traffic)
					+ COMMA + mContext.getString(R.string.battery)
					+ COMMA + mContext.getString(R.string.current)
					+ COMMA + mContext.getString(R.string.temperature)
					+ COMMA + mContext.getString(R.string.voltage)
					+ COMMA + mContext.getString(R.string.cost)
					+ COMMA + mContext.getString(R.string.power)
					+ COMMA + mContext.getString(R.string.backup)
					+ LINE_END);
		} catch (IOException e) {
		}
	}
	public void saveLog(String str)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		Date date = new Date(System.currentTimeMillis());
		String mFileTime=df.format(date);
		try {
			bw.write(mFileTime+COMMA+str+LINE_END);
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}

}