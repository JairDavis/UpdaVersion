package com.example.updateversion.Interface;

import com.example.updateversion.http.UpdateInfo;

public interface OnCheckUpdateListener {
	public void onSuccess(UpdateInfo updateInfo);

	public void onFailure();
}
