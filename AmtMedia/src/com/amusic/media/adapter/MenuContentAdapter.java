package com.amusic.media.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amusic.media.R;
import com.amusic.media.model.MenuItem;

public class MenuContentAdapter extends BaseAdapter{
	private Context context;
	private ArrayList<MenuItem> menuContent = new ArrayList<MenuItem>();
	public MenuContentAdapter(Context context,ArrayList<MenuItem> menuContent){
		this.context=context;
		this.menuContent = menuContent;
	}
	
	public ArrayList<MenuItem> getMenuData(){
		return this.menuContent;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return menuContent.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return menuContent.get(position);
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
			convertView = inflater.inflate(R.layout.menu_item, null);
			holder.menuIcon = (ImageView) convertView.findViewById(R.id.item_image);
			holder.menuName = (TextView) convertView.findViewById(R.id.item_text);
			convertView.setTag(holder);
		}else{
			holder = (HolderView) convertView.getTag();
		}
		MenuItem item = menuContent.get(position);
		if(item!=null){
			holder.menuIcon.setImageResource(item.getResId());
			holder.menuName.setText(item.getResName());
			holder.menuName.setTag(item.getResId());
		}
		return convertView;
	}
	
	class HolderView {
		private ImageView menuIcon;
		private TextView menuName;
	}
}

