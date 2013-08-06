package com.cookpadintern.twitdx.activity.helper;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import twitter4j.Status;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.cookpadintern.twitdx.common.Const;

public class TimelineActivityHelper {
    private Context mContext;

    public TimelineActivityHelper(Context context) {
        mContext = context;
    }

    public synchronized HashMap<String, String> makeStatusMap(Status status) {
        HashMap<String, String> map = new HashMap<String, String>();

        map.put(Const.KEY_UNAME, status.getUser().getScreenName());
        map.put(Const.KEY_TWEET, status.getText());
        String format = "MM-dd HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.JAPAN);
        map.put(Const.KEY_DATE, sdf.format(status.getCreatedAt()));
        map.put(Const.KEY_AVATAR, status.getUser().getProfileImageURL());

        return map;
    }

    public void slideMenuAnimate(LinearLayout content, int menuWidth) {
        LinearLayout.LayoutParams layoutParams =
                (LinearLayout.LayoutParams) content.getLayoutParams();

        int marginX, animateFromX, animateToX = 0;
        // menu is hidden
        if (layoutParams.leftMargin == -menuWidth) {
            animateFromX = 0;
            animateToX = menuWidth;
            marginX = 0;
        } else { // menu is visible
            animateFromX = 0;
            animateToX = -menuWidth;
            marginX = -menuWidth;
        }
        slideMenuIn(content, animateFromX, animateToX, marginX);
    }

    private void slideMenuIn(final LinearLayout content, int animateFromX, int animateToX, final int marginX) {
        final LinearLayout.LayoutParams layoutParams =
                (LinearLayout.LayoutParams) content.getLayoutParams();

        TranslateAnimation slideAnimation =
                new TranslateAnimation(animateFromX, animateToX, 0, 0);
        slideAnimation.setDuration(200);
        slideAnimation.setFillEnabled(true);
        slideAnimation.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                layoutParams.setMargins(marginX, 0, 0, 0);
                content.setLayoutParams(layoutParams);
            }

            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        content.startAnimation(slideAnimation);
    }

    public void openTweetDialog(final TweetDialogClickListener listener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

        alert.setTitle("Twitter");
        alert.setMessage("Update status");

        // Set an EditText view to get user input
        final EditText input = new EditText(mContext);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                listener.onOkClick(input.getText().toString());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                return;
            }
        });

        alert.show();
    }

    public interface TweetDialogClickListener {
        void onOkClick(String tweet);
        void onCancelClick();
    }
}
