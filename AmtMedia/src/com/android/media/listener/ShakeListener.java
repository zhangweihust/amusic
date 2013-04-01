package com.android.media.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.android.media.screens.IScreen.ScreenType;
import com.android.media.services.impl.MediaPlayerService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.Constant;

public class ShakeListener implements SensorEventListener {

	private static final int FORCE_THRESHOLD = 800;
	private static final int TIME_THRESHOLD = 100;
	private static final int SHAKE_TIMEOUT = 500;
	private static final int SHAKE_DURATION = 1000;
	private static final int SHAKE_COUNT = 6;

	private SensorManager mSensorMgr;
	private float mLastX = -1.0f;
	private float mLastY = -1.0f;
	private float mLastZ = -1.0f;
	private long mLastTime;
	private OnShakeListener mShakeListener;
	private Context mContext;
	private int mShakeCount = 0;
	private long mLastShake;
	private long mLastForce;

	public interface OnShakeListener {
		public void onShake();
		public void onNextShake();
		public void onPreShake();
	}

	public ShakeListener(Context context) {
		mContext = context;
		resume();
	}

	public void setOnShakeListener(OnShakeListener listener) {
		mShakeListener = listener;
	}

	public void resume() {
		mSensorMgr = (SensorManager) mContext
				.getSystemService(Context.SENSOR_SERVICE);
		if (mSensorMgr == null) {
			throw new UnsupportedOperationException("Sensors not supported");
		}

		boolean supported = mSensorMgr.registerListener(this,
				mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
		if (!supported) {
			mSensorMgr.unregisterListener(this);
/*			throw new UnsupportedOperationException(
					"Accelerometer not supported");*/
		}
	}

	public void pause() {
		if (mSensorMgr != null) {
			mSensorMgr.unregisterListener(this);
			mSensorMgr = null;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
//		if(!ServiceManager.getMediaplayerService().getMediaPlayer()
//		.isPlaying() || Constant.WHICH_PLAYER != 1/*MediaPlayerService.typefinal != ScreenType.TYPE_AUDIO*/){
//			return ;
//		}
		
		if(Constant.WHICH_PLAYER == 1){
			if(ServiceManager.getMediaplayerService().getMediaPlayer().isPlaying() || !MediaPlayerService.flagMusicError){
				if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
					return;
				}
				long now = System.currentTimeMillis();

				if ((now - mLastForce) > SHAKE_TIMEOUT) {
					mShakeCount = 0;
				}

				if ((now - mLastTime) > TIME_THRESHOLD) {
					long diff = now - mLastTime;
					float speed = Math.abs(event.values[SensorManager.DATA_X]
							+ event.values[SensorManager.DATA_Y]
							+ event.values[SensorManager.DATA_Z] - mLastX - mLastY
							- mLastZ)
							/ diff * 10000;
					
					//System.out.println("speed="+speed);
					//System.out.println("SHAKE_DURATION="+(now - mLastShake));
					//System.out.println("mShakeCount="+mShakeCount);
					if (speed > (100 - Constant.SPECIAL_LASHING_PROGRESS) / 100.0 * FORCE_THRESHOLD) {
						if ((++mShakeCount >= SHAKE_COUNT)
								&& (now - mLastShake > SHAKE_DURATION)) {
							mLastShake = now;
							mShakeCount = 0;
							if (mShakeListener != null) {
								if(Constant.IS_SPECIAL_LASHING_CONTROLL){
									if(event.values[SensorManager.DATA_X] -mLastX > 0){
										mShakeListener.onNextShake();
									}else if(event.values[SensorManager.DATA_X] - mLastX < 0){
										mShakeListener.onPreShake();
									}
								}else{
									mShakeListener.onShake();
								}
							}
						}
						mLastForce = now;
					}
					mLastTime = now;
					mLastX = event.values[SensorManager.DATA_X];
					mLastY = event.values[SensorManager.DATA_Y];
					mLastZ = event.values[SensorManager.DATA_Z];
				}
			}
		}
	}
}