package com.qihoo.testutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ChooseAppActivity extends Activity
{
	private ListView mListView;
	private AppAdapter mAdapter;
	private List<ApplicationInfo> mApplicationInfos;
	/* （非 Javadoc）
	 * @see android.app.Activity#onCreate(android.os.Bundle, android.os.PersistableBundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_app);
		mListView=(ListView) findViewById(R.id.app_list);
		mAdapter=new AppAdapter(this);
		mApplicationInfos=new ArrayList<ApplicationInfo>();
		queryFilterAppInfo();
		mAdapter.setAppList(mApplicationInfos);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO 自动生成的方法存根
				Intent intent=new Intent();
				intent.putExtra("app", mApplicationInfos.get(position));
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
	}
	//全部程序包
	private void queryFilterAppInfo() {  
		final PackageManager pm = this.getPackageManager();  
		// 查询所有已经安装的应用程序  
		List<ApplicationInfo> appInfos= pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);// GET_UNINSTALLED_PACKAGES代表已删除，但还有安装目录的
		for (ApplicationInfo app:appInfos) {
			if((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0)//排除系统应用
			{
				mApplicationInfos.add(app);
			}			
		}
		Collections.sort(mApplicationInfos, new Comparator<ApplicationInfo>() {//按名字排序，便于找到应用

			@Override
			public int compare(ApplicationInfo lhs, ApplicationInfo rhs) {
				// TODO 自动生成的方法存根
				return lhs.loadLabel(pm).toString().compareToIgnoreCase(rhs.loadLabel(pm).toString());
			}
		});
	}
	
}