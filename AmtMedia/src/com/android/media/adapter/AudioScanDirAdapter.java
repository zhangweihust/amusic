package com.android.media.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.dialog.OnScreenHint;

public class AudioScanDirAdapter extends BaseAdapter/* implements OnClickListener*/ {
	private Context context;
	private List<String> filepathlist;
	private boolean[] filepathselected;
	private LayoutInflater inflater;
	private static final String RequiredPath = MediaApplication.ScanSavePath;
	
	public AudioScanDirAdapter(Context context)
	{
		super();
		this.context = context;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return filepathlist == null ? 0 : filepathlist.size() ;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return filepathlist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ScreenScanApaterElement scanviewitem;
		if (convertView == null)
		{
			scanviewitem = new ScreenScanApaterElement();
			convertView = inflater.inflate(R.layout.screen_scan_select_path, null);
			scanviewitem.itemicon = (ImageView)convertView.findViewById(R.id.screen_scan_item_image);
			scanviewitem.itempath = (TextView)convertView.findViewById(R.id.screen_scan_file_path);
			scanviewitem.selected = (Button)convertView.findViewById(R.id.screen_scan_select);
			convertView.setTag(scanviewitem);
		}
		else
		{
			scanviewitem = (ScreenScanApaterElement)convertView.getTag();
		}
		scanviewitem.itemicon.setImageDrawable(context.getResources().getDrawable(R.drawable.fileicon));
		scanviewitem.itempath.setText(filepathlist.get(position));
		if (filepathselected[position] == true)
		{
			scanviewitem.selected.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.screen_audio_playlist_edit_checked_true));
		}
		else
		{
			scanviewitem.selected.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.screen_audio_playlist_edit_checked_false));
		}
		
		OnClickListener listener = new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ScreenScanApaterElement scanviewitem = (ScreenScanApaterElement)v.getTag();
				
				if (RequiredPath.equals(filepathlist.get(position)))
				{
			        OnScreenHint.makeText(context, context.getResources().getString(R.string.screen_scan_adddir_amtplayer_tip)).show();
					return;
				}
				
				filepathselected[position] = !filepathselected[position];
				switch(v.getId())
				{
				case R.id.screen_scan_select:		
					if (filepathselected[position] == true)
					{
						((Button)v).setBackgroundDrawable(context.getResources().getDrawable(R.drawable.screen_audio_playlist_edit_checked_true));
					}
					else
					{
						((Button)v).setBackgroundDrawable(context.getResources().getDrawable(R.drawable.screen_audio_playlist_edit_checked_false));
					}
					
					break;
				case R.id.screen_scan_select_file_item:					
					if (filepathselected[position] == true)
					{
						scanviewitem.selected.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.screen_audio_playlist_edit_checked_true));
					}
					else
					{
						scanviewitem.selected.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.screen_audio_playlist_edit_checked_false));
					}
					break;
				}
			}
			
		};
		
		scanviewitem.selected.setOnClickListener(listener);
		convertView.setOnClickListener(listener);
		
		return convertView;
	}
	
	public void setFilepathlist(List<String> filepathlist) {
		this.filepathlist = filepathlist;
		if (filepathlist == null)
		{
			return;
		}
		filepathselected = new boolean[filepathlist.size()];
		for (int i = 0; i < filepathselected.length; i++)
		{
			filepathselected[i] = true;
		}
	}
	
	public boolean[] getFilepathSelected()
	{
		return filepathselected.length == 0 ? null:filepathselected;
	}
	
	public void setAllSelected(boolean bselected)
	{
		if (filepathselected != null && filepathselected.length > 0)
		{
			if (bselected)
			{
				for (int i = 0; i < filepathselected.length; i++)
				{
					filepathselected[i] = true;
				}
			}
			else
			{
				if (filepathlist.get(0).equals(RequiredPath))
				{
					filepathselected[0] = true;
				}
				else
				{
					filepathselected[0] = false;
				}
				
				for (int i = 1; i < filepathselected.length; i++)
				{
					filepathselected[i] = false;
				}
			}
			notifyDataSetChanged();
		}
	}
	
	private class ScreenScanApaterElement
	{
		private ImageView itemicon;
		private TextView itempath;
		private Button selected;
	}
	
}
