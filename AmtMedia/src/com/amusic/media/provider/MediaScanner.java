package com.amusic.media.provider;

import java.io.File;
import java.io.FileFilter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

public class MediaScanner {
	private Context context;
	public static final String MEDIA_SCAN_STARTED = "amtMedia.media.scan.started";
	public static final String MEDIA_SCAN_FINISHED = "amtMedia.media.scan.finished";
	private final String[] styles = { ".mp3"/*
											 * , ".mp4", ".wav", ".ogg", ".wma",
											 * ".flac"
											 */};
	private FileFilter mediaFilter = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			String fileName = pathname.getName();
			int start = fileName.lastIndexOf(".");
			if (start != -1) {
				String style = fileName.substring(start);
				for (String s : styles) {
					if (s.equals(style.toLowerCase())) {
						return true;
					}
				}
			}
			return false;
		}
	};

	private void scanFile(File file) {
		if (mediaFilter.accept(file)) {
			context.sendBroadcast(new Intent(
					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
		}
	}

	public void scan(File file) {
		context.sendBroadcast(new Intent(MEDIA_SCAN_STARTED));
		if (file.isFile()) {
			scanFile(file);
		} else if (file.isDirectory()) {
			scanDir(file);
		}
		context.sendBroadcast(new Intent(MEDIA_SCAN_FINISHED));
	}

	private void scanDir(File file) {
		File[] files = file.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isFile()) {
					scanFile(f);
				} else if (f.isDirectory()) {
					scanDir(f);
				}
			}
		}
	}

	public MediaScanner(Context context) {
		this.context = context;
	}

	public void deleteFile(File file) {
		context.getContentResolver().delete(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				MediaStore.Audio.Media.DATA + " =? ",
				new String[] { file.getAbsolutePath() });
	}

	public void scanOneDir(String dirPath)
	{
		File dirfile = new File(dirPath);
		if (dirfile.exists())
		{
			File[] files = dirfile.listFiles();
			if (files != null)
			{
				for (File f : files)
				{
					if (f.isFile())
					{
						scanOneFile(f.getAbsolutePath());
					}
				}
			}
		}
	}
	
	public void scanOneFile(String filePath) {
		// TODO Auto-generated method stub
		if (filePath != null && "is-fuck-null".equals(filePath))
			return;
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri = Uri.parse("file://" + filePath);
		intent.setData(uri);
		context.sendBroadcast(intent);

	}
}