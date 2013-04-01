package com.android.media.dialog;

import java.io.File;
import java.io.IOException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import android.app.Dialog;
import android.database.Cursor;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.amusic.media.R;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.screens.Screen;
import com.android.media.services.impl.ServiceManager;

public class DialogSongAttributes {

	private TextView tv_artist;
	private TextView tv_title;
	private TextView tv_ablum;
    private TextView tv_duration;
    private TextView tv_size;
    private TextView tv_year;
    private TextView tv_genre;
    private TextView tv_position;
    private TextView tv_comment;
	private Button btn_ok;        
	private Button btn_cancel;

	private String filename = null;
	private Dialog songAttributesDialog;
	
	private String orginal_name = "";
	private String orginal_artist = "";
	private String orginal_album = "";
	private int orginal_duration = 0;
	private int orginal_size = 0;
	private String orginal_year = "";
	private String orginal_genre = "";
	private String orginal_position = "";
	private String orginal_comment="";
	private Cursor cursor;
	private int songID;
	/*
	 * private String orginal_year = ""; private String orginal_genre_selected =
	 * "";
	 */
	private static String[] m = { "Blues", "Classic Rock", "Country", "Dance",
			"Rock", "R&B", "Jazz", "Rap", "other" };

	/* private Map<String, String> map=null; */

	public DialogSongAttributes(Screen screen) {
		songAttributesDialog = new Dialog(ServiceManager.getAmtMedia(),
				R.style.CustomDialog);
		songAttributesDialog.setCanceledOnTouchOutside(true);
		songAttributesDialog.setContentView(R.layout.screen_audio_look_song_info);
		init();
	}

	private void init() {

		tv_title = (TextView) songAttributesDialog
				.findViewById(R.id.tv_look_audio_song_name); 
		tv_artist = (TextView) songAttributesDialog
				.findViewById(R.id.tv_look_audio_artist); 
		tv_ablum = (TextView) songAttributesDialog
				.findViewById(R.id.tv_look_audio_album); 
        tv_duration = (TextView) songAttributesDialog.findViewById(R.id.tv_look_audio_song_duration);
        tv_size = (TextView) songAttributesDialog.findViewById(R.id.tv_look_audio_song_size);
        tv_year = (TextView) songAttributesDialog.findViewById(R.id.tv_look_audio_song_year);
        tv_genre = (TextView) songAttributesDialog.findViewById(R.id.tv_look_audio_song_genre);
        tv_position =(TextView) songAttributesDialog.findViewById(R.id.tv_look_audio_song_position);
        tv_comment = (TextView) songAttributesDialog.findViewById(R.id.tv_look_audio_song_comment);
		// 将可选内容与adapter联系起来
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				songAttributesDialog.getContext(),
				android.R.layout.simple_spinner_item, m);
		// 设置下拉列表的风格
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		// 将adapter添加到Spinner中
		// audio_genre.setAdapter(adapter);

		btn_ok = (Button) songAttributesDialog.findViewById(R.id.btn_ok_look);
		btn_cancel = (Button) songAttributesDialog
				.findViewById(R.id.btn_cancel_look);

		btn_ok.setOnClickListener(btn_ok_listener);
		btn_cancel.setOnClickListener(btn_cancel_listener);

	}

	public void show(int id) {
		songID = id;
		
		MediaManagerDB db = ServiceManager.getMediaService().getMediaDB();
		cursor = db.querySongById(id);

		// int genre_id=db.getGenreIdByAudioId(id);
		// Log.i("流派id=",genre_id+"");

		if (cursor != null && cursor.getCount() != 0) {
			cursor.moveToFirst();
			// 文件绝对路径
			filename = cursor.getString(cursor
					.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
			// 文件名称（歌曲名称）
			orginal_name = cursor.getString(cursor
					.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SONGNAME));
			// orginal_name=new
			// String(orginal_name.getBytes("iso-8859-1"),"gb2312");
			/* orginal_name=new String(orginal_name.getBytes("gbk"),"utf-8"); */
			// 歌手名称
			orginal_artist = cursor.getString(cursor
					.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ARTISTNAME));
			// orginal_artist=new
			// String(orginal_artist.getBytes("iso-8859-1"),"utf-8");
			// 专辑
			orginal_album = cursor.getString(cursor
					.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_ALBUMNAME));

			orginal_duration = cursor.getInt(cursor
					.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_DURATION));
			
			orginal_size = cursor.getInt(cursor
					.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_SIZE));
			
			orginal_position = cursor.getString(cursor
					.getColumnIndex(MediaDatabaseHelper.COLUMN_AMPLAY_SONGS_FILEPATH));
			
			if (accept(filename)) {
				try {
					File sf = new File(filename);
					if (sf != null && sf.exists()) {
						AudioFile af = AudioFileIO.read(sf);
						Tag t = null;
						t = af.getTag();
						if(t != null){
							orginal_year = t.getFirst(FieldKey.YEAR);
							orginal_comment = t.getFirst(FieldKey.COMMENT);
							orginal_genre = t.getFirst(FieldKey.GENRE);
						}
					}
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TagException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReadOnlyFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidAudioFrameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CannotReadException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			//修改控件属性
			tv_title.setText(ServiceManager.getAmtMedia().getString(R.string.screen_audio_song_options_attributes_song_name) + orginal_name); // temp test
			tv_artist.setText(ServiceManager.getAmtMedia().getString(R.string.screen_audio_song_options_attributes_singer_name) + orginal_artist); // temp test
			tv_ablum.setText(ServiceManager.getAmtMedia().getString(R.string.screen_audio_song_options_attributes_album_name) + orginal_album); // temp test
			tv_duration.setText(ServiceManager.getAmtMedia().getString(R.string.screen_audio_song_options_attributes_song_duration) + formatTime(orginal_duration)); // temp test
			tv_size.setText(ServiceManager.getAmtMedia().getString(R.string.screen_audio_song_options_attributes_song_size) + Formatter.formatFileSize(songAttributesDialog.getContext(), orginal_size));
		    tv_year.setText(ServiceManager.getAmtMedia().getString(R.string.screen_audio_song_options_attributes_song_year) + orginal_year);
		    tv_genre.setText(ServiceManager.getAmtMedia().getString(R.string.screen_audio_song_options_attributes_song_genre) + orginal_genre);
		    tv_position.setText(ServiceManager.getAmtMedia().getString(R.string.screen_audio_song_options_attributes_song_position) + orginal_position);
		    tv_comment.setText(ServiceManager.getAmtMedia().getString(R.string.screen_audio_song_options_attributes_song_comment) + orginal_comment);
		    
		    if(orginal_year == null || orginal_year.trim().equals("")){
		    	tv_year.setVisibility(View.GONE);
		    	TextView tv_line0 = (TextView)songAttributesDialog.findViewById(R.id.line_0);
		    	tv_line0.setVisibility(View.GONE);
		    }
		    if(orginal_genre == null || orginal_genre.trim().equals("")){
		    	tv_genre.setVisibility(View.GONE);
		    	TextView tv_line1 = (TextView)songAttributesDialog.findViewById(R.id.line_1);
		    	tv_line1.setVisibility(View.GONE);
		    }
		    if(orginal_comment == null || orginal_comment.trim().equals("")){
		    	tv_comment.setVisibility(View.GONE);
		    	TextView tv_line2 = (TextView)songAttributesDialog.findViewById(R.id.line_2);
		    	tv_line2.setVisibility(View.GONE);
		    }
		
		}
		songAttributesDialog.show();
	}

	public void dismiss() {
		songAttributesDialog.dismiss();
		if(cursor != null)
			cursor.close();
	}

	private View.OnClickListener btn_ok_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			dismiss();         
			//点击编辑时跳转到编辑页面
			SreenAudioSongInfoEdit songInfoEdit = new SreenAudioSongInfoEdit();
			songInfoEdit.show(songID);

		}
	};

	public boolean accept(String filename) {
		final String[] styles = { ".mp3" };
		int start = filename.lastIndexOf(".");
		if (start != -1) {
			String style = filename.substring(start);
			for (String s : styles) {
				if (s.equals(style.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	private static String formatTime(int times) {
		times /= 1000;
		int minutes = times / 60;
		int seconds = times % 60;
		minutes %= 60;
		return String.format("%02d:%02d", minutes, seconds);
		//return MessageFormat.format("{1,number,00}:{2,number,00}",  time / 1000 / 60 % 60, time / 1000 % 60);
	}
	private View.OnClickListener btn_cancel_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			dismiss();
		}
	};
}