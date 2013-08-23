/*
author:huydx
github:https://github.com/huydx
 */
package com.cookpadintern.twitdx.core_ext;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {
	protected BaseActivityHelper mActivityHelper;


	protected MainApplication mMainApp;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMainApp = (MainApplication) this.getApplicationContext();
	}

	protected MainApplication getTwitdxApplication() {
		return (MainApplication) getApplication();
	}

	protected void onResume() {
		super.onResume();
		mMainApp.setCurrentActivity(this);
	}

	protected void onPause() {
		clearReferences();
		super.onPause();
	}

	protected void onDestroy() {
		clearReferences();
		super.onDestroy();
	}

	private void clearReferences() {
		Activity currActivity = mMainApp.getCurrentActivity();
		if (currActivity != null && currActivity.equals(this))
			mMainApp.setCurrentActivity(null);
	}
	
	public BaseActivityHelper getActivityHelper() {
		return mActivityHelper;
	}

	public void setActivityHelper(BaseActivityHelper mActivityHelper) {
		this.mActivityHelper = mActivityHelper;
	}
}
