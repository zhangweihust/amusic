package com.amusic.media.services.impl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.amusic.media.AmtMedia;
import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.services.INotificatioService;
import com.amusic.media.utils.Constant;

public class NotificationService implements INotificatioService {
	private NotificationManager notificationManager;
	private Notification notification;
	public static final int notificationId = 5316;
	private Context context;
//	private boolean statusBar = false;
	@Override
	public boolean start() {
		context = MediaApplication.getContext();
		notificationManager = (NotificationManager) context
		.getSystemService(Context.NOTIFICATION_SERVICE);
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < 6)
		showNotification();
		
		return true;
	}
	
	public void showNotification(){	
		if (notification == null)
		{
			notification = new Notification(
					R.drawable.amtplayer_notification, null, 0);
		}
		Intent intent = new Intent();
		ComponentName componentName = new ComponentName(context, AmtMedia.class);
		intent.setComponent(componentName);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		notification.contentView = new RemoteViews(MediaApplication.getInstance().getPackageName(),R.layout.screen_notification);
		notification.contentIntent = pendingIntent;
		//		notification.setLatestEventInfo(context, context.getString(R.string.app_name), null, pendingIntent);
		notification.flags = Notification.FLAG_ONGOING_EVENT; // 放在正在进行里面
		notificationManager.notify(notificationId, notification);
	}
	
	public void dismissNotification(){
		notificationManager.cancelAll();
	}

	@Override
	public boolean stop() {
		// notificationManager.cancel(notificationId);
		dismissNotification();
		return true;
	}

	public void setCurSongPrompt(String curSongName)
	{
		if (notification != null)
		{
			notification.contentView.setTextViewText(R.id.screen_nitification_current_song, curSongName);
			notificationManager.notify(notificationId, notification);
		}
	}
}
