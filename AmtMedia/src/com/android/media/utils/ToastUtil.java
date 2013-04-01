package com.android.media.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.MediaApplication;

public class ToastUtil {
	private Context context;
	private static ToastUtil toastUtil;

	private Toast toast;
	private TextView textView;

	private ToastUtil() {
		context = MediaApplication.getContext();
		View view = LayoutInflater.from(context).inflate(R.layout.screen_audio_toast, null);
		textView = (TextView) view.findViewById(R.id.screen_audio_download_toast_text);
		toast = new Toast(context);
		toast.setView(view);
	}

	public static ToastUtil getInstance() {
		if (toastUtil == null) {
			toastUtil = new ToastUtil();
		}
		return toastUtil;
	}

	public Toast getToast(String text) {
		textView.setText(text);
		return toast;
	}
}
