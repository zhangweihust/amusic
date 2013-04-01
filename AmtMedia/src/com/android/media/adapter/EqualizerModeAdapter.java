package com.android.media.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amusic.media.R;

public class EqualizerModeAdapter extends BaseAdapter{
	private Context context; 
	 
    private List<String> list;
    
    public EqualizerModeAdapter(Context context, List<String> list) { 
    	 
        this.context = context; 
        this.list = list; 
 
    } 
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		 return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		 return list.get(position); 
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position; 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
        
        ViewHolder holder; 
        if (convertView==null) { 
            convertView=LayoutInflater.from(context).inflate(R.layout.screen_equalizer_settings_options_list_item, null); 
            holder=new ViewHolder(); 
             
            convertView.setTag(holder); 
             
            holder.modeItem=(TextView) convertView.findViewById(R.id.screen_equalizer_settings_mode); 
             
        } 
        else{ 
            holder=(ViewHolder) convertView.getTag(); 
        } 
        holder.modeItem.setText(list.get(position)); 
         
        return convertView; 
    
	}
	static class ViewHolder { 
        TextView modeItem; 
    } 
}
