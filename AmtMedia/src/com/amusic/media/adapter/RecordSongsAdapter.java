package com.amusic.media.adapter;


import java.io.File;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.dialog.DialogEditSignature;
import com.amusic.media.dialog.DialogSelectPlaylist;
import com.amusic.media.dialog.OnScreenHint;
import com.amusic.media.model.SongInfo;
import com.amusic.media.screens.Screen;
import com.amusic.media.screens.impl.ScreenRecord;
import com.amusic.media.services.impl.MediaPlayerService;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.view.CustomDialog;
import com.amusic.media.view.RemoteImageView;

public class RecordSongsAdapter extends BaseAdapter implements OnItemClickListener{
	private ArrayList<SongInfo> recordSongs;
	private LayoutInflater inflater;
	private DialogSelectPlaylist dialogSelectPlaylist;
	private Screen screen;
	private Dialog deleteDialog;
	private ListView mListView;
	public static OnScreenHint mOnScreenHint;
	
	public ArrayList<SongInfo> getRecordSongs(){
		return recordSongs;
	}
	
	public RecordSongsAdapter(Context context, Screen screen, ListView list){
		this.screen = screen;
		mListView = list;
		inflater = LayoutInflater.from(context);
		dialogSelectPlaylist = new DialogSelectPlaylist(screen);
		dialogSelectPlaylist.registerOnItemClickListener(this);
	}
	public void setRecordSongs(ArrayList<SongInfo> kmediaSongs){
		this.recordSongs = kmediaSongs;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return recordSongs != null ? recordSongs.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return recordSongs != null ? recordSongs.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View view, ViewGroup parent) {
		RecordSong recordSong;
		if(view == null){
			recordSong = new RecordSong();
			view = inflater.inflate(R.layout.screen_record_song_item, null);
			recordSong.image = (RemoteImageView) view.findViewById(R.id.screen_record_singer_icon);
			recordSong.song = (TextView) view.findViewById(R.id.screen_record_song_song);
			recordSong.singer = (TextView) view.findViewById(R.id.screen_record_song_singer);
			recordSong.options = (Button) view.findViewById(R.id.screen_record_song_options);
			recordSong.dateLayout = (LinearLayout) view.findViewById(R.id.screen_record_date_layout);
			recordSong.options.setVisibility(View.VISIBLE);
			recordSong.dateLayout.setVisibility(View.VISIBLE);
			recordSong.duration = (TextView)view.findViewById(R.id.screen_record_music_duration);
			recordSong.date = (TextView)view.findViewById(R.id.screen_record_music_date);
			View popView = inflater.inflate(R.layout.screen_kmedia_song_options, null);
			recordSong.optionsWindow = new PopupWindow(popView, ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_width), LayoutParams.WRAP_CONTENT);
			recordSong.optionsWindow.setFocusable(true);
			recordSong.optionsWindow.setBackgroundDrawable(new BitmapDrawable());
			recordSong.popSong = (TextView) popView.findViewById(R.id.screen_kmedia_song_options_song);
			recordSong.delete = (Button) popView.findViewById(R.id.screen_kmedia_song_options_delete);
			recordSong.delete.setText(screen.getString(R.string.screen_record_delete));
			recordSong.edit_signature = (Button)popView.findViewById(R.id.screen_kmedia_song_options_edit_signature);
			recordSong.edit_signature.setText(screen.getString(R.string.screen_record_player_edit_signature));
			recordSong.accompaniment = (Button) popView.findViewById(R.id.screen_kmedia_song_options_accompaniment);
			recordSong.original = (Button) popView.findViewById(R.id.screen_kmedia_song_options_original);
			recordSong.back = (Button) popView.findViewById(R.id.screen_kmedia_song_options_back);
			recordSong.save = (CheckBox) popView.findViewById(R.id.screen_kmedia_song_options_save);
			popView.findViewById(R.id.screen_kmedia_song_options_line1).setVisibility(View.GONE);
			popView.findViewById(R.id.screen_kmedia_song_options_line2).setVisibility(View.GONE);
			popView.findViewById(R.id.screen_kmedia_song_options_line3).setVisibility(View.GONE);
			popView.findViewById(R.id.screen_kmedia_song_options_line4).setVisibility(View.VISIBLE);
			recordSong.accompaniment.setVisibility(View.GONE);
			recordSong.original.setVisibility(View.GONE);
			recordSong.back.setVisibility(View.GONE);
			recordSong.save.setVisibility(View.GONE);
			view.setTag(recordSong);
		}else{
			recordSong = (RecordSong) view.getTag();
		}
		
		final RecordSong finalRecordSong = recordSong;
		final SongInfo songInfo = recordSongs.get(position);
		if(songInfo != null){
			final String singerName = songInfo.getSingerName();
			recordSong.image.setDefaultImage(R.drawable.screen_audio_item_singers_bg);
			recordSong.image.setImageUrl(singerName, position ,mListView);
			String songName = songInfo.getSongName();
			recordSong.song.setText(songName);
			recordSong.singer.setText(singerName);

			String fullDirectory = songInfo.getDirectory();
			
			String date = "";
			String durationInfo = "";
			String duration = "";
			String extraInfo = "";
			if(fullDirectory.contains("_mp3_")){
				extraInfo = fullDirectory.substring(fullDirectory.lastIndexOf("_mp3_")+"_mp3_".length(),fullDirectory.length()-".mp3".length());
			}
			if(fullDirectory.contains("_wav_")){
				extraInfo = fullDirectory.substring(fullDirectory.lastIndexOf("_wav_")+"_wav_".length(),fullDirectory.length()-".wav".length());
			}
			if(extraInfo.contains("_")){
				 date = extraInfo.substring(0, extraInfo.indexOf("_"));
				 if(extraInfo.indexOf("_")+1 < extraInfo.length()){
					 durationInfo = extraInfo.substring(extraInfo.indexOf("_")+1,extraInfo.length());
				 }
				 
			 }
			if(durationInfo.contains("_") && durationInfo.indexOf("_")+1 < durationInfo.length()){
				duration = durationInfo.substring(0, durationInfo.indexOf("_"))+":"+durationInfo.substring(durationInfo.indexOf("_")+1,durationInfo.length());
			}

			recordSong.duration.setText(duration);
			recordSong.date.setText(date);

			
			recordSong.popSong.setText(songName);
			
//			final Uri ringtoneUri = Uri.fromFile(new File(songInfo.getDirectory()));
			OnClickListener listener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					switch (v.getId()) {
					case R.id.screen_record_song_options:
						int[] location = new int[2];
						v.getLocationInWindow(location);
						int xoff = finalRecordSong.optionsWindow.getWidth();
						int yoff = ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_maginTop);
						int height = ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_height);
						if(location[1] + 3 * height > MediaApplication.getSoftInScreenHeight()){
							yoff = 3 * height;
						}
						finalRecordSong.options.setBackgroundResource(R.drawable.screen_audio_song_options_open_large);
						finalRecordSong.optionsWindow.showAsDropDown(v, -xoff, -yoff);
						break;
					case R.id.screen_kmedia_song_options_delete:
						finalRecordSong.optionsWindow.dismiss();
						deleteDialog(position);
						break;
					case R.id.screen_kmedia_song_options_edit_signature:
						finalRecordSong.optionsWindow.dismiss();
						String fullDirectory = recordSongs.get(position).getDirectory();
						int dirLen = MediaPlayerService.directoryRecord.length();
						String tmp = fullDirectory.substring(dirLen, fullDirectory.length());
						String songName = "";
						if(tmp.contains("-") && tmp.contains("_") && tmp.indexOf("-")+1<tmp.indexOf("_")+1){
							songName = tmp.substring(tmp.indexOf("-")+1, tmp.indexOf("_")+1);
						}
						
						String editSignature = "";
						if(tmp.contains("_") && tmp.contains("_mp3") && tmp.indexOf("_")+1< tmp.lastIndexOf("_mp3")){
							editSignature = tmp.substring(tmp.indexOf("_")+1,tmp.lastIndexOf("_mp3"));
						}
						else if(tmp.contains("_") && tmp.contains("_wav") && tmp.indexOf("_")+1< tmp.lastIndexOf("_wav")){
							editSignature = tmp.substring(tmp.indexOf("_")+1,tmp.lastIndexOf("_wav"));
						}

						DialogEditSignature dialogEditSignature = new DialogEditSignature(ServiceManager.getAmtMedia(),songName,editSignature,fullDirectory);
						dialogEditSignature.show();
					}
				}
			};
			recordSong.optionsWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					finalRecordSong.options.setBackgroundResource(R.drawable.screen_audio_song_options_large);
				}
			});
			recordSong.options.setOnClickListener(listener);
			recordSong.delete.setOnClickListener(listener);
			recordSong.edit_signature.setOnClickListener(listener);

		}
		return view;
	}
	
	private void deleteDialog(final int position){
		final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ServiceManager.getAmtMedia());
		customBuilder.setTitle(screen.getResources().getString(R.string.delete_propt))
		.setWhichViewVisible(CustomDialog.contentIsTextView)
		.setMessage(screen.getResources().getString(R.string.delete_sure_or_not))
		.setPositiveButton(screen.getString(R.string.screen_record_ok), 
            		new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	String directory = recordSongs.get(position).getDirectory();
        			File file = new File(directory);
        			if(file.exists()){
        				file.delete();
        				recordSongs.remove(position);
        				if(recordSongs.size() <= 0){
        					((ScreenRecord)screen).setEmpty();
        				}else{
        					notifyDataSetChanged();
        				}
        				ServiceManager.getMediaScanner().scanOneFile(directory);
        			}
        			deleteDialog.dismiss();
					ServiceManager.getAmtMediaHandler().post(new Runnable() {
						@Override
						public void run() {
							if(mOnScreenHint!=null){
							    mOnScreenHint.cancel();
							}
							mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_record_delete_success));
							mOnScreenHint.show();
						}
					});
//        			mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_record_delete_success));
//					mOnScreenHint.show();
                }
            }).setNegativeButton(screen.getString(R.string.screen_record_cancel), 
            		new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	deleteDialog.dismiss();
                }
            });
		deleteDialog = customBuilder.create();
		deleteDialog.show();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
	
	private class RecordSong {
		private RemoteImageView image;
		private TextView song;
		private TextView singer;
		private Button options;
		private LinearLayout dateLayout;
		private TextView duration;
		private TextView date;
		private PopupWindow optionsWindow;
		private Button original;
		private Button accompaniment;
		private Button back;
		private Button delete;
		private CheckBox save;
		private Button edit_signature;
		private TextView popSong;
	}
}
