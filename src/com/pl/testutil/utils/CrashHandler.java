package com.pl.testutil.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.TreeSet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.pl.testutil.TestApplication;
import com.pl.testutil.R;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 * 
 * @author penglu
 */
public class CrashHandler implements UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;
    private static CrashHandler INSTANCE;

    /** 使用Properties来保存设备的信息和错误堆栈信息 */
    private Properties mDeviceCrashInfo = new Properties();
    public static final String STACK_TRACE = "STACK_TRACE";
    public static final String STACK_DATE = "STACK_DATE";
    public static final String STACK_VERSION_CODE = "STACK_VERSION_CODE";
    public static final String STACK_VERSION_NAME = "STACK_VERSION_NAME";
    public static final String STACK_PROCESS_NAME = "STACK_PROCESS_NAME";
    public static final String CRASH_REPORTER_EXTENSION = ".cr";
    public static String SAVE_FILE_PATH = Environment.getExternalStorageDirectory() + "/testutil/";
    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) || mDefaultHandler == null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            // Sleep一会后结束程序
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }

            		saveCrashInfoToFile(ex);
            		saveCrashInfoToFileForDebug(ex);
            if (ex != null)
                ex.printStackTrace();

            android.os.Process.killProcess(android.os.Process.myPid());
            return true;
    }

    /**
     * 在程序启动时候, 可以调用该函数来发送以前没有发送的报告
     */
    public void sendPreviousReportsToServer() {
        sendCrashReportsToServer(mContext);
    }

    /**
     * 把错误报告发送给服务器,包含新产生的和以前没发送的.
     * 
     * @param ctx
     */
    private void sendCrashReportsToServer(Context ctx) {
        String[] crFiles = getCrashReportFiles(ctx);
        if (crFiles != null && crFiles.length > 0) {
            TreeSet<String> sortedFiles = new TreeSet<String>();
            try {
                sortedFiles.addAll(Arrays.asList(crFiles));
            } catch (Exception e) { //java.lang.NoSuchMethodError: asList
                for (String str : crFiles) {
                    sortedFiles.add(str);
                }
            }
            String lastName = sortedFiles.last();
            File cr = new File(ctx.getFilesDir(), lastName);

            for (String fileName : sortedFiles) {
                File file = new File(ctx.getFilesDir(), fileName);
                file.delete();// 删除已发送的报告
            }
        }
    }

    

    /**
     * 获取错误报告文件名
     * 
     * @param ctx
     * @return
     */
    private String[] getCrashReportFiles(Context ctx) {
        File filesDir = ctx.getFilesDir();
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(CRASH_REPORTER_EXTENSION);
            }
        };
        return filesDir.list(filter);
    }

    /**
     * 保存错误信息到文件中
     * 
     * @param ex
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    private String saveCrashInfoToFile(Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);

        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        String result = info.toString();
        printWriter.close();
        mDeviceCrashInfo.put(STACK_TRACE, result);
        mDeviceCrashInfo.put(STACK_DATE, new SimpleDateFormat("yyyyMMddHHMMss").format(new Date()));
        String processName = TestApplication.getInstance().getString(R.string.app_name);
        if (TextUtils.isEmpty(processName)) {
            processName = "unknown";
        }
        mDeviceCrashInfo.put(STACK_PROCESS_NAME, processName);
        try {
            String fileName = "crash-" + System.currentTimeMillis() + "-maround"
                    + CRASH_REPORTER_EXTENSION;
            FileOutputStream trace = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            mDeviceCrashInfo.store(trace, "");
            trace.flush();
            trace.close();
            return fileName;
        } catch (Exception e) {
        }
        return null;
    }

    @SuppressLint("SimpleDateFormat")
    private void saveCrashInfoToFileForDebug(Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);

        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        String result = info.toString();
        printWriter.close();
        mDeviceCrashInfo.put(STACK_TRACE, result);
        mDeviceCrashInfo.put(STACK_DATE, new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        String processName = TestApplication.getInstance().getString(R.string.app_name);
        if (TextUtils.isEmpty(processName)) {
            processName = "unknown";
        }
        mDeviceCrashInfo.put(STACK_PROCESS_NAME, processName);
        try {
            String debugCrashDir = SAVE_FILE_PATH + "/crash/";
            File dir = new File(debugCrashDir);
            if(!dir.isDirectory()) {
                dir.mkdirs();
            }
            String debugCrashFile = debugCrashDir + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".txt";
            OutputStream outputStream = null;
            try {
                File file = new File(debugCrashFile);
                outputStream = new FileOutputStream(file);
                mDeviceCrashInfo.store(outputStream, "");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(outputStream != null) {
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
        }
        return;
    }

    

}
