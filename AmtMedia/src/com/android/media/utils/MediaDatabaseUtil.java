package com.android.media.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;

import com.android.media.MediaApplication;
import com.android.media.provider.MediaCategoryDatabaseHelper;
import com.android.media.provider.MediaDictionaryDatabaseHelper;

public class MediaDatabaseUtil {
	
	public static boolean copyDatabase(String fileBaseName, int partCount,
			String databasePath, String databaseFileName) {
		File dir = new File(databasePath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		File file = new File(databasePath + "/" + databaseFileName);
		if (!file.exists()) {
			createDatabaseFile(fileBaseName, partCount, file);
		} else {
			SQLiteDatabase db = MediaApplication.getContext()
					.openOrCreateDatabase(databaseFileName, 0, null);
			int version = db.getVersion();
			db.close();
			MediaApplication.logD(MediaDatabaseUtil.class, "version = "
					+ version);
			if (databaseFileName.equals(MediaDictionaryDatabaseHelper.NAME)) {
				if (version < MediaDictionaryDatabaseHelper.version) {
					MediaApplication.logD(MediaDatabaseUtil.class, "MediaDictionaryDatabaseHelper.version = "
							+ MediaDictionaryDatabaseHelper.version);
					file.delete();
					createDatabaseFile(fileBaseName, partCount, file);
				}
			} else if (databaseFileName
					.equals(MediaCategoryDatabaseHelper.NAME)) {
				if (version < MediaCategoryDatabaseHelper.version) {
					MediaApplication.logD(MediaDatabaseUtil.class, "MediaCategoryDatabaseHelper.version = "
							+ MediaCategoryDatabaseHelper.version);
					file.delete();
					createDatabaseFile(fileBaseName, partCount, file);
				}
			}
		}
		return false;
	}

	private static void createDatabaseFile(String fileBaseName, int partCount, File file) {
			InputStream fis = null;
			FileOutputStream fos = null;
			AssetManager assetManager = MediaApplication.getInstance()
					.getAssets();
			try {
				if(!file.exists()){
					file.createNewFile();
				}
				fos = new FileOutputStream(file);
				byte[] buffer = new byte[8 * 1024];
				int count = 0;
				// 开始复制dictionary.db文件
				for (int i = 0; i < partCount; i++) {
					try {
						fis = assetManager.open(fileBaseName + i);
						while ((count = fis.read(buffer)) != -1) {
							fos.write(buffer, 0, count);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							if (fis != null) {
								fis.close();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
}
