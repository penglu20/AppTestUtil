# AppTestUtil
An app to monitor other app's working, including CPU,memory,traffic,battery.

一个简单的工具，可以记录某个app的使用情况，包括耗电量（包括实时电流、电压、温度），cpu使用率，内存使用量，使用的流量等，并可以显示悬浮窗展示这些数据。






内存使用量：

int[] pid={mRunApp.pid};

ActivityManager mActivityManager=
(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

Debug.MemoryInfo[] outInfo=mActivityManager.getProcessMemoryInfo(pid);

memUsage=outInfo[0].getTotalPss();





流量消耗：

trafficUsage=TrafficStats.getUidRxBytes(mUid);
trafficUsage+=TrafficStats.getUidTxBytes(mUid);






CPU使用率：总的cpu使用率
1、在t1和t2时刻，分别读取/proc/stat文件：
(user、nice、system、idle、iowait、irq、softirq、stealstolen、guest)的9元组；

2、计算总的Cpu时间片totalCpuTime
a)  把t1的所有cpu使用情况求和，得到s1；
b)  把t2的所有cpu使用情况求和，得到s2；
c)  s2 - s1得到这个时间间隔内的所有时间片，即totalCpuTime = s2 –s1；

3、计算空闲时间idle
idle=t2的第四列 – t1的第四列；

4、计算cpu在t2与t1之间的平均使用率：
cpu =100* (total-idle)/total








CPU使用率：某一进程Cpu使用率
计算方法：  
1．在t1和t2时刻，读取/proc/stat和/proc/pid/stat文件，
a) /proc/stat 中的user、nice、system、idle、iowait、irq、softirq、stealstolen、guest；
b) /proc/pid/stat 中的utime，stime，cutime，cstime；

2.分别根据计算出两个时刻的总的时间片与进程的时间片，
分别记作：totalCpuTime1，totalCpuTime2，processCpuTime1，processCpuTime2

3．计算该进程的cpu在t2与t1之间的平均使用率
pcpu = 100*( processCpuTime2 – processCpuTime1) / (totalCpuTime2 – totalCpuTime1);





统计耗电量的方法是：
power_profile.xml
官方文档表明：OEM厂商应该有自己的power_profile.xml，因为部件（如：cpu, wifi…）耗电量应与具体硬件相关，这个只有OEM厂商清楚


其中总的电流是通过读取“/sys/class/power_supply/battery/current_now”文件内容获取的



系统设置中的耗电统计：
总耗电 = CPU耗电 + Wake Lock耗电 + 移劢数据耗电 + WIFI耗电 + 传感器耗电

CPU耗电 = 各频率使用时间 * 各频率单位时间耗电

Wake Lock耗电 = wake lock持有时间 * CPU 唤醒下单位时间耗电（只算PARTIAL_WAKE_LOCK 类型）

移劢数据耗电 = （移劢上行数据 + 移劢下行数据） * 移劢每字节单位耗电

WIFI耗电 = （ WIFI上行数据 + WIFI下行数据） * WIFI每字节单位耗电 + Wi-Fi

使用时间 * WIFI保持单位时间耗电 + WIFI scan时间 * WIFI scan单位时间耗电

传感器耗电 = 每种传感器使用时间 * 每种传感器单位时间耗电

（上一次拔掉设备后 ~ 至今） 的App耗电量统计 


某一程序耗电量：
1.取t1和t2时刻，按照前面所述的方法，计算出整体cpu使用率和程序cpu使用率，记为tcpu和pcpu；
2.读取整个系统的电流I；
3.从power_profile.xml中取得各个硬件的耗电量In；
4.判断每个硬件是否开启，如果开启，则从I中减去In；
5.最后得到的结果是Cpu的耗电量Ic；
6.这个程序的电流Ip=Ic*pcpu/tcpu；












同时会将这些数据保存到cvs文件中，方便统计。

暂时没有做本地展示cvs文件的功能，界面也非常简陋，有空再优化。
