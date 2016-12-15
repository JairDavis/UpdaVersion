package com.example.updateversion.utils;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;

public class FileUtils {

	public static final String CACHDIR = "AppCache";

	public static final int FREE_SD_SPACE_NEEDED_TO_CACHE = 10;

	private static final int MB = 1024 * 1024;

	public static boolean isSDCardOK() {
		boolean ok = false;
		// read and write
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			ok = true;
		}

		return ok;
	}

	public static File createFileInSd(String fileName) throws Exception {
		String SDPATH = Environment.getExternalStorageDirectory()
				+ File.separator;
		File file = new File(SDPATH + fileName);
		file.createNewFile();
		return file;
	}

	public static File createDirectoryInCache(String dirName) throws Exception {
		String SDPATH = Environment.getExternalStorageDirectory()
				+ File.separator + CACHDIR + File.separator;
		File dir = new File(SDPATH + dirName);
		dir.mkdir();
		return dir;
	}

	public static File createCacheDirectory() throws Exception {
		String path = Environment.getExternalStorageDirectory()
				+ File.separator + CACHDIR;
		File dir = new File(path);
		if (!dir.exists()) {
			System.out.println("cache dir created");
			dir.mkdir();
		}
		return dir;
	}

	public static boolean checkFileExistence(String fileName) {
		String SDPATH = Environment.getExternalStorageDirectory()
				+ File.separator + CACHDIR + File.separator;
		File file = new File(SDPATH + fileName);
		return file.exists();
	}

	public static boolean isDirEmpty(String fileName) {
		boolean result = false;
		File file = new File(fileName);

		if (file.isDirectory()) {
			if (file.listFiles().length == 0) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * get directory size (byte)
	 * 
	 * @param path
	 * @return size of dir
	 */
	public static float getDirSize(String path) {

		float size = 0.0F;
		if (isSDCardOK()) {
			try {

				File file = new File(path);
				if (file.isDirectory()) {
					File[] fileList = file.listFiles();
					for (int i = 0; i < fileList.length; i++) {
						if (fileList[i].isDirectory()) {
							size = size
									+ getDirSize(fileList[i].getAbsolutePath());
						} else {
							size = size + fileList[i].length();
						}
					}
				}

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

		// System.out.println("size:" + size);
		return size;
	}

	public static void deleteFileInDir(String filePath, boolean isDeleteDir) {
		if (!TextUtils.isEmpty(filePath)) {
			File file = new File(filePath);

			if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					deleteFileInDir(files[i].getAbsolutePath(), true);
				}
			}

			// if also delete the dir
			if (isDeleteDir) {
				if (!file.isDirectory()) {
					file.delete();
				} else {
					if (file.listFiles().length == 0) {
						file.delete();
					}
				}
			}
		}
	}

	public static boolean isEnoughFreeSpace() {

		boolean isEnough = false;

		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());

		double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat
				.getBlockSize()) / 1024 / 1024;

		if (sdFreeMB > FREE_SD_SPACE_NEEDED_TO_CACHE) {
			isEnough = true;
		}

		return isEnough;

	}

	/**
	 * 计算SD卡上的剩余空间
	 **/

	private static int freeSpaceOnSd() {

		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());

		double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat
				.getBlockSize()) / MB;

		return (int) sdFreeMB;

	}

	/*
	 * Java文件操作 获取文件扩展名
	 * 
	 * Created on: 2011-8-2 Author: blueeagle
	 */
	public static String getExtensionName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length() - 1))) {
				return filename.substring(dot + 1);
			}
		}
		return filename;
	}

	/*
	 * Java文件操作 获取不带扩展名的文件名
	 * 
	 * Created on: 2011-8-2 Author: blueeagle
	 */
	public static String getFileNameNoEx(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length()))) {
				return filename.substring(0, dot);
			}
		}
		return filename;
	}

	public static String getCacheApkPath(String fileName) {
		return Environment.getExternalStorageDirectory() + File.separator
				+ CACHDIR + File.separator + fileName;
	}

	public static void deleteFiles(String path) {
		File file = new File(path);
		// 1級文件刪除
		if (!file.isDirectory()) {
			file.delete();
		} else if (file.isDirectory()) {
			// 2級文件列表
			String[] filelist = file.list();
			// 獲取新的二級路徑
			for (int j = 0; j < filelist.length; j++) {
				File filessFile = new File(path + "\\" + filelist[j]);
				if (!filessFile.isDirectory()) {
					filessFile.delete();
				} else if (filessFile.isDirectory()) {
					// 遞歸調用
					deleteFiles(path + "\\" + filelist[j]);
				}
			}
			file.delete();
		}
	}

	public static void deleteApkFile(String absoulePath) {
		File file = new File(absoulePath);
		if (getExtensionName(file.getName()).equals("apk")) {
			file.delete();
		}
	}

	/**
	 * 获得apk文件名
	 * 
	 * @param versionName
	 * @return
	 */
	public static String getApkFileName(String versionName) {
		return new StringBuffer().append("app_v").append(versionName)
				.append(".apk").toString();
	}

}
