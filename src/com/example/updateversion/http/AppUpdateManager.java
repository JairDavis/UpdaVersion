package com.example.updateversion.http;

import java.io.File;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.updateversion.MainActivity;
import com.example.updateversion.R;
import com.example.updateversion.Interface.OnCheckUpdateListener;
import com.example.updateversion.utils.ApkUtils;
import com.example.updateversion.utils.FileUtils;
import com.example.updateversion.utils.ToastUtils;
import com.example.updateversion.utils.Utils;
import com.lidroid.xutils.util.LogUtils;
import com.loopj.android.http.RequestHandle;

/**
 * 
 * @Create By Jair_Huang on 2016年6月14日 下午6:21:40
 */
public class AppUpdateManager implements OnCheckUpdateListener {

	private Context mContext;

	// 版本号
	private String versionName = null;

	// 下载地址
	private String downloadUrl = null;

	// 下载进度
	public static final int MSG_DOWNLOAD_PROGRESS = 0;
	// 下载结果
	public static final int MSG_DOWNLOAD_RESULT = 1;
	// 下载成功
	public static final int FLAG_DOWNLOAD_SUCCESS = 0;
	// 取消下载
	public static final int FLAG_CANCEL_UPDATE = -1;
	// 空间不足
	public static final int FLAG_NO_ENOUGH_SPACE = 1;
	// 下载失败或者写入失败
	public static final int FLAG_DOWNLOAD_ERROR = 2;

	// handler用到的key
	public static final String KEY_DOWNLOAD_RESULT = "downloadResult";
	public static final String KEY_FILENAME = "fileName";
	public static final String KEY_PERCENT = "percent";

	private static AppUpdateManager instance;

	// 定义更新服务
	private AppUpdateService updateService = null;
	// 更新服务连接
	private ServiceConnection updateServiceConnection = null;

	// 进度对话框
	private ProgressDialog progressDialog = null;

	// 请求句柄
	private RequestHandle updateHandle;

	// 是否已绑定服务
	private boolean isBinded = false;

	// 根据它来判断是否显示消息通知
	private boolean isForeground = false;

	// 通知
	private Notification notification = null;
	private NotificationManager notificationManager = null;
	private NotificationCompat.Builder builder = null;

	public static AppUpdateManager getInstance(Context context) {
		if (instance == null) {
			synchronized (AppUpdateManager.class) {
				instance = new AppUpdateManager(context);
			}
		}
		return instance;
	}

	public AppUpdateManager(Context context) {
		this.mContext = context;
		LogUtils.d("6." + "初始化服务");

		builder = new NotificationCompat.Builder(mContext);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setTicker("下载完成");
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);
		notification = builder.build();

		// 获取通知服务
		notificationManager = (NotificationManager) mContext
				.getSystemService(mContext.NOTIFICATION_SERVICE);

		initServiceConnection();
		initProgressDialog();

	}

	/**
	 * 初始化ProgressDialog
	 */
	private void initProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(mContext);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setTitle(mContext.getString(R.string.update_app));
			progressDialog.setMax(100);
			progressDialog.setCancelable(true);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							// 取消下载
							updateHandle.cancel(true);
							ToastUtils.showShort(mContext,
									R.string.tip_cancelupdate);
							unBindService();
						}
					});
		}
	}

	/**
	 * 取消绑定服务
	 */
	private void unBindService() {
		Intent intent = new Intent(mContext, AppUpdateService.class);
		if (isBinded) {
			Log.d("service", "unbind service");
			mContext.unbindService(updateServiceConnection);
			mContext.stopService(intent);
			updateService = null;
			isBinded = false;
		} else {
			Log.d("service", "not binded");
		}
	}

	/**
	 * 初始化ServiceConnection
	 */
	private void initServiceConnection() {
		updateServiceConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d("ServiceConnection", "service connected");
				LogUtils.d("7." + "连接服务");
				AppUpdateService.AppUpdateBinder aBinder = (AppUpdateService.AppUpdateBinder) service;
				updateService = aBinder.getService();

				isBinded = true;
				// 为service设置handler
				updateService.setHandler(handler);
				// TODO: 执行下载任务
				updateService.downloadStart(downloadUrl, versionName);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d("ServiceConnection", "service disconnected");
				updateService = null;
				isBinded = false;

			}
		};
	}

	/**
	 * 定义检查更新的方法 
	 * 
	 * @parms void
	 * @Create By Jair_Huang on 2016年6月17日 上午9:00:38
	 */
	public void checkUpdate() {
		updateHandle = HttpRequestHelper.getInstance().checkUpdate(mContext,
				URLHelper.CHECK_UPDATE, null,
				new CheckUpdateAsynResponseHandler(mContext, this));// 创建回调接口拿到数据
	}

	/**
	 * 混合调用 先以startService方式启动服务 再以bindService方式绑定服务
	 */
	private void bindUpdateService() {
		LogUtils.d("5." + "启动服务");
		Intent intent = new Intent(mContext, AppUpdateService.class);
		mContext.startService(intent);
		mContext.bindService(intent, updateServiceConnection,
				Context.BIND_AUTO_CREATE);
	}

	/**
	 * 获取到更新信息回调
	 *
	 * @param updateInfo
	 */
	@Override
	public void onSuccess(UpdateInfo updateInfo) {
		// 相应操作
		LogUtils.d("3." + versionName + "-----" + downloadUrl);
		versionName = updateInfo.getVersionName();
		downloadUrl = updateInfo.getUpdateUrl();

		// 首先获取当前版本
		int currentVersionCode = Utils.getAppVersionCode(mContext);

		// 如果当前版本低于服务器版本，则需要弹出更新对话框
		if (updateInfo.getVersionCode() <= currentVersionCode) {
			ToastUtils.showShort(mContext, "已经是最新版");
		} else {
			showUpdateDialog(versionName, updateInfo.getFeatures());
		}
	}

	/**
	 * 显示更新对话框
	 *
	 * @param versionName
	 *            版本名
	 * @param features
	 *            更新信息
	 */
	private void showUpdateDialog(String versionName, String features) {
		LogUtils.d("4." + versionName + "-----" + features);
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
		alertBuilder.setTitle("更新应用 V" + versionName);
		alertBuilder.setMessage(features);
		alertBuilder.setCancelable(true);
		alertBuilder.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 这里启动服务下载apk
						bindUpdateService();
					}
				});
		alertBuilder.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		// 显示对话框
		alertBuilder.create().show();
	}

	/**
	 * 获取更新信息失败回调
	 */
	@Override
	public void onFailure() {

	}

	/**
	 * 用来显示ui的Handler
	 * 
	 * @Filed handler
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_DOWNLOAD_PROGRESS: // 更新进度
				updateProgress(msg);
				break;
			case MSG_DOWNLOAD_RESULT: // 下载结果
				handleDownloadResult(msg);
				break;
			}
		}
	};

	/**
	 * 更新下载进度
	 *
	 * @param msg
	 */
	private void updateProgress(Message msg) {
		if (updateService != null) {
			Notification sNotification = updateService.getNotification();
			NotificationManager sNotificationManager = updateService
					.getNotificationManager();

			// 得到下载进度百分比
			int percentage = msg.getData().getInt(AppUpdateManager.KEY_PERCENT);
			// 设置progress进度
			progressDialog.setProgress(percentage);
			progressDialog.show();

			// 在后台时在消息通知栏显示下载进度
			if (!isForeground) {
				if (sNotification != null) {
					RemoteViews remoteViews = sNotification.contentView;
					if (remoteViews != null) {
						remoteViews.setTextViewText(
								R.id.notification_update_progress_text,
								percentage + "%");
						remoteViews.setProgressBar(
								R.id.notification_update_progress_bar, 100,
								percentage, false);
						sNotificationManager
								.notify(AppUpdateService.NOTIFICATION_ID,
										sNotification);
					} else {
						Log.d("update", "remoteview null");
					}
				}
			}
		}
	}

	/**
	 * 下载结果
	 * 
	 * @param msg
	 *            void
	 * @Create By Jair_Huang on 2016年6月17日 上午8:57:39
	 */
	public void handleDownloadResult(Message msg) {

		// 拿到下载结果
		String fileName = msg.getData().getString(KEY_FILENAME);
		int downloadResult = msg.getData().getInt(KEY_DOWNLOAD_RESULT);

		switch (downloadResult) {
		case FLAG_CANCEL_UPDATE: // 取消下载
			ToastUtils.showShort(mContext, R.string.tip_cancelupdate);

			showNotification("取消下载", "已取消下载", new Intent(mContext,
					MainActivity.class));
			break;
		case FLAG_DOWNLOAD_ERROR: // 下载错误
			ToastUtils.showShort(mContext, R.string.tip_update_error);
			showNotification("更新出错", "更新出错，请稍后再试", new Intent(mContext,
					MainActivity.class));

			break;
		case FLAG_NO_ENOUGH_SPACE: // 空间不足
			ToastUtils.showShort(mContext, R.string.tip_no_enough_space);
			showNotification("下载出错，", "存储空间不足", new Intent(mContext,
					MainActivity.class));

			break;
		case FLAG_DOWNLOAD_SUCCESS: // 下载成功
			showInstallNotification("下载完毕", "点击安装");

			// 如果在前台，则安装apk
			if (isForeground) {
				ApkUtils.installApp(mContext,
						FileUtils.getCacheApkPath(fileName));
			}
			break;
		}

		progressDialog.dismiss();

		if (updateService != null) {
			unBindService();
		}
	}

	/**
	 * 在前台，消除notification
	 */
	public void appForeground() {
		isForeground = true;

		if (updateService != null) {
			updateService.cancelNotification();
		}
	}

	/**
	 * 在后台，显示notification
	 */
	public void appBackground() {
		isForeground = false;

		if (updateService != null) {
			updateService.notifyMessage();
		}

	}

	/**
	 * 显示通知
	 *
	 * @param nTitle
	 * @param nMessage
	 * @param intent
	 */
	public void showNotification(String nTitle, String nMessage, Intent intent) {
		PendingIntent contentIntent = PendingIntent.getActivity(mContext,
				AppUpdateService.NOTIFICATION_ID, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		setNotification(nTitle, nTitle, nMessage, contentIntent);

		notificationManager.notify(AppUpdateService.NOTIFICATION_ID,
				notification);
	}

	/**
	 * 设置消息通知内容
	 *
	 * @param ticker
	 * @param title
	 * @param text
	 * @param intent
	 */
	private void setNotification(String ticker, String title, String text,
			PendingIntent intent) {

		if (notification != null) {
			// change notification and repo
			if (builder != null) {
				builder.setTicker(ticker);
				builder.setContentTitle(title);
				builder.setContentText(text);
				builder.setAutoCancel(true);
				builder.setContent(null);
				builder.setContentIntent(intent);

				notification = builder.build();
			}

		}
	}

	/**
	 * 显示安装的通知
	 *
	 * @param nTitle
	 * @param nMessage
	 */
	public void showInstallNotification(String nTitle, String nMessage) {
		Intent installIntent = new Intent(Intent.ACTION_VIEW);
		installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		installIntent.setDataAndType(Uri.fromFile(new File(FileUtils
				.getCacheApkPath(FileUtils.getApkFileName(versionName)))),
				"application/vnd.android.package-archive");

		showNotification(nTitle, nMessage, installIntent);

	}

}
