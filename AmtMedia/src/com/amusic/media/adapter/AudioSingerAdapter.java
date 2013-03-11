package com.amusic.media.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amusic.media.MediaApplication;
import com.amusic.media.R;
import com.amusic.media.dialog.DialogSelectPlaylist;
import com.amusic.media.provider.MediaDatabaseHelper;
import com.amusic.media.provider.MediaManagerDB;
import com.amusic.media.screens.Screen;
import com.amusic.media.screens.impl.ScreenAudio;
import com.amusic.media.services.impl.ServiceManager;
import com.amusic.media.view.RemoteImageView;

public class AudioSingerAdapter extends CursorAdapter implements OnItemClickListener{
	private LayoutInflater inflater;
	private MediaManagerDB db;
	private Screen screen;
	private Integer audioId;
	private ListView mListView;
	private int position;
	private DialogSelectPlaylist dialogSelectPlaylist;
	public AudioSingerAdapter(Context context, Cursor c, Screen screen, ListView list) {
		super(context, c);
		inflater = LayoutInflater.from(context);
		db = ServiceManager.getMediaService().getMediaDB();
		this.screen = screen;
		audioId = screen.getHighlightId();
		mListView = list;
		dialogSelectPlaylist = new DialogSelectPlaylist(screen);
		dialogSelectPlaylist.registerOnItemClickListener(this);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.screen_audio_singer_item, null);
		AudioSinger audioSinger = new AudioSinger();
		audioSinger.image = (RemoteImageView) view.findViewById(R.id.screen_audio_singer_icon);
		audioSinger.name = (TextView) view.findViewById(R.id.screen_audio_singer_name);
		audioSinger.count = (TextView) view.findViewById(R.id.screen_audio_singer_count);
		view.setTag(audioSinger);
		return view;
	}

	
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		this.position = position;
		return view;
	}

	@Override
	public void bindView(View view, Context context, final Cursor cursor) {
		final AudioSinger audioSinger = (AudioSinger) view.getTag();
		final String artist = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME));
		audioSinger.image.setDefaultImage(R.drawable.screen_audio_item_singers_bg);
		audioSinger.image.setImageUrl(artist, position ,mListView);
		final int id = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ID));
		String name = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME));
		final Cursor c = db.querySingerAudios(name);
		audioSinger.name.setText(name);
		
		if (audioId != null && audioId == id) {
			audioSinger.name.setTextColor(MediaApplication.color_highlight);
			audioSinger.count.setTextColor(MediaApplication.color_highlight);
			screen.setHighlightScreenAudioId(ScreenAudio.SCREEN_AUDIO_SINGERS);
		} else {
			audioSinger.name.setTextColor(Color.WHITE);
			audioSinger.count.setTextColor(Color.WHITE);
		}
		String resultsText = String.format(context.getResources().getString(R.string.screen_audio_songs_count), String.valueOf(c.getCount())); 
		audioSinger.count.setText(resultsText);
		c.close();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor cursor = db.queryPlaylistAudios((int) id);
		int auId=dialogSelectPlaylist.getAudioId();
		boolean bool = false; // 该歌曲是否已经存在该列表中，如果存在，返回true 反之，返回false
		 while (cursor.moveToNext()) {
			int systemId = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SID));
			if (systemId == auId) {
				bool = true;
				break;
			}
		}
		if (!bool) {
			ContentValues values = new ContentValues();
			values.put(MediaDatabaseHelper.COLUMN_AUDIO_PLAYLIST_ID, id);
			values.put(MediaDatabaseHelper.COLUMN_AUDIO_SYSTEM_ID,
					dialogSelectPlaylist.getAudioId());
			db.addAudioToPlaylist(values);
			Toast.makeText(screen, screen.getString(R.string.screen_create_new_addto_success),Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(screen, screen.getString(R.string.screen_create_new_playlist_exist),Toast.LENGTH_LONG).show();
		}
		dialogSelectPlaylist.dismiss();
	}
	

	private class AudioSinger {
		private RemoteImageView image;
		private TextView name;
		private TextView count;
	}

	@Override
	public void changeCursor(Cursor cursor) {
		audioId = screen.getHighlightId();
		super.changeCursor(cursor);
	}
}
