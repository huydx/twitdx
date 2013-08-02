/*
author:huydx
github:https://github.com/huydx
*/
package com.cookpad.intern.twitdx.customize;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {
    protected MainApplication mMainApp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainApp = (MainApplication)this.getApplicationContext();
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

    private void clearReferences(){
        Activity currActivity = mMainApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            mMainApp.setCurrentActivity(null);
    }
}

