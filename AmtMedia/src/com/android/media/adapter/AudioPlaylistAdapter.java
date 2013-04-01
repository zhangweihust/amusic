package com.android.media.adapter;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.MediaApplication;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.model.ScreenArgs;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.screens.Screen;
import com.android.media.screens.IScreen.ScreenType;
import com.android.media.screens.impl.ScreenAudio;
import com.android.media.screens.impl.ScreenAudioAlbumSongs;
import com.android.media.screens.impl.ScreenAudioPlayer;
import com.android.media.screens.impl.ScreenAudioPlaylistEdit;
import com.android.media.services.IAudioScreenService;
import com.android.media.services.IMediaPlayerService;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.ToastUtil;
import com.android.media.view.CustomDialog;

public class AudioPlaylistAdapter extends CursorAdapter{

	private LayoutInflater inflater;
	private MediaManagerDB db;
	private Screen screen;
	private IAudioScreenService audioScreenService;
	private EditText createPlaylistName;
	private Dialog dialog;
	private ContentValues values = new ContentValues();
    private Integer audioId;

	public AudioPlaylistAdapter(Context context, Cursor c, Screen screen) {
		super(context, c);
		inflater = LayoutInflater.from(context);
		db = ServiceManager.getMediaService().getMediaDB();
		this.screen = screen;
	    audioId = screen.getHighlightId();
		audioScreenService = ServiceManager.getAudioScreenService();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.screen_audio_playlist_item, null);
		AudioPlaylist audioPlaylist = new AudioPlaylist();
		// audioPlaylist.playing = (ImageView) view.findViewById(R.id.screen_audio_playing);
		// audioPlaylist.item = (LinearLayout) view.findViewById(R.id.screen_audio_playlist_item);
		audioPlaylist.playlist = (TextView) view.findViewById(R.id.screen_audio_playlist_name);
		audioPlaylist.count = (TextView) view.findViewById(R.id.screen_audio_playlist_count);
		audioPlaylist.options = (Button) view.findViewById(R.id.screen_audio_playlist_options);
		View popView = inflater.inflate(R.layout.screen_audio_playlist_options, null);
		audioPlaylist.optionsWindow = new PopupWindow(popView, ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_width), LayoutParams.WRAP_CONTENT);
		audioPlaylist.optionsWindow.setFocusable(true);
		audioPlaylist.optionsWindow.setBackgroundDrawable(new BitmapDrawable());
		audioPlaylist.popPlaylist = (TextView) popView.findViewById(R.id.screen_audio_playlist_options_playlist);
		audioPlaylist.play = (Button) popView.findViewById(R.id.screen_audio_playlist_options_play);
		audioPlaylist.addSongs = (Button) popView.findViewById(R.id.screen_audio_playlist_options_addsongs);
		audioPlaylist.rename = (Button) popView.findViewById(R.id.screen_audio_playlist_options_rename);
		audioPlaylist.favorite = (CheckBox) popView.findViewById(R.id.screen_audio_playlist_options_favorite);
		audioPlaylist.delete = (Button) popView.findViewById(R.id.screen_audio_playlist_options_delete);
//		audioPlaylist.cleanup = (Button) popView.findViewById(R.id.screen_audio_playlist_options_cleanup);
		view.setTag(audioPlaylist);
		return view;
	}

	@Override
	public void bindView(View view, Context context, final Cursor cursor) {
		final AudioPlaylist audioPlaylist = (AudioPlaylist) view.getTag();
		final int id = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_PLAYLIST_ID));
		 if (audioId != null && audioId == id) {
			 audioPlaylist.playlist.setTextColor(MediaApplication.color_highlight);
			 audioPlaylist.count.setTextColor(MediaApplication.color_highlight);
			 screen.setHighlightScreenAudioId(ScreenAudio.SCREEN_AUDIO_PLAYLISTS);
		 } else {
			 audioPlaylist.playlist.setTextColor(Color.WHITE);
			 audioPlaylist.count.setTextColor(Color.WHITE);
		 }
		final Cursor c = db.queryPlaylistAudios(cursor.getInt(cursor.getColumnIndex((MediaDatabaseHelper.COLUMN_PLAYLIST_ID))));
		 
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.screen_audio_playlist_options:
					int[] location = new int[2];
					v.getLocationInWindow(location);
					int xoff = audioPlaylist.optionsWindow.getWidth();
					int yoff = ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_maginTop);
					int height = ServiceManager.getAmtMedia().getResources().getDimensionPixelSize(R.dimen.popwindow_item_height);
					if(location[1] + 4 * height > MediaApplication.getSoftInScreenHeight()){
						yoff = 4 * height;
					}
					audioPlaylist.options.setBackgroundResource(R.drawable.screen_audio_song_options_open_large);
					audioPlaylist.optionsWindow.showAsDropDown(v, -xoff, -yoff);
					break;
				case R.id.screen_audio_playlist_options_play:
					audioPlaylist.optionsWindow.dismiss();
					ServiceManager.getMediaplayerService().changeCorsor(c, IMediaPlayerService.MEDIA_MODEL_LOCAL);
					ScreenArgs args = new ScreenArgs();
					args.putExtra("screenType", ScreenType.TYPE_AUDIO);
					int playId = -1;
					if(c.moveToFirst()){
						playId = c.getInt(c.getColumnIndex(MediaStore.Audio.Media._ID));
					}
					args.putExtra("id", playId);
					args.putExtra("position", 0);
					args.putExtra("screenId", ScreenAudioAlbumSongs.class.getCanonicalName());
					ServiceManager.getAmtScreenService().show(ScreenAudioPlayer.class, args, View.GONE);
					break;
				case R.id.screen_audio_playlist_options_addsongs:
					audioPlaylist.optionsWindow.dismiss();
					audioScreenService.show(ScreenAudioPlaylistEdit.class, new ScreenArgs().putExtra("id", id));
					break;
				case R.id.screen_audio_playlist_options_rename:
					final CustomDialog.Builder customBuilder = new CustomDialog.Builder(ServiceManager.getAmtMedia());
					customBuilder.setTitle(screen.getString(R.string.screen_create_new_playlist_rename))
					.setWhichViewVisible(CustomDialog.contentIsEditText)
					.setPositiveButton(screen.getString(R.string.screen_create_new_playlist_ok), 
			            		new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int which) {
			                	String name = createPlaylistName.getEditableText().toString().trim();
			        			if (name != null && name.length() > 0) {
			        				ContentValues values = new ContentValues();
			        				values.put(MediaDatabaseHelper.COLUMN_PLAYLIST_NAME, name);
			        				values.put(MediaDatabaseHelper.COLUMN_PLAYLIST_UPDATE_TIME, System.currentTimeMillis());
			        				if (!db.renamePlaylist(id, values)) {
			        					ServiceManager.getAmtMediaHandler().post(new Runnable() {

			        						@Override
			        						public void run() {
			        							Toast toast = ToastUtil.getInstance().getToast(screen.getString(R.string.screen_audio_playlists_exist));
			        							toast.setDuration(Toast.LENGTH_SHORT);
			        							toast.setGravity(Gravity.CENTER, 0, 0);
			        							toast.show();
			        						}
			        					});
			        				}
			        				createPlaylistName.setText("");
			        			}
			        			dialog.dismiss();
			        			if (name != null && name.length() > 0) {
			        				screen.refresh();
			        			}
			                }
			            })
			            .setNegativeButton(screen.getString(R.string.screen_create_new_playlist_cancel), 
			            		new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int which) {
			                	createPlaylistName.setText("");
			        			dialog.dismiss();
			                }
			            });
					dialog = customBuilder.create();
					createPlaylistName = customBuilder.getEditText();
					dialog.show();			
					audioPlaylist.optionsWindow.dismiss();
					break;
				case R.id.screen_audio_playlist_options_delete:
					db.deletePlaylist(id);
					audioPlaylist.optionsWindow.dismiss();
					screen.refresh();
					ScreenAudio.refreshCount(ScreenAudio.REFRESH_PLAYLISTS_COUNT);
					break;
//				case R.id.screen_audio_playlist_options_cleanup:
//					db.deletePlaylistSongs(id);
//					audioPlaylist.optionsWindow.dismiss();
//					screen.refresh();
//					break;
				}
			}
		};
		audioPlaylist.optionsWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				audioPlaylist.options.setBackgroundResource(R.drawable.screen_audio_song_options_large);
			}
		});
		audioPlaylist.options.setOnClickListener(listener);
		audioPlaylist.play.setOnClickListener(listener);
		audioPlaylist.addSongs.setOnClickListener(listener);
		audioPlaylist.rename.setOnClickListener(listener);
		audioPlaylist.delete.setOnClickListener(listener);
//		audioPlaylist.cleanup.setOnClickListener(listener);
		audioPlaylist.favorite.setOnCheckedChangeListener(null);
		if (db.audioIsFavorite(id)) {
			audioPlaylist.favorite.setChecked(true);
		} else {
			audioPlaylist.favorite.setChecked(false);
		}
		audioPlaylist.favorite.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Cursor cursor = db.queryPlaylistAudios(id);
				int audioId = -1;
				if (isChecked) {
					while(cursor.moveToNext()){
						audioId = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AUDIO_ID));
						values.clear();
						values.put(MediaDatabaseHelper.COLUMN_FAVORITY_MEDIA_ID, audioId);
						db.addAudioToFavorite(values);
					}
					cursor.close();
				} else {
					while(cursor.moveToNext()){
						audioId = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_AUDIO_ID));
						db.deleteAudioFromFavority(audioId);
					}
					cursor.close();
				}
				ServiceManager.getMediaEventService().onMediaUpdateEvent(new MediaEventArgs()
				.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_FAVORITE_COUNT));
				audioPlaylist.optionsWindow.dismiss();
			}
		});
		String name = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.COLUMN_PLAYLIST_NAME));
		audioPlaylist.popPlaylist.setText(name);
		audioPlaylist.playlist.setText(name);
		
		String resultsText = String.format(context.getResources().getString(R.string.screen_audio_songs_count), String.valueOf(c.getCount())); 
		audioPlaylist.count.setText(resultsText);
		c.close();
	}

	private class AudioPlaylist {
		private TextView playlist;
		// private LinearLayout item;
		private TextView count;
		private PopupWindow optionsWindow;
		private Button play;
		private Button addSongs;
		private Button rename;
		private Button delete;
//		private Button cleanup;
		private Button options;
		private TextView popPlaylist;
		private CheckBox favorite;
		// private ImageView playing;
	}

	@Override
	public void changeCursor(Cursor cursor) {
		audioId = screen.getHighlightId();
		super.changeCursor(cursor);
	}
}
