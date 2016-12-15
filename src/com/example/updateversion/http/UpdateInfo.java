package com.example.updateversion.http;

/**
 * 更新信息（包括应用名、版本名、版本号、系统版本、sdk版本、更新信息、apk地址、是否强制更新）
 * 
 * @Create By Jair_Huang on 2016年6月14日 下午6:23:00
 */
public class UpdateInfo {
	private String name;
	private String versionName;
	private int versionCode;
	private String osVersion;
	private String sdkVersion;
	private String features;
	private String updateUrl;
	private int lastForceUpdate;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getSdkVersion() {
		return sdkVersion;
	}

	public void setSdkVersion(String sdkVersion) {
		this.sdkVersion = sdkVersion;
	}

	public String getFeatures() {
		return features;
	}

	public void setFeatures(String features) {
		this.features = features;
	}

	public String getUpdateUrl() {
		return updateUrl;
	}

	public void setUpdateUrl(String updateUrl) {
		this.updateUrl = updateUrl;
	}

	public int getLastForceUpdate() {
		return lastForceUpdate;
	}

	public void setLastForceUpdate(int lastForceUpdate) {
		this.lastForceUpdate = lastForceUpdate;
	}

	@Override
	public String toString() {
		return "UpdateInfo [name=" + name + ", versionName=" + versionName
				+ ", versionCode=" + versionCode + ", osVersion=" + osVersion
				+ ", sdkVersion=" + sdkVersion + ", features=" + features
				+ ", updateUrl=" + updateUrl + "]";
	}

}
