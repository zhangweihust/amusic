package com.android.media.screens.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.screens.AmtScreen;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.ToastUtil;

public class ScreenTimingExit extends AmtScreen implements OnClickListener {
	private Button ok;
	private Button cancel;
	private CheckBox checkbox1;
	private CheckBox checkbox2;
	private CheckBox checkbox3;
	private CheckBox checkbox4;
	private CheckBox checkbox5;
	private CheckBox checkbox6;
	private RelativeLayout layout1;
	private RelativeLayout layout2;
	private RelativeLayout layout3;
	private RelativeLayout layout4;
	private RelativeLayout layout5;
	private RelativeLayout layout6;
	private EditText timingEdit;
	private TextView timingMinite;
	private TextView timingSpaceTime;
	private TimerTask task;
	private Timer mTimer;
	private int checkedId = 0;
	public static long nowtime = 0;
	public static int mytype = 0;
	MyHandler myHandler;
	MyThread m;
	boolean m_flag = true;
	List<Integer> listItemID = new ArrayList<Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_timing_exit);
		checkbox1 = (CheckBox) findViewById(R.id.screen_timing_exit_choose1);
		checkbox2 = (CheckBox) findViewById(R.id.screen_timing_exit_choose2);
		checkbox3 = (CheckBox) findViewById(R.id.screen_timing_exit_choose3);
		checkbox4 = (CheckBox) findViewById(R.id.screen_timing_exit_choose4);
		checkbox5 = (CheckBox) findViewById(R.id.screen_timing_exit_choose5);
		checkbox6 = (CheckBox) findViewById(R.id.screen_timing_exit_choose6);

		checkbox1.setClickable(false);
		checkbox2.setClickable(false);
		checkbox3.setClickable(false);
		checkbox4.setClickable(false);
		checkbox5.setClickable(false);
		checkbox6.setClickable(false);

		layout1 = (RelativeLayout) findViewById(R.id.screen_timing_exit_layout_choose1);
		layout2 = (RelativeLayout) findViewById(R.id.screen_timing_exit_layout_choose2);
		layout3 = (RelativeLayout) findViewById(R.id.screen_timing_exit_layout_choose3);
		layout4 = (RelativeLayout) findViewById(R.id.screen_timing_exit_layout_choose4);
		layout5 = (RelativeLayout) findViewById(R.id.screen_timing_exit_layout_choose5);
		layout6 = (RelativeLayout) findViewById(R.id.screen_timing_exit_layout_choose6);

		layout1.setOnClickListener(this);
		layout2.setOnClickListener(this);
		layout3.setOnClickListener(this);
		layout4.setOnClickListener(this);
		layout5.setOnClickListener(this);
		layout6.setOnClickListener(this);

		timingEdit = (EditText) findViewById(R.id.screen_timing_exit_edit);
		timingMinite = (TextView) findViewById(R.id.screen_timing_exit_minite);
		timingSpaceTime = (TextView) findViewById(R.id.screen_timing_exit_spare_time);
		timingSpaceTime.setTextColor(MediaApplication.color_normal);
		ok = (Button) findViewById(R.id.screen_timing_exit_ok);
		cancel = (Button) findViewById(R.id.screen_timing_exit_cancal);
		ok.setOnClickListener(this);
		cancel.setOnClickListener(this);
		myHandler = new MyHandler();
		m = new MyThread();
	}

	@Override
	protected void onResume() {
		super.onResume();
		timingSpaceTime.setTextColor(MediaApplication.color_normal);
		ServiceManager.getAmtMedia().getGoPlayerBtn()
				.setVisibility(View.INVISIBLE);
		setScreenTitle(getString(R.string.screen_timing_exit_title));
		if (mytype == 0) {
			checkbox1.setChecked(true);
			checkbox2.setChecked(false);
			checkbox3.setChecked(false);
			checkbox4.setChecked(false);
			checkbox5.setChecked(false);
			checkbox6.setChecked(false);
			timingEdit.setVisibility(View.INVISIBLE);
			timingMinite.setVisibility(View.INVISIBLE);
		} else if (mytype == 15) {
			checkbox1.setChecked(false);
			checkbox2.setChecked(true);
			checkbox3.setChecked(false);
			checkbox4.setChecked(false);
			checkbox5.setChecked(false);
			checkbox6.setChecked(false);
			timingEdit.setVisibility(View.INVISIBLE);
			timingMinite.setVisibility(View.INVISIBLE);
		} else if (mytype == 30) {
			checkbox1.setChecked(false);
			checkbox2.setChecked(false);
			checkbox3.setChecked(true);
			checkbox4.setChecked(false);
			checkbox5.setChecked(false);
			checkbox6.setChecked(false);
			timingEdit.setVisibility(View.INVISIBLE);
			timingMinite.setVisibility(View.INVISIBLE);
		} else if (mytype == 60) {
			checkbox1.setChecked(false);
			checkbox2.setChecked(false);
			checkbox3.setChecked(false);
			checkbox4.setChecked(true);
			checkbox5.setChecked(false);
			checkbox6.setChecked(false);
			timingEdit.setVisibility(View.INVISIBLE);
			timingMinite.setVisibility(View.INVISIBLE);
		} else if (mytype == 120) {
			checkbox1.setChecked(false);
			checkbox2.setChecked(false);
			checkbox3.setChecked(false);
			checkbox4.setChecked(false);
			checkbox5.setChecked(true);
			checkbox6.setChecked(false);
			timingEdit.setVisibility(View.INVISIBLE);
			timingMinite.setVisibility(View.INVISIBLE);
		} else {
			checkbox1.setChecked(false);
			checkbox2.setChecked(false);
			checkbox3.setChecked(false);
			checkbox4.setChecked(false);
			checkbox5.setChecked(false);
			checkbox6.setChecked(true);
			timingEdit.setVisibility(View.VISIBLE);
			timingMinite.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_timing_exit_layout_choose1:
			if (!checkbox1.isChecked()) {
				checkbox1.setChecked(true);
				checkbox2.setChecked(false);
				checkbox3.setChecked(false);
				checkbox4.setChecked(false);
				checkbox5.setChecked(false);
				checkbox6.setChecked(false);
				timingEdit.setVisibility(View.INVISIBLE);
				timingMinite.setVisibility(View.INVISIBLE);
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(this.checkbox1.getWindowToken(), 0);
				checkedId = checkbox1.getId();
			} /*else {
				checkbox1.setChecked(false);
				checkedId = 0;
			}*/
			break;
		case R.id.screen_timing_exit_layout_choose2:
			if (!checkbox2.isChecked()) {
				checkbox1.setChecked(false);
				checkbox2.setChecked(true);
				checkbox3.setChecked(false);
				checkbox4.setChecked(false);
				checkbox5.setChecked(false);
				checkbox6.setChecked(false);
				timingEdit.setVisibility(View.INVISIBLE);
				timingMinite.setVisibility(View.INVISIBLE);
				checkedId = checkbox2.getId();
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(this.checkbox1.getWindowToken(), 0);
			}/* else {
				checkbox2.setChecked(false);
				checkedId = 0;
			}*/
			break;
		case R.id.screen_timing_exit_layout_choose3:
			if (!checkbox3.isChecked()) {
				checkbox1.setChecked(false);
				checkbox2.setChecked(false);
				checkbox3.setChecked(true);
				checkbox4.setChecked(false);
				checkbox5.setChecked(false);
				checkbox6.setChecked(false);
				timingEdit.setVisibility(View.INVISIBLE);
				timingMinite.setVisibility(View.INVISIBLE);
				checkedId = checkbox3.getId();
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(this.checkbox1.getWindowToken(), 0);
			} /*else {
				checkbox3.setChecked(false);
				checkedId = 0;
			}*/
			break;
		case R.id.screen_timing_exit_layout_choose4:
			if (!checkbox4.isChecked()) {
				checkbox1.setChecked(false);
				checkbox2.setChecked(false);
				checkbox3.setChecked(false);
				checkbox4.setChecked(true);
				checkbox5.setChecked(false);
				checkbox6.setChecked(false);
				timingEdit.setVisibility(View.INVISIBLE);
				timingMinite.setVisibility(View.INVISIBLE);
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(this.checkbox1.getWindowToken(), 0);
				checkedId = checkbox4.getId();
			} /*else {
				checkbox4.setChecked(false);
				checkedId = 0;
			}*/
			break;
		case R.id.screen_timing_exit_layout_choose5:
			if (!checkbox5.isChecked()) {
				checkbox1.setChecked(false);
				checkbox2.setChecked(false);
				checkbox3.setChecked(false);
				checkbox4.setChecked(false);
				checkbox5.setChecked(true);
				checkbox6.setChecked(false);
				timingEdit.setVisibility(View.INVISIBLE);
				timingMinite.setVisibility(View.INVISIBLE);
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(this.checkbox1.getWindowToken(), 0);
				checkedId = checkbox5.getId();
			}/* else {
				checkbox5.setChecked(false);
				checkedId = 0;
			}*/
			break;
		case R.id.screen_timing_exit_layout_choose6:
			if (!checkbox6.isChecked()) {
				checkbox1.setChecked(false);
				checkbox2.setChecked(false);
				checkbox3.setChecked(false);
				checkbox4.setChecked(false);
				checkbox5.setChecked(false);
				checkbox6.setChecked(true);
				timingEdit.setVisibility(View.VISIBLE);
				timingMinite.setVisibility(View.VISIBLE);
				checkedId = checkbox6.getId();
			} /*else {
				checkbox6.setChecked(false);
				timingEdit.setVisibility(View.INVISIBLE);
				timingMinite.setVisibility(View.INVISIBLE);
				checkedId = 0;
			}*/
			break;
		case R.id.screen_timing_exit_ok:
			if (checkedId == R.id.screen_timing_exit_choose1) {
				exitTimer();
				m_flag = false;
				mytype = 0;
				timingSpaceTime.setText(R.string.screen_timing_exit_spacetime_default);
				this.onBackPressed();
			} else if (checkedId == R.id.screen_timing_exit_choose2) {
				exitTimer();
				mTimer = new Timer();
				task = new TimerTask() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						exitTimer();
						ServiceManager.exit();
					}
				};
				mTimer.schedule(task, 15 * 60 * 1000);
				nowtime = SystemClock.elapsedRealtime();
				m_flag = true;
				mytype = 15;
				// MyThread m = new MyThread();
				new Thread(m).start();
				this.onBackPressed();
			} else if (checkedId == R.id.screen_timing_exit_choose3) {
				exitTimer();
				mTimer = new Timer();
				task = new TimerTask() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						exitTimer();
						ServiceManager.exit();
					}
				};
				mTimer.schedule(task, 30 * 60 * 1000);
				nowtime = SystemClock.elapsedRealtime();
				m_flag = true;
				mytype = 30;
				new Thread(m).start();
				this.onBackPressed();
			} else if (checkedId == R.id.screen_timing_exit_choose4) {
				exitTimer();
				mTimer = new Timer();
				task = new TimerTask() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						exitTimer();
						ServiceManager.exit();
					}
				};
				mTimer.schedule(task, 60 * 60 * 1000);
				nowtime = SystemClock.elapsedRealtime();
				m_flag = true;
				mytype = 60;
				// MyThread m = new MyThread();
				new Thread(m).start();
				this.onBackPressed();
			} else if (checkedId == R.id.screen_timing_exit_choose5) {
				exitTimer();
				mTimer = new Timer();
				task = new TimerTask() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						exitTimer();
						ServiceManager.exit();
					}
				};
				mTimer.schedule(task, 120 * 60 * 1000);
				nowtime = SystemClock.elapsedRealtime();
				mytype = 120;
				m_flag = true;
				new Thread(m).start();
				this.onBackPressed();
			} else if (checkedId == R.id.screen_timing_exit_choose6) {
				exitTimer();
				String mytime = "";
				mytime = timingEdit.getText().toString();
				if (mytime.matches("[0-9]+")) {
					int t = Integer.parseInt(mytime);
					mTimer = new Timer();
					task = new TimerTask() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							exitTimer();
							ServiceManager.exit();
						}
					};
					mTimer.schedule(task, t * 60 * 1000);
					nowtime = SystemClock.elapsedRealtime();
					m_flag = true;
					mytype = t;
					new Thread(m).start();
					this.onBackPressed();
				} else {
					Toast toast = ToastUtil.getInstance().getToast(
							getString(R.string.screen_timing_exit_toast1));
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			} else { // checkedId==0 没有一种被选中
				Toast toast = ToastUtil.getInstance().getToast(
						getString(R.string.screen_timing_exit_toast2));
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			break;
		case R.id.screen_timing_exit_cancal:
			if (mytype == 0) {
				checkbox1.setChecked(true);
				checkbox2.setChecked(false);
				checkbox3.setChecked(false);
				checkbox4.setChecked(false);
				checkbox5.setChecked(false);
				checkbox6.setChecked(false);
				timingEdit.setVisibility(View.INVISIBLE);
				timingMinite.setVisibility(View.INVISIBLE);
			} else if (mytype == 15) {
				checkbox1.setChecked(false);
				checkbox2.setChecked(true);
				checkbox3.setChecked(false);
				checkbox4.setChecked(false);
				checkbox5.setChecked(false);
				checkbox6.setChecked(false);
				timingEdit.setVisibility(View.INVISIBLE);
				timingMinite.setVisibility(View.INVISIBLE);
			} else if (mytype == 30) {
				checkbox1.setChecked(false);
				checkbox2.setChecked(false);
				checkbox3.setChecked(true);
				checkbox4.setChecked(false);
				checkbox5.setChecked(false);
				checkbox6.setChecked(false);
				timingEdit.setVisibility(View.INVISIBLE);
				timingMinite.setVisibility(View.INVISIBLE);
			} else if (mytype == 60) {
				checkbox1.setChecked(false);
				checkbox2.setChecked(false);
				checkbox3.setChecked(false);
				checkbox4.setChecked(true);
				checkbox5.setChecked(false);
				checkbox6.setChecked(false);
				timingEdit.setVisibility(View.INVISIBLE);
				timingMinite.setVisibility(View.INVISIBLE);
			} else if (mytype == 120) {
				checkbox1.setChecked(false);
				checkbox2.setChecked(false);
				checkbox3.setChecked(false);
				checkbox4.setChecked(false);
				checkbox5.setChecked(true);
				checkbox6.setChecked(false);
				timingEdit.setVisibility(View.INVISIBLE);
				timingMinite.setVisibility(View.INVISIBLE);
			} else {
				checkbox1.setChecked(false);
				checkbox2.setChecked(false);
				checkbox3.setChecked(false);
				checkbox4.setChecked(false);
				checkbox5.setChecked(false);
				checkbox6.setChecked(true);
			}
			this.onBackPressed();
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Activity parent = getParent();
		if (parent != null) {
			parent.onBackPressed();
		}
	}
	
	

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.checkbox1.getWindowToken(), 0);
		super.onPause();
	}

	public void exitTimer() {
		if (mTimer != null) {
			mTimer.cancel();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	public boolean hasMenu() {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * 接受消息,处理消息 ,此Handler会与当前主线程一块运行
	 * */

	class MyHandler extends Handler {
		public MyHandler() {
		}

		public MyHandler(Looper L) {
			super(L);
		}

		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// Log.d("MyHandler", "handleMessage......");
			super.handleMessage(msg);
			// 此处可以更新UI
			Bundle b = msg.getData();
			int m_type = 0;
			m_type = b.getInt("type");
			String spaceStr =getString(R.string.screen_timing_exit_spacetime_before);
			String closeStr =getString(R.string.screen_timing_exit_spacetime_after);
			long spacetime = 0;
			spacetime = SystemClock.elapsedRealtime() - nowtime;
			spacetime = spacetime / (60 * 1000);
			spacetime = m_type - spacetime;
			if (spacetime > 0) {
				timingSpaceTime.setText(spaceStr + spacetime + closeStr);
			} else {
				timingSpaceTime.setText(getString(R.string.screen_timing_exit_spacetime_default));
			}
			// MyHandlerActivity.this.button.append(color);

		}
	}

	class MyThread implements Runnable {
		public void run() {

			try {
				while (m_flag) {
					Thread.sleep(1000);
					Message msg = new Message();
					Bundle b = new Bundle();// 存放数据
					b.putInt("type", mytype);
					msg.setData(b);
					ScreenTimingExit.this.myHandler.sendMessage(msg);
				}//
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Log.d("thread.......", "mThread........");

			// 向Handler发送消息,更新UI

		}
	}
}
