package com.android.media.dialog;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.utils.Constant;

public class SkinFontColorPreference extends DialogPreference implements OnClickListener{
	private TextView foregroundColorText,backgroundColorText;
	private ImageView foregroundColorImg,backgroundColorImg;
	private GridView foregroundGridView,backgroundGridView;
	private SharedPreferences sp;
	private Context context;
	private int BACKGROUNDCOLOR = Constant.BACKGROUNDCOLOR;
	private int FOREGROUNDCOLOR = Constant.FOREGROUNDCOLOR;
	private SkinFontColorAdapter foregroundColorAdapter,backgroundColorAdapter;
	public SkinFontColorPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onBindDialogView(View view) {
		// TODO Auto-generated method stub
		super.onBindDialogView(view);
		makeAdapters();
		foregroundColorText = (TextView) view.findViewById(R.id.foregroundColor);
		backgroundColorText = (TextView) view.findViewById(R.id.backgroundColor);
		foregroundColorImg = (ImageView) view.findViewById(R.id.foregroundColor_img);
		backgroundColorImg = (ImageView) view.findViewById(R.id.backgroundColor_img);
		foregroundGridView = (GridView) view.findViewById(R.id.foregroundColor_gridview);
		backgroundGridView = (GridView) view.findViewById(R.id.backgroundColor_gridview);
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
				FOREGROUNDCOLOR = Constant.FOREGROUNDCOLOR;
				switch (viewId) {
				case R.drawable.skin_fontcolor1:
					FOREGROUNDCOLOR = 0xFFAFE5E7;
					break;
				case R.drawable.skin_fontcolor2:
					FOREGROUNDCOLOR = 0xFF25F301;
					break;
				case R.drawable.skin_fontcolor3:
					FOREGROUNDCOLOR = 0xFF00AAFF;
					break;
				case R.drawable.skin_fontcolor4:
					FOREGROUNDCOLOR = 0xFFFF01BB;
					break;
				case R.drawable.skin_fontcolor5:
					FOREGROUNDCOLOR = 0xFFFFFFFF;
					break;
				case R.drawable.skin_fontcolor6:
					FOREGROUNDCOLOR = 0xFF000000;
					break;
				case R.drawable.skin_fontcolor7:
					FOREGROUNDCOLOR = 0xFFFFF700;
					break;
				case R.drawable.skin_fontcolor8:
					FOREGROUNDCOLOR = 0xFFFE7800;
					break;
				}
			}
		});
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
				BACKGROUNDCOLOR = Constant.BACKGROUNDCOLOR;
				switch (viewId) {
				case R.drawable.skin_fontcolor1:
					BACKGROUNDCOLOR = 0xFFAFE5E7;
					break;
				case R.drawable.skin_fontcolor2:
					BACKGROUNDCOLOR = 0xFF25F301;
					break;
				case R.drawable.skin_fontcolor3:
					BACKGROUNDCOLOR = 0xFF00AAFF;
					break;
				case R.drawable.skin_fontcolor4:
					BACKGROUNDCOLOR = 0xFFFF01BB;
					break;
				case R.drawable.skin_fontcolor5:
					BACKGROUNDCOLOR = 0xFFFFFFFF;
					break;
				case R.drawable.skin_fontcolor6:
					BACKGROUNDCOLOR = 0xFF000000;
					break;
				case R.drawable.skin_fontcolor7:
					BACKGROUNDCOLOR = 0xFFFFF700;
					break;
				case R.drawable.skin_fontcolor8:
					BACKGROUNDCOLOR = 0xFFFE7800;
					break;
				}

			}
		});
		foregroundColorText.setOnClickListener(this);
		backgroundColorText.setOnClickListener(this);
		foregroundColorImg.setVisibility(View.VISIBLE);
		backgroundColorImg.setVisibility(View.INVISIBLE);
		foregroundGridView.setAdapter(foregroundColorAdapter);
		
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// TODO Auto-generated method stub
		if(positiveResult){
			Constant.FOREGROUNDCOLOR = FOREGROUNDCOLOR;
			Constant.BACKGROUNDCOLOR = BACKGROUNDCOLOR;
			MediaApplication.color_highlight=Constant.FOREGROUNDCOLOR;
		    MediaApplication.color_normal=Constant.BACKGROUNDCOLOR;
			sp = getSharedPreferences();
			Editor editor = sp.edit();
			editor.putInt(Constant.SoftParametersSetting.skin_foregroundColor_key, Constant.FOREGROUNDCOLOR);
			editor.putInt(Constant.SoftParametersSetting.skin_backgroundColor_key, Constant.BACKGROUNDCOLOR);
			editor.commit();
		}else{
			BACKGROUNDCOLOR = Constant.BACKGROUNDCOLOR;
			FOREGROUNDCOLOR = Constant.FOREGROUNDCOLOR;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
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
		foregroundColorAdapter = new SkinFontColorAdapter(context, foregroundColors);

		ArrayList<Integer> backgroundColors = new ArrayList<Integer>();
		backgroundColors.add(R.drawable.skin_fontcolor1);
		backgroundColors.add(R.drawable.skin_fontcolor2);
		backgroundColors.add(R.drawable.skin_fontcolor3);
		backgroundColors.add(R.drawable.skin_fontcolor4);
		backgroundColors.add(R.drawable.skin_fontcolor5);
		backgroundColors.add(R.drawable.skin_fontcolor6);
		backgroundColors.add(R.drawable.skin_fontcolor7);
		backgroundColors.add(R.drawable.skin_fontcolor8);
		backgroundColorAdapter = new SkinFontColorAdapter(context, backgroundColors);	
	}
	
	public class SkinFontColorAdapter extends BaseAdapter{
		private Context context;
		private ArrayList<Integer> SkinFontColor = new ArrayList<Integer>();
		public SkinFontColorAdapter(Context context,ArrayList<Integer> SkinFontColor){
			this.context=context;
			this.SkinFontColor = SkinFontColor;
		}
		
		public ArrayList<Integer> getMenuData(){
			return this.SkinFontColor;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return SkinFontColor.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return SkinFontColor.get(position);
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
				convertView = inflater.inflate(R.layout.skin_fongcolor_item, null);
				holder.fongColorIcon = (ImageView) convertView.findViewById(R.id.fongColor_image);
				convertView.setTag(holder);
			}else{
				holder = (HolderView) convertView.getTag();
			}
			holder.fongColorIcon.setImageResource(SkinFontColor.get(position));
			holder.fongColorIcon.setTag(SkinFontColor.get(position));
			return convertView;
		}
		
		class HolderView {
			private ImageView fongColorIcon;
		}
	}

	
}