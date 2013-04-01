package com.android.media.dialog;

import java.io.File;
import java.io.IOException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import android.app.Dialog;
import android.database.Cursor;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amusic.media.R;
import com.android.media.event.impl.MediaEventArgs;
import com.android.media.event.impl.MediaEventTypes;
import com.android.media.provider.MediaDatabaseHelper;
import com.android.media.provider.MediaManagerDB;
import com.android.media.services.impl.ServiceManager;
import com.android.media.utils.ToastUtil;

public class SreenAudioSongInfoEdit {
	private Dialog songInfoEditdlg;
	private Button btn_ok;
	private Button btn_cancel;
	private EditText audio_artist;
	private EditText audio_title;
	private EditText audio_album;

	private String filename = null;
	private String orginal_name = "";
	private String orginal_artist = "";
	private String orginal_album = "";
	private Cursor cursor;
	private MediaManagerDB db ;
	private int songID;
	private static OnScreenHint mOnScreenHint;
	public SreenAudioSongInfoEdit() {
		songInfoEditdlg = new Dialog(ServiceManager.getAmtMedia(),
				R.style.CustomDialog);
		songInfoEditdlg.setCanceledOnTouchOutside(true);
		songInfoEditdlg.setContentView(R.layout.screen_audio_modify_song_info);
		db = ServiceManager.getMediaService().getMediaDB();
		init();
	}

	public void init() {
		btn_ok = (Button) songInfoEditdlg.findViewById(R.id.btn_ok);
		btn_cancel = (Button) songInfoEditdlg.findViewById(R.id.btn_cancel);

		btn_ok.setOnClickListener(btn_ok_listener);
		btn_cancel.setOnClickListener(btn_cancel_listener);

		audio_artist = (EditText) songInfoEditdlg
				.findViewById(R.id.audio_artist);
		audio_title = (EditText) songInfoEditdlg.findViewById(R.id.audio_title);
		audio_album = (EditText) songInfoEditdlg.findViewById(R.id.audio_album);
		int lengthmax=80;//设置编辑的最大字符为80个字符
		lengthFilter(audio_artist, lengthmax, ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_edit_out_of_range));
		lengthFilter(audio_title, lengthmax, ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_edit_out_of_range));
		lengthFilter(audio_album, lengthmax, ServiceManager.getAmtMedia().getString(R.string.screen_audio_player_edit_out_of_range));

	}
	
	public static void lengthFilter(final EditText editText, final int max_length, final String msg) {
		InputFilter[] filters = new InputFilter[1];
		filters[0] = new InputFilter.LengthFilter(max_length) {
		    @Override
		   public CharSequence filter(CharSequence source, int start, int end,
		      Spanned dest, int dstart, int dend) {
		    int destLen = getCharacterNum(dest.toString()); //获取字符个数(一个中文算2个字符)
		    int sourceLen =  getCharacterNum(source.toString()); 
		    if (destLen + sourceLen > max_length) {
//		         mOnScreenHint=OnScreenHint.makeText(ServiceManager.getAmtMedia(),msg);
//		         mOnScreenHint.setPosition(Gravity.TOP|Gravity.LEFT, 20, 200);
//		         mOnScreenHint.show();	
//		         
		     	Toast toast = ToastUtil.getInstance().getToast(
		     			msg);
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();

		         return "";
		    }
		    return source;
		 }
		};
		
		editText.setFilters(filters);
	}
	
	public static int getCharacterNum(final String content) {
		if (null == content || "".equals(content)) {
		return 0;
		}else {
		return (content.length() + getChineseNum(content));
		}
		}
	
	public static int getChineseNum(String s) {

		int num = 0;
		char[] myChar = s.toCharArray();
		for (int i = 0; i < myChar.length; i++) {
		if ((char)(byte)myChar[i] != myChar[i]) {
		num++;
		}
		}
		return num;
		}
	
	private View.OnClickListener btn_ok_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			dismiss();
			if(audio_artist.isEnabled()){
				updateSongInfo();
			}
		}
	};

	private View.OnClickListener btn_cancel_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			dismiss();
		}
	};
	

	public void show(int id) {
		songID = id;
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
			if (orginal_artist.equals("<unknown>")) {
				audio_artist.setText("");
			} else
				audio_artist.setText(orginal_artist);

			if (orginal_name.equals("<unknown>")) {
				audio_title.setText("");
			} else {
				audio_title.setText(orginal_name);
			}

			if (orginal_album.equals("<unknown>")) {
				audio_album.setText("");
			} else {
				audio_album.setText(orginal_album);
			}

			if (accept(filename)) {
				audio_artist.setEnabled(true);
				audio_title.setEnabled(true);
				audio_album.setEnabled(true);
			} else {
				audio_artist.setEnabled(false);
				audio_title.setEnabled(false);
				audio_album.setEnabled(false);
				// audio_year.setEnabled(false);
				// audio_genre.setEnabled(false);

				Toast toast = ToastUtil.getInstance().getToast(
						ServiceManager.getAmtMedia().getString(R.string.song_info_edit_not_support));
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();

			}
		}
		songInfoEditdlg.show();
	}

	public void dismiss() {
		songInfoEditdlg.dismiss();
		if(cursor != null)
			cursor.close();
	}

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

	public void updateSongInfo() {
		String new_name = audio_title.getText().toString().trim();
		String new_artist = audio_artist.getText().toString().trim();
		String new_album = audio_album.getText().toString().trim();
		
		db.updateSongById(songID, new_name,new_artist,new_album);
		ServiceManager.getMediaEventService().onMediaUpdateEvent(new MediaEventArgs()
		.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_SINGER_COUNT));
		ServiceManager.getMediaEventService().onMediaUpdateEvent(new MediaEventArgs()
		.setMediaUpdateEventTypes(MediaEventTypes.AUDIO_UPDATE_ABLUM_COUNT));
		if (accept(filename)) {
			try {
				File sf = new File(filename);
				if (sf != null && sf.exists()) {
					AudioFile af = AudioFileIO.read(sf);
					AudioFileIO.delete(af);
					Tag t = null;
					t = af.getTag();
					String year = null;
					String comment = null;
					String genre = null;
					if(t != null){
						year = t.getFirst(FieldKey.YEAR);
						comment = t.getFirst(FieldKey.COMMENT);
						genre = t.getFirst(FieldKey.GENRE);
						//System.out.println(year + " " + comment + " " + genre);
					}
					Tag tag = null;
					if ((tag = af.createDefaultTag()) != null) {
						af.setTag(tag);
						tag.addField(FieldKey.TITLE, new_name);
						tag.addField(FieldKey.ARTIST, new_artist);
						tag.addField(FieldKey.ALBUM, new_album);
						if(year !=null && !year.equals("")){
							tag.addField(FieldKey.YEAR, year);
						}
						if(comment !=null && !comment.equals("")){
							tag.addField(FieldKey.COMMENT, comment);
						}
						if(genre !=null && !genre.equals("")){
							tag.addField(FieldKey.GENRE, genre);
						}
						af.commit();
					}else {
						Toast toast = ToastUtil.getInstance().getToast(
								"failure");
						toast.setDuration(Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
				} else {
					Toast toast = ToastUtil.getInstance().getToast(
							"file is not exist");
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
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
			} catch (CannotWriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CannotReadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}