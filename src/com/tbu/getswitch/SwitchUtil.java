package com.tbu.getswitch;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SwitchUtil {
	
	private static final String TAG = "SwitchUtil";
	
	private static String urlStr = "http://poxiaoshipshape.duapp.com/API01/MoreGameSwitchState/";
	
	public static void getMoreGameSwitch(final Context context,final SwitchCallback callback){
		new AsyncTask<String, Integer, String>() {

			@Override
			protected String doInBackground(String... params) {			
				HttpClient httpClient = null;
				String result = null;
				try {
					httpClient = new DefaultHttpClient();
					HttpParams connectionParams = httpClient.getParams();
					if(isMobileConnect(context) && getCarrier(context)==1){
						HttpHost proxy = new HttpHost("10.0.0.172", 80);
						connectionParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
					}
					HttpConnectionParams.setConnectionTimeout(connectionParams, 30*1000);
					HttpConnectionParams.setSoTimeout(connectionParams, 30*1000);
			        String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";  
			        HttpProtocolParams.setUserAgent(connectionParams, userAgent); 
					HttpGet httpRequest = new HttpGet(params[0]+LoadGameInfoUtil.getAppIndex(context)+"/"+LoadGameInfoUtil.getAppChannelId(context));
					HttpResponse response = httpClient.execute(httpRequest);
					if(response.getStatusLine().getStatusCode() == 200){
						result = EntityUtils.toString(response.getEntity());
						if(result == null){
							result = "14";
						}else if(result.equals("0")){
							result = "15";
						}
					}else{						
						result = String.valueOf(response.getStatusLine().getStatusCode());
					}
					Log.i(TAG,"result=" + result);
				} catch (ConnectionPoolTimeoutException e) {
					result = "2";
					Log.i(TAG,">>>>>>>>>>>>>>>>>>>>>>catch ConnectionPoolTimeoutException");
					e.printStackTrace();
				} catch (ConnectTimeoutException e) {
					result = "3";
					Log.i(TAG,">>>>>>>>>>>>>>>>>>>>>>catch ConnectTimeoutException");
					e.printStackTrace();
				} catch (SocketTimeoutException e) {
					result = "4";
					Log.i(TAG,">>>>>>>>>>>>>>>>>>>>>>catch SocketTimeoutException");
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					result = "5";
					Log.i(TAG,">>>>>>>>>>>>>>>>>>>>>>catch ClientProtocolException");
					e.printStackTrace();
				} catch (UnknownHostException e) {
					result = "6";
					Log.i(TAG,">>>>>>>>>>>>>>>>>>>>>>catch UnknownHostException");
					e.printStackTrace();
				} catch (IOException e) {
					result = "7";
					Log.i(TAG,">>>>>>>>>>>>>>>>>>>>>>catch IOException");
					e.printStackTrace();
				} catch (Exception e) {
					result = "12";
					Log.i(TAG,">>>>>>>>>>>>>>>>>>>>>>catch Exception");
					e.printStackTrace();
				}finally {
					if(httpClient != null){
						httpClient.getConnectionManager().shutdown();
					}
				}
				
				return result;
			}
			
			@Override
			protected void onPostExecute(String result) {
				if(result != null && result.length() > 0){
					if(result.contains("{\"state\":\"0\"}") || result.contains("{\"state\":\"1\"}")){
						try {
							JSONObject obj = new JSONObject(result);
							callback.result(obj.getInt("state") == 1,0);
						} catch (JSONException e) {
							callback.result(false, 8);
							e.printStackTrace();
						}
					}else if(result.length() <= 3){
						try{
							callback.result(false, Integer.valueOf(result));
						}catch(NumberFormatException e){
							callback.result(false, 9);
						}
					}else{
						if(result.contains("html") || result.contains("xml")){
							callback.result(false, 13);
						}else{
							callback.result(false, 10);
						}
					}
				}else{
					callback.result(false, 11);
				}
			}
		}.execute(urlStr);
	}
	
	private static boolean isMobileConnect(Context context){
		ConnectivityManager manager = 
				(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		if(networkInfo != null && networkInfo.isConnected()){
			Log.i(TAG,"networkType=" + networkInfo.getType());
			return networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
		}
		return false;
	}
	
	private static int getCarrier(Context context){
		int carrier = 0;
		TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = tm.getSubscriberId();
        if (imsi == null || imsi.equals("")) {
            imsi = "111111111111111";
        } else if (imsi.length() < 15) {
            int len = imsi.length();
            int s = 15 - len;
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < s; i++) {
                sb.append("0");
            }
            imsi += sb.toString();
        } else if (imsi.length() > 15) {
            imsi = imsi.substring(0, 15);
        }
        Log.i(TAG,"imsi=" + imsi);

        String imsiInfo = imsi;
        String operPrefix = imsiInfo.substring(0, 5);
        if (operPrefix.equalsIgnoreCase("46000") || operPrefix.equalsIgnoreCase("46002")
                || operPrefix.equalsIgnoreCase("46007")) {
            // 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号 //中国移动
            carrier = 1;
        } else if (operPrefix.equalsIgnoreCase("46001") || operPrefix.equalsIgnoreCase("46006")) {
            // 中国联通
            carrier = 2;
        } else if (operPrefix.equalsIgnoreCase("46003") || operPrefix.equalsIgnoreCase("46005")) {
            // 中国电信
            carrier = 3;
        } else if (operPrefix.equalsIgnoreCase("46020")) {
            // 中国铁通
            carrier = 4;
        }
        return carrier;
	}
}
