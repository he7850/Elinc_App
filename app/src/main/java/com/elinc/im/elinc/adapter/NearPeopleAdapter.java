package com.elinc.im.elinc.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.v3.datatype.BmobGeoPoint;

import com.elinc.im.elinc.CustomApplcation;
import com.elinc.im.elinc.R;
import com.elinc.im.elinc.adapter.base.BaseListAdapter;
import com.elinc.im.elinc.adapter.base.ViewHolder;
import com.elinc.im.elinc.bean.User;
import com.elinc.im.elinc.util.CollectionUtils;
import com.elinc.im.elinc.util.ImageLoadOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 附近的人
 * 
 * ClassName: BlackListAdapter
 * Description: TODO
 * author smile
 * date 2014-6-24 下午5:27:14
 */
public class NearPeopleAdapter extends BaseListAdapter<User> {

	public NearPeopleAdapter(Context context, List<User> list) {
		super(context, list);
	}

	@Override
	public View bindView(int arg0, View convertView, ViewGroup arg2) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_near_people, null);
		}
		final User contract = getList().get(arg0);
		TextView tv_name = ViewHolder.get(convertView, R.id.tv_friend_name);
		TextView campus = ViewHolder.get(convertView, R.id.campus);
		ImageView iv_avatar = ViewHolder.get(convertView, R.id.img_friend_avatar);
		TextView tag1 = ViewHolder.get(convertView, R.id.tag1);
		TextView tag2 = ViewHolder.get(convertView, R.id.tag2);
		TextView tag3 = ViewHolder.get(convertView, R.id.tag3);
		if (CollectionUtils.isNotNull(contract.getTags())) {
			if (contract.getTags().size() > 0 && contract.getTags().get(0)!=null) {
				tag1.setText(contract.getTags().get(0));
			}
			if (contract.getTags().size() > 1 && contract.getTags().get(1)!=null) {
				tag2.setText(contract.getTags().get(1));
			}else{
				tag2.setVisibility(View.GONE);
			}
			if (contract.getTags().size() > 2 && contract.getTags().get(2)!=null) {
				tag3.setText(contract.getTags().get(2));
			} else {
				tag3.setVisibility(View.GONE);
			}
		}else {
			tag2.setVisibility(View.GONE);
			tag3.setVisibility(View.GONE);
		}
		String avatar = contract.getAvatar();
		if (avatar != null && !avatar.equals("")) {
			ImageLoader.getInstance().displayImage(avatar, iv_avatar,
					ImageLoadOptions.getOptions());
		} else {
			iv_avatar.setImageResource(R.drawable.default_head);
		}
//		BmobGeoPoint location = contract.getLocation();
//		String currentLat = CustomApplcation.getInstance().getLatitude();
//		String currentLong = CustomApplcation.getInstance().getLongtitude();
		campus.setText(contract.getCampus());
		tv_name.setText(contract.getUsername());
		return convertView;
	}

	private static final double EARTH_RADIUS = 6378137;

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	/**
	 * 根据两点间经纬度坐标（double值），计算两点间距离，
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return 距离：单位为米
	 */
	public static double DistanceOfTwoPoints(double lat1, double lng1,double lat2, double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}

}
