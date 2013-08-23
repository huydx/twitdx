package com.cookpadintern.twitdx.core_ext;
/*
author:huydx
github:https://github.com/huydx
 */


import android.app.Activity;
import android.app.Application;

import com.cookpadintern.twitdx.model.TwitterAccount;

public class MainApplication extends Application {
    private TwitterAccount mAccount;
    private Activity mCurrentActivity = null;

    public void onCreate() {
        super.onCreate();
        mAccount = new TwitterAccount(this);
    }

    public TwitterAccount getAccount() {
        return mAccount;
    }

    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }
}

