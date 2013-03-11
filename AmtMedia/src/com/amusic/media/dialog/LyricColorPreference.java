package com.amusic.media.dialog;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.lyric.player.LyricPlayer;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.utils.Constant;
public class LyricColorPreference implements OnClickListener{
	private TextView foregroundColorText,backgroundColorText;
	private ImageView foregroundColorImg,backgroundColorImg;
	private GridView foregroundGridView,backgroundGridView;
	private SharedPreferences sp;
	private Context context;
	private int LYRICBACKGROUNDCOLOR = Constant.LYRICBACKGROUNDCOLOR;
	private int LYRICFOREGROUNDCOLOR = Constant.LYRICFOREGROUNDCOLOR;
	private LyricFontColorAdapter foregroundColorAdapter,backgroundColorAdapter;
	private Dialog lyricColorDialog;
	private Button lyric_color_ok,lyric_color_cancel;

	public LyricColorPreference(Context context) {
		this.context = context;
		lyricColorDialog = new Dialog(ServiceManager.getAmtMedia(), R.style.CustomDialog);
		lyricColorDialog.setContentView(R.layout.dialog_lyric_color);
		lyricColorDialog.setCanceledOnTouchOutside(true);
		init();
	}

	private void init() {

		makeAdapters();
		lyric_color_ok = (Button) lyricColorDialog.findViewById(R.id.lyric_color_ok);
		lyric_color_cancel = (Button) lyricColorDialog.findViewById(R.id.lyric_color_cancel);
		foregroundColorText = (TextView) lyricColorDialog.findViewById(R.id.foregroundColor);
		backgroundColorText = (TextView) lyricColorDialog.findViewById(R.id.backgroundColor);
		foregroundColorImg = (ImageView) lyricColorDialog.findViewById(R.id.foregroundColor_img);
		backgroundColorImg = (ImageView) lyricColorDialog.findViewById(R.id.backgroundColor_img);
		foregroundGridView = (GridView) lyricColorDialog.findViewById(R.id.foregroundColor_gridview);
		backgroundGridView = (GridView) lyricColorDialog.findViewById(R.id.backgroundColor_gridview);
		backgroundGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int viewId = (Integer) ((FrameLayout) view).getChildAt(0)
				.getTag();
				View childView = ((FrameLayout) view).getChildAt(1);
				for(int index = 0; index < 8; index++){
					View tempView = parent.getChildAt(index);
					((FrameLayout) tempView).getChildAt(1).setVisibility(View.GONE);
				}
				childView.setVisibility(View.VISIBLE);
				LYRICBACKGROUNDCOLOR = Constant.LYRICBACKGROUNDCOLOR;
				/*switch (viewId) {
				case R.drawable.skin_fontcolor1:
					LYRICBACKGROUNDCOLOR = 0xFFAFE5E7;
					break;
				case R.drawable.skin_fontcolor2:
					LYRICBACKGROUNDCOLOR = 0xFF25F301;
					break;
				case R.drawable.skin_fontcolor3:
					LYRICBACKGROUNDCOLOR = 0xFF00AAFF;
					break;
				case R.drawable.skin_fontcolor4:
					LYRICBACKGROUNDCOLOR = 0xFFFF01BB;
					break;
				case R.drawable.skin_fontcolor5:
					LYRICBACKGROUNDCOLOR = 0xFFFFFFFF;
					break;
				case R.drawable.skin_fontcolor6:
					LYRICBACKGROUNDCOLOR = 0xFF000000;
					break;
				case R.drawable.skin_fontcolor7:
					LYRICBACKGROUNDCOLOR = 0xFFFFF700;
					break;
				case R.drawable.skin_fontcolor8:
					LYRICBACKGROUNDCOLOR = 0xFFFE7800;
					break;
				}*/
				switch (viewId) {
				case R.drawable.skin_backcolor1:
					LYRICBACKGROUNDCOLOR = 0xFF8CB7EC;
					break;
				case R.drawable.skin_backcolor2:
					LYRICBACKGROUNDCOLOR = 0xFF70C338;
					break;
				case R.drawable.skin_backcolor3:
					LYRICBACKGROUNDCOLOR = 0xFF0088CC;
					break;
				case R.drawable.skin_backcolor4:
					LYRICBACKGROUNDCOLOR = 0xFFC338BD;
					break;
				case R.drawable.skin_backcolor5:
					LYRICBACKGROUNDCOLOR = 0xFFC0C0C0;
					break;
				case R.drawable.skin_backcolor6:
					LYRICBACKGROUNDCOLOR = 0xFF404040;
					break;
				case R.drawable.skin_backcolor7:
					LYRICBACKGROUNDCOLOR = 0xFFC5BB3A;
					break;
				case R.drawable.skin_backcolor8:
					LYRICBACKGROUNDCOLOR = 0xFFC37538;
					break;
				}
			}
		});
		foregroundGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int viewId = (Integer) ((FrameLayout) view).getChildAt(0)
				.getTag();
				View childView = ((FrameLayout) view).getChildAt(1);
				for(int index = 0; index < 8; index++){
					View tempView = parent.getChildAt(index);
					((FrameLayout) tempView).getChildAt(1).setVisibility(View.GONE);
				}
				childView.setVisibility(View.VISIBLE);
				LYRICFOREGROUNDCOLOR = Constant.LYRICFOREGROUNDCOLOR;
				switch (viewId) {
				case R.drawable.skin_fontcolor1:
					LYRICFOREGROUNDCOLOR = 0xFFAFE5E7;
					break;
				case R.drawable.skin_fontcolor2:
					LYRICFOREGROUNDCOLOR = 0xFF25F301;
					break;
				case R.drawable.skin_fontcolor3:
					LYRICFOREGROUNDCOLOR = 0xFF00AAFF;
					break;
				case R.drawable.skin_fontcolor4:
					LYRICFOREGROUNDCOLOR = 0xFFFF01BB;
					break;
				case R.drawable.skin_fontcolor5:
					LYRICFOREGROUNDCOLOR = 0xFFFFFFFF;
					break;
				case R.drawable.skin_fontcolor6:
					LYRICFOREGROUNDCOLOR = 0xFF000000;
					break;
				case R.drawable.skin_fontcolor7:
					LYRICFOREGROUNDCOLOR = 0xFFFFF700;
					break;
				case R.drawable.skin_fontcolor8:
					LYRICFOREGROUNDCOLOR = 0xFFFE7800;
					break;
				}

			}
		});
		lyric_color_ok.setOnClickListener(this);
		lyric_color_cancel.setOnClickListener(this);
		foregroundColorText.setOnClickListener(this);
		backgroundColorText.setOnClickListener(this);
		foregroundColorImg.setVisibility(View.VISIBLE);
		backgroundColorImg.setVisibility(View.INVISIBLE);
		foregroundGridView.setAdapter(foregroundColorAdapter);
		
	}
	public void makeAdapters() {
		ArrayList<Integer> foregroundColors = new ArrayList<Integer>();
		foregroundColors.add(R.drawable.skin_fontcolor1);
		foregroundColors.add(R.drawable.skin_fontcolor2);
		foregroundColors.add(R.drawable.skin_fontcolor3);
		foregroundColors.add(R.drawable.skin_fontcolor4);
		foregroundColors.add(R.drawable.skin_fontcolor5);
		foregroundColors.add(R.drawable.skin_fontcolor6);
		foregroundColors.add(R.drawable.skin_fontcolor7);
		foregroundColors.add(R.drawable.skin_fontcolor8);
		foregroundColorAdapter = new LyricFontColorAdapter(context, foregroundColors);

		ArrayList<Integer> backgroundColors = new ArrayList<Integer>();
		backgroundColors.add(R.drawable.skin_backcolor1);
		backgroundColors.add(R.drawable.skin_backcolor2);
		backgroundColors.add(R.drawable.skin_backcolor3);
		backgroundColors.add(R.drawable.skin_backcolor4);
		backgroundColors.add(R.drawable.skin_backcolor5);
		backgroundColors.add(R.drawable.skin_backcolor6);
		backgroundColors.add(R.drawable.skin_backcolor7);
		backgroundColors.add(R.drawable.skin_backcolor8);
		backgroundColorAdapter = new LyricFontColorAdapter(context, backgroundColors);	
	}
	
	public class LyricFontColorAdapter extends BaseAdapter{
		private Context context;
		private ArrayList<Integer> lyricFontColor = new ArrayList<Integer>();
		public LyricFontColorAdapter(Context context,ArrayList<Integer> lyricFontColor){
			this.context = context;
			this.lyricFontColor = lyricFontColor;
		}
		
		public ArrayList<Integer> getMenuData(){
			return this.lyricFontColor;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return lyricFontColor.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return lyricFontColor.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			HolderView holder;
			if(convertView==null){
				LayoutInflater inflater = LayoutInflater.from(context);
				holder = new HolderView();
				convertView = inflater.inflate(R.layout.lyric_fongcolor_item, null);
				holder.fongColorIcon = (ImageView) convertView.findViewById(R.id.fongColor_image);
				convertView.setTag(holder);
			}else{
				holder = (HolderView) convertView.getTag();
			}
			holder.fongColorIcon.setImageResource(lyricFontColor.get(position));
			holder.fongColorIcon.setTag(lyricFontColor.get(position));
			return convertView;
		}
		
		class HolderView {
			private ImageView fongColorIcon;
		}
	}

	public void show() {
		lyricColorDialog.show();
	}

	public void dismiss() {
		lyricColorDialog.dismiss();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lyric_color_ok:
			Constant.LYRICFOREGROUNDCOLOR = LYRICFOREGROUNDCOLOR;
			Constant.LYRICBACKGROUNDCOLOR = LYRICBACKGROUNDCOLOR;
			sp = MediaApplication.getContext().getSharedPreferences("com.amusic.media_preferences",Context.MODE_WORLD_WRITEABLE);
			Editor editor = sp.edit();
			editor.putInt(Constant.SoftParametersSetting.lyric_foregroundColor_key, Constant.LYRICFOREGROUNDCOLOR);
			editor.putInt(Constant.SoftParametersSetting.lyric_backgroundColor_key, Constant.LYRICBACKGROUNDCOLOR);
			editor.commit();
			LyricPlayer lyricPlayer = ServiceManager.getMediaplayerService().getLyricplayer();
			lyricPlayer.setColor(Constant.LYRICBACKGROUNDCOLOR, Constant.LYRICFOREGROUNDCOLOR);
			dismiss();
			break;
		case R.id.lyric_color_cancel:
			LYRICFOREGROUNDCOLOR = Constant.LYRICFOREGROUNDCOLOR;
			LYRICBACKGROUNDCOLOR = Constant.LYRICBACKGROUNDCOLOR;
			dismiss();
			break;
		case R.id.foregroundColor:
			foregroundColorImg.setVisibility(View.VISIBLE);
			backgroundColorImg.setVisibility(View.INVISIBLE);
			if(foregroundGridView.getAdapter() == null){
				foregroundGridView.setAdapter(foregroundColorAdapter);
			}
			foregroundGridView.setVisibility(View.VISIBLE);
			backgroundGridView.setVisibility(View.GONE);
			break;
		case R.id.backgroundColor:
			backgroundColorImg.setVisibility(View.VISIBLE);
			foregroundColorImg.setVisibility(View.INVISIBLE);
			if(backgroundGridView.getAdapter() == null){
				backgroundGridView.setAdapter(backgroundColorAdapter);
			}
			backgroundGridView.setVisibility(View.VISIBLE);
			foregroundGridView.setVisibility(View.GONE);
			break;
		}
	}

}
