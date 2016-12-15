package com.example.updateversion.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;
import java.util.Map;

public class GlobalSettings {

	private static final String SETTINGS_FILE_NAME = "com.example.updateversion";

	// app update
	public static void saveNewVersionInfo(Context context, String verNum,
			String url, String features, boolean canUpdate) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		if (verNum != null && verNum != "") {
			editor.putString("version", verNum);
		}
		if (url != null && url != "") {
			editor.putString("version_url", url);
		}
		if (features != null && features != "") {
			editor.putString("features", features);
		}
		editor.putBoolean("updatable", canUpdate);
		editor.commit();
	}

	public static Map<String, Object> getNewVersionInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
		String versionString = sharedPreferences.getString("version", "");
		String versionUrl = sharedPreferences.getString("version_url", "");
		String versionFeatures = sharedPreferences.getString("features", "");
		boolean canUpdate = sharedPreferences.getBoolean("updatable", false);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("version", versionString);
		map.put("version_url", versionUrl);
		map.put("features", versionFeatures);
		map.put("updatable", canUpdate);

		return map;
	}

	/**
	 * 保存完成下载
	 *
	 * @param context
	 * @param isFinish
	 */
	public static void saveIsFinishDownload(Context context, boolean isFinish) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean("finishDownload", isFinish);
		editor.commit();
	}

	public static boolean getIsFinishDownload(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
		boolean isFinish = sharedPreferences
				.getBoolean("finishDownload", false);
		return isFinish;
	}

}
