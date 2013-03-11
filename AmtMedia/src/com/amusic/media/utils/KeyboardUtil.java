package com.amusic.media.utils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;

import com.amusic.media.R;
import com.amusic.media.services.IMediaService;

public class KeyboardUtil implements OnClickListener,OnLongClickListener{
	private Handler handler;

	@Override
	public void onClick(View v) {
		Message message = new Message();
		switch (v.getId()) {
		case R.id.screen_search_list_keyboard_0:

		case R.id.screen_search_list_keyboard_1:

		case R.id.screen_search_list_keyboard_2:

		case R.id.screen_search_list_keyboard_3:

		case R.id.screen_search_list_keyboard_4:

		case R.id.screen_search_list_keyboard_5:

		case R.id.screen_search_list_keyboard_6:

		case R.id.screen_search_list_keyboard_7:

		case R.id.screen_search_list_keyboard_8:

		case R.id.screen_search_list_keyboard_9:

		case R.id.screen_search_list_keyboard_q:

		case R.id.screen_search_list_keyboard_w:

		case R.id.screen_search_list_keyboard_e:

		case R.id.screen_search_list_keyboard_r:

		case R.id.screen_search_list_keyboard_t:

		case R.id.screen_search_list_keyboard_y:

		case R.id.screen_search_list_keyboard_u:

		case R.id.screen_search_list_keyboard_i:

		case R.id.screen_search_list_keyboard_o:

		case R.id.screen_search_list_keyboard_p:

		case R.id.screen_search_list_keyboard_a:

		case R.id.screen_search_list_keyboard_s:

		case R.id.screen_search_list_keyboard_d:

		case R.id.screen_search_list_keyboard_f:

		case R.id.screen_search_list_keyboard_g:

		case R.id.screen_search_list_keyboard_h:

		case R.id.screen_search_list_keyboard_j:

		case R.id.screen_search_list_keyboard_k:

		case R.id.screen_search_list_keyboard_l:
	    
		case R.id.screen_search_list_keyboard_question:   //?

		case R.id.screen_search_list_keyboard_z:

		case R.id.screen_search_list_keyboard_x:

		case R.id.screen_search_list_keyboard_c:

		case R.id.screen_search_list_keyboard_v:

		case R.id.screen_search_list_keyboard_b:

		case R.id.screen_search_list_keyboard_n:

		case R.id.screen_search_list_keyboard_m:
			message.what = IMediaService.MSG_WHAT_CHARACTER;
			message.obj = ((Button) v).getText();
			Bundle b = new Bundle();
			b.putInt("left", v.getLeft());
			message.setData(b);/*
			new Thread(new myThread(handler)).start();*/
			break;
		case R.id.screen_search_list_keyboard_wildcard:
			message.what = IMediaService.MSG_WHAT_WILD;
			break;
		case R.id.screen_search_list_keyboard_del:
			message.what = IMediaService.MSG_WHAT_DELETE;
			message.obj = "DEL";
			Bundle b_del = new Bundle();
			b_del.putInt("left", v.getLeft());
			message.setData(b_del);
			break;
		}
		handler.sendMessage(message);
	}
	
	public KeyboardUtil(Activity screen, Handler handler) {
		this.handler = handler;
		Button character_0 = (Button) screen.findViewById(R.id.screen_search_list_keyboard_0);
		Button character_1 = (Button) screen.findViewById(R.id.screen_search_list_keyboard_1);
		Button character_2 = (Button) screen.findViewById(R.id.screen_search_list_keyboard_2);
		Button character_3 = (Button) screen.findViewById(R.id.screen_search_list_keyboard_3);
		Button character_4 = (Button) screen.findViewById(R.id.screen_search_list_keyboard_4);
		Button character_5 = (Button) screen.findViewById(R.id.screen_search_list_keyboard_5);
		Button character_6 = (Button) screen.findViewById(R.id.screen_search_list_keyboard_6);
		Button character_7 = (Button) screen.findViewById(R.id.screen_search_list_keyboard_7);
		Button character_8 = (Button) screen.findViewById(R.id.screen_search_list_keyboard_8);
		Button character_9 = (Button) screen.findViewById(R.id.screen_search_list_keyboard_9);

		Button character_q = (Button) screen.findViewById(R.id.screen_search_list_keyboard_q);
		Button character_w = (Button) screen.findViewById(R.id.screen_search_list_keyboard_w);
		Button character_e = (Button) screen.findViewById(R.id.screen_search_list_keyboard_e);
		Button character_r = (Button) screen.findViewById(R.id.screen_search_list_keyboard_r);
		Button character_t = (Button) screen.findViewById(R.id.screen_search_list_keyboard_t);
		Button character_y = (Button) screen.findViewById(R.id.screen_search_list_keyboard_y);
		Button character_u = (Button) screen.findViewById(R.id.screen_search_list_keyboard_u);
		Button character_i = (Button) screen.findViewById(R.id.screen_search_list_keyboard_i);
		Button character_o = (Button) screen.findViewById(R.id.screen_search_list_keyboard_o);
		Button character_p = (Button) screen.findViewById(R.id.screen_search_list_keyboard_p);

		Button character_a = (Button) screen.findViewById(R.id.screen_search_list_keyboard_a);
		Button character_s = (Button) screen.findViewById(R.id.screen_search_list_keyboard_s);
		Button character_d = (Button) screen.findViewById(R.id.screen_search_list_keyboard_d);
		Button character_f = (Button) screen.findViewById(R.id.screen_search_list_keyboard_f);
		Button character_g = (Button) screen.findViewById(R.id.screen_search_list_keyboard_g);
		Button character_h = (Button) screen.findViewById(R.id.screen_search_list_keyboard_h);
		Button character_j = (Button) screen.findViewById(R.id.screen_search_list_keyboard_j);
		Button character_k = (Button) screen.findViewById(R.id.screen_search_list_keyboard_k);
		Button character_l = (Button) screen.findViewById(R.id.screen_search_list_keyboard_l);
		Button character_question = (Button) screen.findViewById(R.id.screen_search_list_keyboard_question);

		Button character_wildcard = (Button) screen.findViewById(R.id.screen_search_list_keyboard_wildcard);
		Button character_z = (Button) screen.findViewById(R.id.screen_search_list_keyboard_z);
		Button character_x = (Button) screen.findViewById(R.id.screen_search_list_keyboard_x);
		Button character_c = (Button) screen.findViewById(R.id.screen_search_list_keyboard_c);
		Button character_v = (Button) screen.findViewById(R.id.screen_search_list_keyboard_v);
		Button character_b = (Button) screen.findViewById(R.id.screen_search_list_keyboard_b);
		Button character_n = (Button) screen.findViewById(R.id.screen_search_list_keyboard_n);
		Button character_m = (Button) screen.findViewById(R.id.screen_search_list_keyboard_m);
		Button character_del = (Button) screen.findViewById(R.id.screen_search_list_keyboard_del);

		character_0.setOnClickListener(this);
		character_1.setOnClickListener(this);
		character_2.setOnClickListener(this);
		character_3.setOnClickListener(this);
		character_4.setOnClickListener(this);
		character_5.setOnClickListener(this);
		character_6.setOnClickListener(this);
		character_7.setOnClickListener(this);
		character_8.setOnClickListener(this);
		character_9.setOnClickListener(this);

		character_q.setOnClickListener(this);
		character_w.setOnClickListener(this);
		character_e.setOnClickListener(this);
		character_r.setOnClickListener(this);
		character_t.setOnClickListener(this);
		character_y.setOnClickListener(this);
		character_u.setOnClickListener(this);
		character_i.setOnClickListener(this);
		character_o.setOnClickListener(this);
		character_p.setOnClickListener(this);

		character_a.setOnClickListener(this);
		character_s.setOnClickListener(this);
		character_d.setOnClickListener(this);
		character_f.setOnClickListener(this);
		character_g.setOnClickListener(this);
		character_h.setOnClickListener(this);
		character_j.setOnClickListener(this);
		character_k.setOnClickListener(this);
		character_l.setOnClickListener(this);
		character_question.setOnClickListener(this);

		character_wildcard.setOnClickListener(this);
		character_z.setOnClickListener(this);
		character_x.setOnClickListener(this);
		character_c.setOnClickListener(this);
		character_v.setOnClickListener(this);
		character_b.setOnClickListener(this);
		character_n.setOnClickListener(this);
		character_m.setOnClickListener(this);
		character_del.setOnClickListener(this);
		character_del.setOnLongClickListener(this);
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		Message message = new Message();
		switch (v.getId()) {		
		case R.id.screen_search_list_keyboard_del:
			message.what = IMediaService.MSG_WHAT_DELETE_LONGCLICK;
			message.obj = "DEL";
			Bundle b_del = new Bundle();
			b_del.putInt("left", v.getLeft());
			message.setData(b_del);
			break;
		}
		handler.sendMessage(message);		
		return true;
	}

/*	class myThread implements Runnable {   
		private Handler handler;  
		myThread(Handler h){
			  handler = h;
		}
		
		public void run() {  
		  try {
			Thread.sleep(500);
			Message message = new Message();
			message.what = IMediaService.MSG_WHAT_BIGWORD_HIDE;
		    handler.sendMessage(message);
		  } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
		}
	} */
}
