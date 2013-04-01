package com.android.media.adapter;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.services.impl.ServiceManager;

public class AudioScanAddDirAdapter extends BaseAdapter {

	private static final String rootdir = "/";
	
	private ArrayList<String> filelist;
	private ArrayList<String> adddirlist;
	private String CurPath;
	private Context context;
	private String gobackstr;
	private ListView listview;
	
	private class Element
	{
		private LinearLayout item;
		private ImageView itemicon;
		private TextView itempath;
		private Button selected;
		private boolean value = false;
		private boolean sendBroadcastFlag = false;
	}
	
	public AudioScanAddDirAdapter(Context context,ListView lv)
	{
		this.context = context;
		gobackstr = context.getResources().getString(R.string.screen_scan_adddir_goback_parent);
		filelist = new ArrayList<String>();
		adddirlist = new ArrayList<String>();
		setFilelistByCurPath(rootdir);
		listview = lv;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return filelist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return filelist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final Element element;
		
		if (convertView == null)
		{
			element = new Element();
			convertView = LayoutInflater.from(context).inflate(R.layout.screen_scan_select_path, null);
			element.item = (LinearLayout)convertView.findViewById(R.id.screen_scan_select_file_item);
			element.itemicon = (ImageView)convertView.findViewById(R.id.screen_scan_item_image);
			element.itempath = (TextView)convertView.findViewById(R.id.screen_scan_file_path);
			element.selected = (Button)convertView.findViewById(R.id.screen_scan_select);
			convertView.setTag(element);
		}
		else
		{
			element = (Element) convertView.getTag();
		}
		element.itempath.setText(filelist.get(position));
		if (adddirlist.contains(filelist.get(position)))
		{
			element.selected.setBackgroundResource(R.drawable.screen_audio_playlist_edit_checked_true);
			element.value = true;
		}
		else
		{
			element.selected.setBackgroundResource(R.drawable.screen_audio_playlist_edit_checked_false);
			element.value = false;
		}
		if (filelist.get(position).equals(gobackstr))
		{
			element.itemicon.setImageDrawable(context.getResources().getDrawable(R.drawable.scangoback_normal));
			element.selected.setVisibility(View.GONE);
		}
		else
		{
			element.itemicon.setImageDrawable(context.getResources().getDrawable(R.drawable.fileicon));
			element.selected.setVisibility(View.VISIBLE);
		}
		
		OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (position >= filelist.size())
				{
					return;
				}
				if (filelist.get(position).equals(gobackstr))
				{
					gobackParent();
					return;
				}
				switch (v.getId()) {
				case R.id.screen_scan_select_file_item:
					setFilelistByCurPath(filelist.get(position));
					if (listview != null)
					{
						listview.setSelection(0);
					}
					break;

				case R.id.screen_scan_select:
					element.value = !element.value;
					if (element.value)
					{
						adddirlist.add(filelist.get(position));
						element.selected.setBackgroundResource(R.drawable.screen_audio_playlist_edit_checked_true);
						if (!element.sendBroadcastFlag)
						{
							element.sendBroadcastFlag = true;
							ServiceManager.getMediaScanner().scanOneDir(filelist.get(position));
							
						}
					}
					else
					{
						adddirlist.remove(filelist.get(position));
						element.selected.setBackgroundResource(R.drawable.screen_audio_playlist_edit_checked_false);
					}
					break;
				
				default:
					break;
				}
			}
		};
		
		convertView.setOnClickListener(listener);
		element.selected.setOnClickListener(listener);
		
		OnTouchListener touchlistener = new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				switch (event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
					element.item.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.screen_list_item_press));
					if (filelist.get(position).equals(gobackstr))
					{
						element.itemicon.setImageDrawable(context.getResources().getDrawable(R.drawable.scangoback_highlight));
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					element.item.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.screen_list_item));
					if (filelist.get(position).equals(gobackstr))
					{
						element.itemicon.setImageDrawable(context.getResources().getDrawable(R.drawable.scangoback_normal));
//						gobackParent();
					}
//					else
//					{
//						setFilelistByCurPath(filelist.get(position));
//					}
					break;
				default:
					break;
				}
				
				return false;
			}
		};
		
		convertView.setOnTouchListener(touchlistener);
		
		return convertView;
	}

	
	private void setFilelistByCurPath(String path)
	{
		String filepath;
		CurPath = path;
		filelist.clear();
		filelist.add(gobackstr);
		File file = new File(CurPath);
		if (file.exists())
		{
			File[] files = file.listFiles();
			if (files != null)
			{
				for (File f : files)
				{
					if (f.isDirectory())
					{
						filepath = f.getAbsolutePath();
						if (!filepath.matches(MediaApplication.deleteRecordPath))
						{
							filelist.add(filepath);
						}
					}
				}
			}
		}
		notifyDataSetChanged();
	}
	
	public void gobackParent()
	{
		if (CurPath.equals(rootdir))
		{
			return;
		}
		File file = new File(CurPath);
		if (file.exists())
		{
			setFilelistByCurPath(file.getParent());
		}
	}
	
	public ArrayList<String> getAddDir()
	{
		return adddirlist;
	}
	
	public void refresh()
	{
		if (MediaApplication.getInstance().getClearFlag() == true)
		{
			clearAddDirList();
			MediaApplication.getInstance().setClearFlag(false);
		}
		
		setFilelistByCurPath(rootdir);
	}
	
	public void clearAddDirList()
	{
		if (adddirlist != null)
		{
			adddirlist.clear();
		}
	}
}
