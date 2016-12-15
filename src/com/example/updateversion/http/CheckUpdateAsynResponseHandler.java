package com.example.updateversion.http;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.example.updateversion.R;
import com.example.updateversion.Interface.OnCheckUpdateListener;
import com.lidroid.xutils.util.LogUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class CheckUpdateAsynResponseHandler extends JsonHttpResponseHandler {

	private Context mContext;

	private ProgressDialog progressDialog;

	private OnCheckUpdateListener listener;

	public CheckUpdateAsynResponseHandler(Context context,
			OnCheckUpdateListener listener) {
		this.mContext = context;
		this.listener = listener;

		progressDialog = new ProgressDialog(context);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(context.getString(R.string.tip_requesting));
		progressDialog.setCancelable(true);
	}

	/**
	 * 请求开始时回调
	 */
	@Override
	public void onStart() {
		super.onStart();
		progressDialog.show();
	}

	/**
	 * 响应成功时回调
	 * 
	 * @param statusCode
	 * @param headers
	 * @param response
	 */
	@Override
	public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
		super.onSuccess(statusCode, headers, response);

		if (response != null) {
			LogUtils.d("2." + response.toString());

			UpdateInfo updateInfo = new UpdateInfo();
			try {
				// 对数据进行解析
				JSONObject ireaderObj = response
						.optJSONObject("android_update");
				if (ireaderObj != null) {
					updateInfo.setName(ireaderObj.optString("name"));
					updateInfo.setVersionName(ireaderObj
							.optString("app_version"));
					updateInfo
							.setVersionCode(ireaderObj.optInt("version_code"));
					updateInfo.setFeatures(ireaderObj.optString("features"));
					updateInfo.setUpdateUrl(ireaderObj.optString("update_url"));
					updateInfo.setSdkVersion(ireaderObj
							.optString("sdk_version"));
					updateInfo.setOsVersion(ireaderObj.optString("os_version"));

					// 解析json数据
					listener.onSuccess(updateInfo);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * 请求结束时回调
	 */
	@Override
	public void onFinish() {
		super.onFinish();
		progressDialog.dismiss();
	}

	/**
	 * 响应失败时回调
	 * 
	 * @param statusCode
	 * @param headers
	 * @param responseString
	 * @param throwable
	 */
	@Override
	public void onFailure(int statusCode, Header[] headers,
			String responseString, Throwable throwable) {
		super.onFailure(statusCode, headers, responseString, throwable);

		listener.onFailure();
	}

	/**
	 * 复写取消时操作
	 * 
	 * @see com.loopj.android.http.AsyncHttpResponseHandler#onCancel()
	 */
	@Override
	public void onCancel() {
		super.onCancel();
		progressDialog.cancel();
	}
}
