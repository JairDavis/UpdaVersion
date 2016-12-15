package com.example.updateversion.http;

import android.content.Context;

import com.example.updateversion.utils.NetworkUtils;
import com.lidroid.xutils.util.LogUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

/**
 * 数据请求类
 * 
 * @Create By Jair_Huang on 2016年6月14日 下午6:22:34
 */
public class HttpRequestHelper {
	private static AsyncHttpClient client;

	private static HttpRequestHelper mInstance;

	private HttpRequestHelper() {
		if (client == null) {
			client = new AsyncHttpClient();
			client.setUserAgent("android_update");
			client.setTimeout(11000);// 设置链接超时，如果不设置，默认为10s
		}
	}

	public static HttpRequestHelper getInstance() {
		if (mInstance == null) {
			synchronized (HttpRequestHelper.class) {
				if (mInstance == null) {
					mInstance = new HttpRequestHelper();
				}
			}
		}
		return mInstance;
	}

	/**
	 * GET请求
	 * 
	 * @param mRequestURL
	 *            请求URL
	 * @param mHandler
	 *            数据处理句柄
	 */
	public void getHttpRequestAsync(String mRequestURL,
			AsyncHttpResponseHandler mHandler) {
		client.get(mRequestURL, mHandler);
	}

	/**
	 * POST请求
	 * 
	 * @param mRequestURL
	 *            请求
	 * @param params
	 *            请求参数
	 * @param mHandler
	 *            数据处理句柄
	 */
	public void postHttpRequestAsync(String mRequestURL, RequestParams params,
			AsyncHttpResponseHandler mHandler) {
		client.post(mRequestURL, params, mHandler);
	}

	/**
	 * GET请求
	 * 
	 * @param mContext
	 *            上下文菜单
	 * @param url
	 *            请求URL
	 * @param params
	 *            请求参数
	 * @param mHandler
	 *            处理句柄
	 */
	public void getHttpRequestAsync(Context mContext, String url,
			RequestParams params, AsyncHttpResponseHandler mHandler) {
		if (NetworkUtils.isNetworkAvailable(mContext)) {
			client.get(url, params, mHandler);
		}
	}

	/**
	 * HEAD请求
	 * 
	 * @param mContext
	 * @param url
	 * @param mHandler
	 */
	public void headHttpRequestAsyns(Context mContext, String url,
			AsyncHttpResponseHandler mHandler) {
		client.head(mContext, url, mHandler);
	}

	/**
	 * 检查更新
	 * 
	 * @param params
	 * @param responseHandler
	 */
	public RequestHandle checkUpdate(Context mContext, String url,
			RequestParams params, AsyncHttpResponseHandler responseHandler) {
		LogUtils.d("1." + url + "?" + params);
		return client.get(url, params, responseHandler);
	}

}
