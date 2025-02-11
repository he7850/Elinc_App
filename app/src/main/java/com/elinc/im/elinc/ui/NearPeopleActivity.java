package com.elinc.im.elinc.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import cn.bmob.im.task.BRequest;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;

import com.elinc.im.elinc.R;
import com.elinc.im.elinc.adapter.NearPeopleAdapter;
import com.elinc.im.elinc.bean.User;
import com.elinc.im.elinc.util.CollectionUtils;
import com.elinc.im.elinc.view.xlist.XListView;
import com.elinc.im.elinc.view.xlist.XListView.IXListViewListener;

/**
 * 附近的人列表
 * 现已被修改为推荐学伴
 * 
 * ClassName: NewFriendActivity
 * Description:
 * author smile
 * date 2014-6-6 下午4:28:09
 */
public class NearPeopleActivity extends ActivityBase implements IXListViewListener,OnItemClickListener {

	private XListView mListView;
	private NearPeopleAdapter adapter;
	private User currentUser;

	List<User> nears = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_near_people);
		initView();
	}

	private void initView() {
		initTopBarForLeft("学伴推荐");
		currentUser = BmobUser.getCurrentUser(this,User.class);
		initXListView();
	}

	private void initXListView() {
		mListView = (XListView) findViewById(R.id.list_near);
		mListView.setOnItemClickListener(this);
		mListView.setPullLoadEnable(false);// 首先不允许加载更多
		mListView.setPullRefreshEnable(true);// 允许下拉
		mListView.setXListViewListener(this);// 设置监听器
		mListView.pullRefreshing();// 设置下拉刷新
		adapter = new NearPeopleAdapter(this, nears);
		mListView.setAdapter(adapter);
		initNearByList(false);
	}

	private ProgressDialog progress;
	private void initNearByList(final boolean isUpdate){
		if(!isUpdate){
			progress = new ProgressDialog(NearPeopleActivity.this);
			progress.setMessage("正在为您推荐...");
			progress.setCanceledOnTouchOutside(true);
			progress.show();
		}

		/**之下为按标签匹配的算法
		 * by VkaZas 2015.8.16
		 */
		List<String> tags = currentUser.getTags();
		List<BmobQuery<User>> queries = new ArrayList<>();
		BmobQuery<User> query = new BmobQuery<>();
		if (CollectionUtils.isNotNull(tags)) {
			BmobLog.i("获取tag数量", String.valueOf(tags.size()));
			for (int i=0;i<tags.size();i++) {
				BmobLog.i("获取tag", tags.get(i));
				if (tags.get(i)!=null) {
					query.addWhereContains("tags",tags.get(i));
					queries.add(query);
				}
			}
		}
		BmobQuery<User> orQuery = new BmobQuery<>();
		orQuery.or(queries);
		orQuery.findObjects(this, new FindListener<User>() {

				@Override
				public void onSuccess(List<User> arg0) {
					if (CollectionUtils.isNotNull(arg0)) {
						if(isUpdate){
							nears.clear();
						}
						for (int i=0;i<arg0.size();i++) {
							BmobLog.i("匹配列表中的用户",arg0.get(i).getUsername());
							if (arg0.get(i).getUsername().equals(currentUser.getUsername())) {
								arg0.remove(i);
								break;
							}
						}
						for (int i=arg0.size()-1;i>=0;i--) {
							if (i>4) {
								arg0.remove(i);
							}
						}
						adapter.addAll(arg0);
						if(arg0.size()<BRequest.QUERY_LIMIT_COUNT){
							mListView.setPullLoadEnable(false);
							ShowToast("匹配完成^_^!");
						}else{
							mListView.setPullLoadEnable(true);
						}
					}else{
						ShowToast("没有匹配到人T_T");
					}

					if(!isUpdate){
						progress.dismiss();
					}else{
						refreshPull();
					}
				}

				@Override
				public void onError(int arg0, String arg1) {
					ShowToast("没有匹配到人T_T");
					mListView.setPullLoadEnable(false);
					if(!isUpdate){
						progress.dismiss();
					}else{
						refreshPull();
					}
				}
		});
	}
	
	/** 查询更多
	  * Title: queryMoreNearList
	  * Description:
	  * param page
	  * return void
	  * throws
	  */
	private void queryMoreNearList(int page){
		double latitude = Double.parseDouble(mApplication.getLatitude());
		double longtitude = Double.parseDouble(mApplication.getLongtitude());
		double QUERY_KILOMETERS = 100;
		userManager.queryKiloMetersListByPage(true,page,"location", longtitude, latitude, true, QUERY_KILOMETERS,"sex",false,new FindListener<User>() {
			@Override
			public void onSuccess(List<User> arg0) {
				if (CollectionUtils.isNotNull(arg0)) {
					adapter.addAll(arg0);
				}
				refreshLoad();
			}
			
			@Override
			public void onError(int arg0, String arg1) {
				ShowLog("查询更多附近的人出错:"+arg1);
				mListView.setPullLoadEnable(false);
				refreshLoad();
			}

		});
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		User user = (User) adapter.getItem(position-1);
		Intent intent =new Intent(this,SetMyInfoActivity.class);
		intent.putExtra("from", "add");
		intent.putExtra("username", user.getUsername());
		startAnimActivity(intent);		
	}

	@Override
	public void onRefresh() {
		initNearByList(true);
	}

	private void refreshLoad(){
		if (mListView.getPullLoading()) {
			mListView.stopLoadMore();
		}
	}
	
	private void refreshPull(){
		if (mListView.getPullRefreshing()) {
			mListView.stopRefresh();
		}
	}
	@Override
	public void onLoadMore() {
		//double latitude = Double.parseDouble(mApplication.getLatitude());
		//double longtitude = Double.parseDouble(mApplication.getLongtitude());
		//这是查询10公里范围内的性别为女用户总数
		//userManager.queryKiloMetersTotalCount(User.class, "location", longtitude, latitude, true,QUERY_KILOMETERS,"sex",false,new CountListener() {
	    //这是查询附近的人且性别为女性的用户总数
		//userManager.queryNearTotalCount(User.class, "location", longtitude, latitude, true,"sex",false,new CountListener() {
		//	@Override
		//	public void onSuccess(int arg0) {
		//		if(arg0 >nears.size()){
		//			curPage++;
		//			queryMoreNearList(curPage);
		//		}else{
		//			ShowToast("数据加载完成");
		//			mListView.setPullLoadEnable(false);
		//			refreshLoad();
		//		}
		//	}
		//
		//	@Override
		//	public void onFailure(int arg0, String arg1) {
		//		ShowLog("查询附近的人总数失败"+arg1);
		//		refreshLoad();
		//	}
		//});
	}

}
