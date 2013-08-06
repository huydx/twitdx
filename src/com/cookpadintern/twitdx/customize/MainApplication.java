package com.cookpadintern.twitdx.customize;
/*
author:huydx
github:https://github.com/huydx
 */


import android.app.Activity;
import android.app.Application;

public class MainApplication extends Application {
    public void onCreate() {
        super.onCreate();
    }

    private Activity mCurrentActivity = null;
    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }
}

