package com.android.media.services.impl;

import java.text.SimpleDateFormat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.services.INetworkService;
import com.android.media.task.SendCrashReportsTask;
import com.android.media.utils.ToastUtil;

public class NetworkService implements INetworkService {

	private WifiManager wifiManager;
	private WifiLock wifiLock;
	private static String TAG = NetworkService.class.getCanonicalName();
	// Will be added in froyo SDK
	private int ConnectivityManager_TYPE_WIMAX = 6;
	private Context context;
	private Handler handler;
	private NetstateReceiver mReceiver;
	private SimpleDateFormat sDateFormat;
	private static final String XML_NAME = "SendCrash";
	private static final String XML_KEY_TIME = "crash_upload_time";
	SharedPreferences preferences;
	private int netType;

	@Override
	public boolean start() {
		context = MediaApplication.getContext();
		this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		handler = ServiceManager.getAmtMediaHandler();
		mReceiver = new NetstateReceiver();
		preferences = context.getSharedPreferences(XML_NAME,Context.MODE_WORLD_WRITEABLE);
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		context.registerReceiver(mReceiver, filter);
		return true;
	}

	@Override
	public boolean stop() {
		context.unregisterReceiver(mReceiver);
		return true;
	}
	
	
	@Override
	public int getNetType() {
		return netType;
	}

	@Override
	public boolean acquire(boolean show) {
		boolean connected = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		if (networkInfo == null) {
			if (show)
			handler.post(new Runnable() {

				@Override
				public void run() {
					Toast toast = ToastUtil.getInstance().getToast(context.getString(R.string.get_network_failed));
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			});

//			Log.d(NetworkService.TAG, "Failed to get Network information");
			return false;
		}

		netType = networkInfo.getType();
		int netSubType = networkInfo.getSubtype();
		if (!networkInfo.isAvailable()) {
			if (show)
			handler.post(new Runnable() {

				@Override
				public void run() {
					Toast toast = ToastUtil.getInstance().getToast(context.getString(R.string.no_network));
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			});

			return false;
		}

//		Log.d(NetworkService.TAG, String.format("netType=%d and netSubType=%d", netType, netSubType));

		if (netType == ConnectivityManager.TYPE_WIFI) {
			if (this.wifiManager.isWifiEnabled()) {
				this.wifiLock = this.wifiManager.createWifiLock(NetworkService.TAG);
				final WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
				if (wifiInfo != null && this.wifiLock != null) {
					final DetailedState detailedState = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
					if (detailedState == DetailedState.CONNECTED || detailedState == DetailedState.CONNECTING || detailedState == DetailedState.OBTAINING_IPADDR) {
						connected = true;
					}
				}
			} else {
				if (show)
				handler.post(new Runnable() {

					@Override
					public void run() {
						Toast toast = ToastUtil.getInstance().getToast(context.getString(R.string.wifi_unable));
						toast.setDuration(Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
				});

//				Log.d(NetworkService.TAG, "WiFi not enabled");
			}
		} else if (netType == ConnectivityManager.TYPE_MOBILE || netType == ConnectivityManager_TYPE_WIMAX) {
			if ((netSubType >= TelephonyManager.NETWORK_TYPE_UMTS) || // HACK
					(netSubType == TelephonyManager.NETWORK_TYPE_GPRS) || (netSubType == TelephonyManager.NETWORK_TYPE_EDGE)) {
				connected = true;
			} else {
				if (show)
				handler.post(new Runnable() {

					@Override
					public void run() {
						Toast toast = ToastUtil.getInstance().getToast(context.getString(R.string.n_3g_unable));
						toast.setDuration(Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
				});

//				Log.d(NetworkService.TAG, "3G not enabled");
			}
		}
		if (!connected) {
			return false;
		}
		return true;
	}
	
	class NetstateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			MediaApplication.logD(NetworkService.class, "Net is changed");
				ConnectivityManager manager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo gprs = manager
						.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				NetworkInfo wifi = manager
						.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if ((gprs == null || !gprs.isConnected()) && (wifi == null ||!wifi.isConnected())) {
					MediaApplication.networkIsOk = false;
				} else {
					MediaApplication.networkIsOk = true;
					String saveTime =preferences.getString(XML_KEY_TIME, null);
					sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
					if(saveTime != null && sDateFormat.format(new java.util.Date()).equals(saveTime)){
						return;
					} 
					MediaApplication.logD(NetworkService.class, "Net is connected");
					String date = sDateFormat.format(new java.util.Date());
					preferences.edit().putString(XML_KEY_TIME, date).commit();
					SendCrashReportsTask task = new SendCrashReportsTask();
					task.execute();
				}
		}

	}
	
	

}
