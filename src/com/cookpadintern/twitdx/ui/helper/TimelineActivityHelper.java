package com.cookpadintern.twitdx.ui.helper;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import twitter4j.Status;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import com.cookpadintern.twitdx.common.Const;
import com.cookpadintern.twitdx.core_ext.BaseActivityHelper;

public class TimelineActivityHelper extends BaseActivityHelper {
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

		if (status.getMediaEntities().length > 0) {
			// put first image in tweets (in case twitpic or yfrog)
			map.put(Const.KEY_MEDIA, status.getMediaEntities()[0].getMediaURL().toString());
		} else {
			map.put(Const.KEY_MEDIA, null);
		}

		return map;
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
