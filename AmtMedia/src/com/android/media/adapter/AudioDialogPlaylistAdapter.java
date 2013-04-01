package com.android.media.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.screens.Screen;
import com.android.media.services.impl.ServiceManager;

public class AudioDialogPlaylistAdapter extends CursorAdapter {

	private LayoutInflater inflater;
	private MediaManagerDB db;

	public AudioDialogPlaylistAdapter(Context context, Cursor c, Screen screen) {
		super(context, c);
		inflater = LayoutInflater.from(context);
		db = ServiceManager.getMediaService().getMediaDB();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.screen_audio_dialog_playlist_item, null);
		AudioPlaylist audioPlaylist = new AudioPlaylist();
		audioPlaylist.dialogPlaylist = (TextView) view.findViewById(R.id.screen_audio_dialog_playlist_name);
		view.setTag(audioPlaylist);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final AudioPlaylist audioPlaylist = (AudioPlaylist) view.getTag();
		String name = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_PLAYLIST_NAME));
		Cursor c = db.queryPlaylistAudios(cursor.getInt(cursor.getColumnIndex((MediaDatabaseHelper.COLUMN_PLAYLIST_ID))));
		name = name + " ( " + String.format(context.getResources().getString(R.string.screen_audio_songs_count), String.valueOf(c.getCount())) + ") ";
		audioPlaylist.dialogPlaylist.setText(name);
		c.close();
	}

	private class AudioPlaylist {
		private TextView dialogPlaylist;
	}
}
