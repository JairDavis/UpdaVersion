package com.example.updateversion.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.io.File;

public class ApkUtils {

	public static int getApkVersionCode(Context context, String filePath) {
		PackageManager manager = context.getPackageManager();
		PackageInfo packageInfo = manager.getPackageArchiveInfo(filePath,
				PackageManager.GET_ACTIVITIES);
		if (packageInfo != null) {
			return packageInfo.versionCode;
		}
		return 0;
	}

	public static String getApkVersionName(Context context, String filePath) {
		PackageManager manager = context.getPackageManager();
		PackageInfo packageInfo = manager.getPackageArchiveInfo(filePath,
				PackageManager.GET_ACTIVITIES);
		if (packageInfo != null) {
			return packageInfo.versionName;
		}
		return "";
	}

	public static String getApkPackageName(Context context, String filePath) {
		PackageManager manager = context.getPackageManager();
		PackageInfo packageInfo = manager.getPackageArchiveInfo(filePath,
				PackageManager.GET_ACTIVITIES);
		if (packageInfo != null) {
			return packageInfo.packageName;
		}
		return "";

	}

	public static String getApkShareUserId(Context context, String filePath) {
		PackageManager manager = context.getPackageManager();
		PackageInfo packageInfo = manager.getPackageArchiveInfo(filePath,
				PackageManager.GET_ACTIVITIES);
		if (packageInfo != null) {
			return packageInfo.sharedUserId;
		}
		return "";

	}

	/**
	 * 安装app
	 * 
	 * @param filePath
	 */
	public static void installApp(Context context, String filePath) {
		System.out.println("apk path:" + filePath);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(new File(filePath)),
				"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

}
