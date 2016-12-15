package com.example.updateversion.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.List;

public class Utils {
	/**
	 * 获取Android系统版本
	 * 
	 * @return
	 */
	public static int getAndroidOSVersion() {
		int osVersion;
		try {
			osVersion = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
		} catch (NumberFormatException e) {
			osVersion = 0;
		}

		return osVersion;
	}

	public static float getScale(Context mContext) {
		return mContext.getResources().getDisplayMetrics().density;
	}

	public static float getScaledDensity(Context mContext) {
		return mContext.getResources().getDisplayMetrics().scaledDensity;
	}

	/**
	 * dp转成px
	 * 
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(Context context, float dipValue) {
		return (int) (dipValue * getScale(context) + 0.5f);
	}

	/**
	 * px转成dp
	 * 
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		return (int) (pxValue / getScale(context) + 0.5f);
	}

	/**
	 * 获取评论和分享数
	 * 
	 * @param count
	 * @return
	 */
	public static String getCountFormatString(int count) {

		if (count > 0 && count < 1000) {
			return count + "";
		} else if (count >= 1000 && count < 99999) {
			return (count / 1000) + "k";
		} else if (count >= 99999) {
			return "99k";
		}

		return "0";
	}

	public static String getArticleCountFormatString(int count) {
		if (count > 0 && count < 1000) {
			return count + "";
		} else if (count >= 1000 && count < 99999) {
			return (count / 1000) + "k";
		} else if (count >= 99999) {
			return "99k";
		}

		return "0";
	}

	/**
	 * 防止多次点击
	 */

	private static long lastClickTime;

	public static boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;

		if (0 < timeD && timeD < 500) {
			return true;
		}
		lastClickTime = time;
		return false;
	}

	public static boolean isWeixinAvilible(Context context) {
		final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				if (pn.equals("com.tencent.mm")) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 判断qq是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isQQClientAvailable(Context context) {
		final PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				if (pn.equals("com.tencent.mobileqq")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 设置ListView子项高度
	 * 
	 * @param listView
	 */
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		// 获取Listview对应的adapter
		ListAdapter adapter = listView.getAdapter();
		if (adapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0, len = listView.getCount(); i < len; i++) {
			// listAdapter.getCount()返回数据项的数目
			View listItem = adapter.getView(i, null, listView);
			// 计算子项view的宽高
			listItem.measure(0, 0);
			// 统计所有子项的总高度
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (adapter.getCount()) - 1);
		// listView.getDividerHeight() 获取子项间分隔符占用的高度
		// params.height 最后得到整个ListView完整显示需要的高度
		listView.setLayoutParams(params);
	}

	/**
	 * 针对TextView显示中文中出现的排版错乱问题，通过调用此方法得以解决
	 * 
	 * @param str
	 * @return 返回全部为全角字符的字符串
	 */
	public static String toDBC(String str) {
		char[] c = str.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375) {
				c[i] = (char) (c[i] - 65248);
			}

		}
		return new String(c);
	}

	public static String getAppVersionName(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static int getAppVersionCode(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 分享功能
	 * 
	 * @param context
	 *            上下文
	 * @param activityTitle
	 *            Activity的名字
	 * @param msgTitle
	 *            消息标题
	 * @param msgText
	 *            消息内容
	 * @param imgPath
	 *            图片路径，不分享图片则传null
	 */
	public void shareMsg(Context context, String activityTitle,
			String msgTitle, String msgText, String imgPath) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		if (imgPath == null || imgPath.equals("")) {
			intent.setType("text/plain"); // 纯文本
		} else {
			File f = new File(imgPath);
			if (f != null && f.exists() && f.isFile()) {
				intent.setType("image/jpg");
				Uri u = Uri.fromFile(f);
				intent.putExtra(Intent.EXTRA_STREAM, u);
			}
		}
		intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
		intent.putExtra(Intent.EXTRA_TEXT, msgText);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(Intent.createChooser(intent, activityTitle));
	}

	public static boolean isAppOnForeground(Context mContext) {
		ActivityManager am = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(mContext.getPackageName())) {
				return false;
			}
		}
		return true;
	}

}
