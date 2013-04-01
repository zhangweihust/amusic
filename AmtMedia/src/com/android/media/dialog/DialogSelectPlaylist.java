package com.android.media.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.adapter.AudioDialogPlaylistAdapter;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.screens.Screen;
import com.android.media.services.impl.ServiceManager;
import com.android.media.view.CustomDialog;

public class DialogSelectPlaylist implements View.OnClickListener {
	private Dialog selectPlaylist;
	private ListView dialogPlaylistListView;
	private TextView createPlaylist;
	private EditText createPlaylistName;
	private int id;
	private AudioDialogPlaylistAdapter dialogPlaylistAdapter;
	private MediaManagerDB db;
	private Screen screen;
	private Context context;
	private Dialog dialog;
	private static OnScreenHint mOnScreenHint;
	public DialogSelectPlaylist(Screen screen) {
		db = ServiceManager.getMediaService().getMediaDB();
		context = ServiceManager.getAmtMedia();
		selectPlaylist = new Dialog(context, R.style.CustomDialog);
		selectPlaylist.setCanceledOnTouchOutside(true);
		selectPlaylist.setContentView(R.layout.screen_audio_dialog_playlists);
		dialogPlaylistListView = (ListView) selectPlaylist
				.findViewById(R.id.screen_audio_dialog_playlists_listview);		
		View headview=LayoutInflater.from(ServiceManager.getAmtMedia()).inflate(R.layout.screen_audio_dialog_playlist_head, null);
		createPlaylist=(TextView) headview.findViewById(R.id.screen_audio_dialog_playlists_headview);		
		dialogPlaylistListView.addHeaderView(headview);
		headview.setOnClickListener(this);		
		this.screen = screen;
		Cursor playlistsCursor = db.queryPlaylists();
		((Activity) screen).startManagingCursor(playlistsCursor);
		dialogPlaylistAdapter = new AudioDialogPlaylistAdapter(screen,
				playlistsCursor, screen);
		dialogPlaylistListView.setAdapter(dialogPlaylistAdapter);

	}

	public void show(int id) {
		this.id = id;
		refresh();
		selectPlaylist.show();
	}

	public void refresh() {
		Cursor playlistsCursor = db.queryPlaylists();
		((Activity) screen).startManagingCursor(playlistsCursor);
		dialogPlaylistAdapter.changeCursor(playlistsCursor);
	}

	public void dismiss() {
		selectPlaylist.dismiss();
	}

	public void registerOnItemClickListener(OnItemClickListener listener) {
		dialogPlaylistListView.setOnItemClickListener(listener);
	}

	public int getAudioId() {
		return id;
	}

	@Override
	public void onClick(View v) {
		dismiss();
		final CustomDialog.Builder customBuilder = new CustomDialog.Builder(
				ServiceManager.getAmtMedia());
		customBuilder
				.setTitle(
						context.getString(R.string.screen_audio_playlist_create_title))
				.setWhichViewVisible(CustomDialog.contentIsEditText)
				.setPositiveButton(
						context.getString(R.string.screen_audio_playlist_edit_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								ContentValues values = null;
								String name = createPlaylistName
										.getEditableText().toString().trim();
								if (name != null && name.length() > 0) {
									values = new ContentValues();
									values.put(
											MediaDatabaseHelper.COLUMN_PLAYLIST_NAME,
											name);
									values.put(
											MediaDatabaseHelper.COLUMN_PLAYLIST_CREATE_TIME,
											System.currentTimeMillis());
									values.put(
											MediaDatabaseHelper.COLUMN_PLAYLIST_UPDATE_TIME,
											System.currentTimeMillis());
									boolean bool = db.addPlaylist(values);
									if (!bool) {
										ServiceManager.getAmtMediaHandler()
												.post(new Runnable() {

													@Override
													public void run() {
														mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_audio_playlists_exist));
						    							mOnScreenHint.show();
													}
												});
									} else {
										values = new ContentValues();
										values.put(
												MediaDatabaseHelper.COLUMN_AUDIO_PLAYLIST_ID,
												db.queryPlaylists(0));
										values.put(
												MediaDatabaseHelper.COLUMN_AUDIO_SYSTEM_ID,
												id);
										db.addAudioToPlaylist(values);
										ServiceManager
												.getMediaEventService()
												.onMediaUpdateEvent(
														new MediaEventArgs()
																.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_PLAYLIST_COUNT));
										ServiceManager.getAmtMediaHandler().post(new Runnable() {
											@Override
											public void run() {
												if(mOnScreenHint!=null){
												    mOnScreenHint.cancel();
												}
												mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_create_new_addto_success));
												mOnScreenHint.show();
											}
										});
//										mOnScreenHint = OnScreenHint.makeText(ServiceManager.getAmtMedia(), ServiceManager.getAmtMedia().getString(R.string.screen_create_new_addto_success));
//		    							mOnScreenHint.show();
									}
								}
								createPlaylistName.setText("");
								dialog.dismiss();
								if (name != null && name.length() > 0) {
									refresh();
								}
								
							}
						})
				.setNegativeButton(
						context.getString(R.string.screen_audio_playlist_create_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								createPlaylistName.setText("");
								dialog.dismiss();
							}
						});
		dialog = customBuilder.create();
		createPlaylistName = customBuilder.getEditText();
		dialog.show();
	}
}
