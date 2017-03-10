package com.pl.testutil;

import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppAdapter extends BaseAdapter
{

	private  List<ApplicationInfo> mApplicationInfos;
	private Context mContext;
	
	public AppAdapter(Context context)
	{
		mContext=context;
	}
	
	public void setAppList(List<ApplicationInfo> list)
	{
		mApplicationInfos=list;
	}
	@Override
	public int getCount() {
		// TODO 自动生成的方法存根
		return mApplicationInfos.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO 自动生成的方法存根
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO 自动生成的方法存根
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// convertView是复用的View，比如：当listView的一个item1向上划出屏幕的时候，则最下面新加的一个item2会复用划出去的这个View
		//则此时传入的convertView就是item1的View，如果没有可以复用的View，则传入null
		
		ViewHolder holder;
		if (convertView==null) {
			convertView=LayoutInflater.from(mContext).inflate(R.layout.item_choose_app, null);
			holder=new ViewHolder();
			holder.mAppName=(TextView) convertView.findViewById(R.id.app_name);
			holder.mAppIcon=(ImageView) convertView.findViewById(R.id.app_icon);
			holder.mPackageName=(TextView) convertView.findViewById(R.id.package_name);
			//将viewholder与convertView绑定，省去了fingviewbyid的时间，提高性能
			convertView.setTag(holder);
		}else {
			holder=(ViewHolder) convertView.getTag();
		}		
		Drawable drawable=mApplicationInfos.get(position).loadIcon(mContext.getPackageManager());
		if (drawable!=null) {
			holder.mAppIcon.setImageDrawable(drawable);
		}
		holder.mAppName.setText(mApplicationInfos.get(position).loadLabel(mContext.getPackageManager()));
		holder.mPackageName.setText(mApplicationInfos.get(position).packageName);
		return convertView;
	}
	
	private class ViewHolder
	{
		public TextView mAppName;
		public TextView mPackageName;
		public ImageView mAppIcon;
	}
}