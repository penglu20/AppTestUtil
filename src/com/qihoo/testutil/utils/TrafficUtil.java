package com.qihoo.testutil.utils;

import android.net.TrafficStats;

public class TrafficUtil 
{
	private int mUid=-1;
	private long oldTraffic=0;
	private long trafficUsage=0;
	public TrafficUtil(int uid)
	{
		mUid=uid;
		if (uid!=-1) {
			oldTraffic=TrafficStats.getUidRxBytes(uid);
			oldTraffic+=TrafficStats.getUidTxBytes(uid);
		}
	}
	public long getTraffic()
	{
		if (mUid!=-1) {
			trafficUsage=TrafficStats.getUidRxBytes(mUid);
			trafficUsage+=TrafficStats.getUidTxBytes(mUid);
			trafficUsage-=oldTraffic;
		}
		return trafficUsage;
	}
}