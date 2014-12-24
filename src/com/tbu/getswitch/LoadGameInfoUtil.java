package com.tbu.getswitch;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class LoadGameInfoUtil {
	
	private static final String APP_INDEX = "switch_app_index";
	private static final String APP_CHANNEL_ID = "switch_app_channel_id";
	
	public static String getAppIndex(Context context){
		String index = getMetaData(context, LoadGameInfoUtil.APP_INDEX);
		if(index.length() == 1){
			index = "0" + index;
		}
		return index;
	}
	
	public static String getAppChannelId(Context context){
		return getMetaData(context, LoadGameInfoUtil.APP_CHANNEL_ID);
	}
	
	private static String getMetaData(Context context,String key) {
		ApplicationInfo appInfo;
		try {
			appInfo = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			return String.valueOf(appInfo.metaData.get(key));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "unknown";
		}
	}
}
