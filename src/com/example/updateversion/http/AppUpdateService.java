package com.example.updateversion.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.updateversion.MainActivity;
import com.example.updateversion.R;
import com.example.updateversion.utils.FileUtils;
import com.lidroid.xutils.util.LogUtils;

/**
 * 
 * @Create By Jair_Huang on 2016年6月14日 下午6:22:10
 */
public class AppUpdateService extends Service {

	private final AppUpdateBinder aBinder = new AppUpdateBinder();
	// 文件大小
	private long fileSize = 0;
	// 已经下载文件的大小
	private long downedSize = 0;
	// 下载进度
	private int downloadPercentage = 0;

	// UI线程处理句柄
	private Handler handler;

	// 是否取消了升级
	private boolean isCancelUpdate = false;

	// 消息通知管理对象
	private NotificationManager notificationManager = null;
	// 消息通知对象
	private Notification notification = null;
	// 消息通知id
	public static final int NOTIFICATION_ID = 1000;

	/**
	 * 设置handler
	 * 
	 * @param handler
	 */
	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public NotificationManager getNotificationManager() {
		return notificationManager;
	}

	public void setNotificationManager(NotificationManager notificationManager) {
		this.notificationManager = notificationManager;
	}

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}

	public class AppUpdateBinder extends Binder {
		public AppUpdateService getService() {
			return AppUpdateService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		initNotification();
	}

	/**
	 * 初始化消息通知
	 */
	public void initNotification() {
		long when = System.currentTimeMillis();

		// V7包下的NotificationCompat
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this);
		// Ticker是状态栏显示的提示
		builder.setTicker("应用更新");
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setWhen(when);
		builder.setAutoCancel(true);
		// 自定义contentView
		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.notification_update);
		contentView.setTextViewText(R.id.notification_update_progress_text,
				"0%");
		contentView.setImageViewResource(R.id.notification_update_image,
				R.drawable.ic_launcher);
		// 为notification设置contentView
		builder.setContent(contentView);

		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(contentIntent);
		// 构建一个notification
		notification = builder.build();
	}

	public void notifyMessage() {
		if (notification != null && notificationManager != null) {
			// 显示通知
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
	}

	/**
	 * 消除通知
	 */
	public void cancelNotification() {
		notificationManager.cancel(NOTIFICATION_ID);
	}

	/**
	 * 必须要实现的方法
	 *
	 * @param intent
	 * @return 返回IBinder对象
	 */
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return aBinder;
	}

	/**
	 * 服务被启动时回调此方法
	 *
	 * @param intent
	 * @param flags
	 * @param startId
	 * @return
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。
		// 随后系统会尝试重新创建service，由于服务状态为开始状态，
		// 所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。
		// 如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。
		return START_STICKY;
	}

	/**
	 * 开一个线程来下载apk文件
	 *
	 * @param url
	 *            apk地址
	 * @param version
	 *            版本
	 */
	public void downloadStart(String url, String version) {
		LogUtils.d("8." + "开始下载");
		DownloadRunnable downloadRunnable = new DownloadRunnable(url, version);
		Thread thread = new Thread(downloadRunnable);
		thread.start();
	}

	/**
	 * 下载类
	 * 
	 * @Create By Jair_Huang on 2016年6月17日 上午8:40:27
	 */
	class DownloadRunnable implements Runnable {
		private String url;
		private String version;
		private InputStream input = null;
		private int flag = 0;
		private String fileName;

		public DownloadRunnable(String url, String version) {
			this.url = url;
			this.version = version;
		}

		@Override
		public void run() {
			fileName = "app_v" + version + ".apk";
			LogUtils.d("9." + "下载名：" + fileName);
			// 检查文件是否存在，如果存在则删除旧文件
			if (FileUtils.checkFileExistence(fileName)) {
				FileUtils.deleteApkFile(FileUtils.getCacheApkPath(fileName));
			}

			// 检查是否有足够空间
			if (FileUtils.isEnoughFreeSpace()) {
				try {
					input = getInputStream(url);
					if (input != null) {
						// 这时写入SDcard
						File resultFile = writeFile(fileName, input, fileSize);
						if (resultFile == null) {
							flag = AppUpdateManager.FLAG_DOWNLOAD_ERROR;
						} else {
							flag = AppUpdateManager.FLAG_DOWNLOAD_SUCCESS;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						input.close();
					} catch (Exception e) {
						// 超时，input为null
						flag = AppUpdateManager.FLAG_DOWNLOAD_ERROR;
						e.printStackTrace();
					}
				}
			} else { // sdcard空间不足
				flag = AppUpdateManager.FLAG_NO_ENOUGH_SPACE;
			}

			sendDownloadResult(flag, fileName);
		}
	}

	/**
	 * 通过handle通知消息
	 * 
	 * @param flag
	 * @param fileName
	 */
	private void sendDownloadResult(int flag, String fileName) {
		LogUtils.d("10." + "下载状态和名名称：" + flag + "---------" + fileName);
		Message msg = new Message();
		msg.what = AppUpdateManager.MSG_DOWNLOAD_RESULT;
		Bundle bundle = new Bundle();
		bundle.putInt(AppUpdateManager.KEY_DOWNLOAD_RESULT, flag);
		bundle.putString(AppUpdateManager.KEY_FILENAME, fileName);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	/**
	 * 写入文件
	 *
	 * @param fileName
	 *            文件名
	 * @param input
	 *            输入流
	 * @param fileSize
	 *            文件大小
	 * @return
	 */
	private File writeFile(String fileName, InputStream input, long fileSize) {
		File file = null;
		OutputStream output = null;
		try {
			// 创建缓存目录
			FileUtils.createCacheDirectory();

			// 4 * 1024的Buffer
			byte buffer[] = new byte[4 * 1024];
			int temp;
			// 在sd卡创建文件
			file = FileUtils.createFileInSd(FileUtils.CACHDIR + File.separator
					+ fileName);
			// 文件输出流
			output = new FileOutputStream(file);

			// 循环写入
			while ((temp = input.read(buffer)) != -1) {
				output.write(buffer, 0, temp);
				downedSize = downedSize + temp;

				int tempPercent = downloadPercentage;
				// 计算下载百分比
				downloadPercentage = (int) (downedSize * 100.0F / fileSize);

				// 打印下载进度
				// Log.d("percent", downloadPercentage + "");

				// 'cause the percentage may be the same for some a/b
				if (downloadPercentage != tempPercent) {
					Message msg = new Message();
					msg.what = AppUpdateManager.MSG_DOWNLOAD_PROGRESS;
					Bundle bundle = new Bundle();
					bundle.putInt(AppUpdateManager.KEY_PERCENT,
							downloadPercentage);
					msg.setData(bundle);
					handler.sendMessage(msg);
				}

			}
			output.flush();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				output.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return file;
	}

	/**
	 * 获取输入流
	 *
	 * @param url
	 *            文件地址
	 * @return
	 * @throws IOException
	 */
	private InputStream getInputStream(String url) throws IOException {
		InputStream input = null;
		try {
			URL urlS = new URL(url);
			// 打开连接
			HttpURLConnection httpConn = (HttpURLConnection) urlS
					.openConnection();
			// 设置header参数（这里根据服务端来设）
			httpConn.setRequestProperty("Connection", "close");
			// 设置User-Agent
			httpConn.setRequestProperty("User-Agent", "android updateversion");
			// 获取文件大小
			fileSize = httpConn.getContentLength();
			// 获取响应信息
			httpConn.getResponseMessage();
			// 设置连接超时
			httpConn.setConnectTimeout(6 * 1000);
			// 如果返回码为200说明下载成功
			if (httpConn.getResponseCode() == 200) {
				input = httpConn.getInputStream();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return input;
	}

	/**
	 * 取消升级
	 */
	public void cancelUpdate() {
		isCancelUpdate = true;
	}

	/**
	 * 服务被销毁时回调
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
